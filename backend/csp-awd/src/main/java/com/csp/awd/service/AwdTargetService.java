package com.csp.awd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.awd.dto.TargetVO;
import com.csp.awd.entity.AwdTarget;
import com.csp.awd.enums.TargetStatus;
import com.csp.awd.mapper.AwdTargetMapper;
import com.csp.common.exception.BizException;
import com.csp.infra.docker.TargetInstance;
import com.csp.infra.docker.TargetScheduler;
import com.csp.infra.docker.TargetSpec;
import com.csp.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 靶场编排服务：为参赛战队批量拉起 / 重置 / 销毁靶机，并同步状态到库。
 *
 * <p>容器调度委托给 csp-infra 的 {@link TargetScheduler}（无 Docker 环境自动降级模拟模式）。
 * 本服务负责：幂等编排、DB 状态持久化、健康巡检回写。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwdTargetService {

    private final AwdTargetMapper targetMapper;
    private final TargetScheduler scheduler;
    private final RedisService redisService;
    private final AwdNameResolver nameResolver;
    /** 题库只读访问：用于取题目配置的靶场镜像 */
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /** 靶机对外访问主机（端口映射在宿主机上） */
    @org.springframework.beans.factory.annotation.Value("${csp.docker.public-host:127.0.0.1}")
    private String publicHost;

    // ------------------------------------------------------------------ 查询

    public List<TargetVO> listByContest(Long contestId, Long teamId) {
        LambdaQueryWrapper<AwdTarget> q = new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId)
                .orderByAsc(AwdTarget::getTeamId)
                .orderByAsc(AwdTarget::getQuestionId);
        if (teamId != null) q.eq(AwdTarget::getTeamId, teamId);
        return toVOs(contestId, targetMapper.selectList(q));
    }

    public AwdTarget requireTarget(Long contestId, Long teamId, Long questionId) {
        AwdTarget t = targetMapper.selectOne(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId)
                .eq(AwdTarget::getTeamId, teamId)
                .eq(AwdTarget::getQuestionId, questionId)
                .last("LIMIT 1"));
        if (t == null) throw BizException.notFound("靶机");
        return t;
    }

    private List<AwdTarget> findByTeam(Long contestId, Long teamId) {
        return targetMapper.selectList(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId)
                .eq(AwdTarget::getTeamId, teamId)
                .orderByAsc(AwdTarget::getQuestionId));
    }

    // ------------------------------------------------------------------ 编排

    /**
     * 为整个竞赛的所有已就绪靶机做一次幂等编排：
     * 已有 RUNNING 靶机保持不变，缺失或异常的重新拉起。
     *
     * <p>战队列表来源：本模块不直接依赖赛事模块（避免循环依赖），
     * 由调用方通过 {@link #provisionTeam} 显式传参；此方法用于开局时的“自愈式”巡检。</p>
     */
    public int provisionForContest(Long contestId) {
        List<AwdTarget> existing = targetMapper.selectList(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId));

        List<Long> teams = jdbcTemplate.queryForList(
                "SELECT id FROM contest_team WHERE contest_id = ? AND deleted = 0 AND status = 'APPROVED' ORDER BY id",
                Long.class, contestId);

        int healed = 0;
        java.util.Set<String> present = new java.util.HashSet<>();
        for (AwdTarget t : existing) {
            // 只维护仍有效参赛队的靶机；退赛/已删除战队的历史行不再拉起
            if (!teams.contains(t.getTeamId())) continue;
            present.add(t.getTeamId() + ":" + t.getQuestionId());
            if (TargetStatus.RUNNING.name().equals(t.getStatus())) continue;
            try {
                provisionOne(t.getContestId(), t.getTeamId(), t.getQuestionId(), null, null, null);
                healed++;
            } catch (Exception e) {
                log.warn("靶机自愈失败 contest={} team={} q={}: {}",
                        t.getContestId(), t.getTeamId(), t.getQuestionId(), e.getMessage());
            }
        }

        // 补齐：参赛队 × 赛题 缺失的靶机（自愈只修复已有行，不会新建）
        List<Long> questions = jdbcTemplate.queryForList(
                "SELECT cq.question_id FROM contest_question cq JOIN question q ON q.id = cq.question_id "
                        + "WHERE cq.contest_id = ? AND q.bank_type = 'AWD' AND q.deleted = 0 ORDER BY cq.question_id",
                Long.class, contestId);
        int created = 0;
        for (Long teamId : teams) {
            for (Long qid : questions) {
                if (!present.add(teamId + ":" + qid)) continue;
                try {
                    provisionOne(contestId, teamId, qid, null, null, null);
                    created++;
                } catch (Exception e) {
                    log.warn("靶机编排失败 contest={} team={} q={}: {}",
                            contestId, teamId, qid, e.getMessage());
                }
            }
        }
        log.info("靶场编排完成 contest={} 自愈 {} 台，新建 {} 台", contestId, healed, created);
        return healed + created;
    }

    /**
     * 为单个战队编排一台靶机（幂等）：已 RUNNING 直接返回。
     *
     * @param image         镜像名（可为空，使用题目默认镜像）
     * @param containerPort 容器端口（默认 80）
     * @param env           注入环境变量（如 FLAG）
     */
    @Transactional(rollbackFor = Exception.class)
    public TargetVO provisionOne(Long contestId, Long teamId, Long questionId,
                                 String image, Integer containerPort, Map<String, String> env) {
        AwdTarget existing = targetMapper.selectOne(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId)
                .eq(AwdTarget::getTeamId, teamId)
                .eq(AwdTarget::getQuestionId, questionId)
                .last("LIMIT 1"));
        if (existing != null && TargetStatus.RUNNING.name().equals(existing.getStatus())) {
            return toVO(existing);
        }

        // 每队独立动态 flag：未显式指定时自动生成，保证同一题目不同战队 flag 不同
        Map<String, String> envFinal = env == null ? new HashMap<>() : new HashMap<>(env);
        if (!envFinal.containsKey("FLAG")) {
            envFinal.put("FLAG", "flag{awd_" + teamId + "_" + questionId + "_"
                    + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "}");
        }

        TargetSpec spec = TargetSpec.builder()
                .contestId(contestId)
                .teamId(teamId)
                .questionId(questionId)
                .image(image == null || image.isBlank() ? defaultImage(questionId) : image)
                .containerPort(containerPort == null ? 80 : containerPort)
                .sshPort(22)
                .env(envFinal)
                .cpuLimit("1.0")
                .memoryLimit("512m")
                .internalOnly(true)
                .build();

        TargetInstance inst = scheduler.create(spec);

        // 初始化 SSH：每台靶机独立随机密码，写入容器并启动 sshd
        String sshUser = "root";
        String sshPassword = "Awd@" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        boolean sshReady = inst.getSshPort() > 0 && !inst.isSimulated()
                && initSsh(inst.getContainerId(), sshUser, sshPassword);

        AwdTarget t = existing == null ? new AwdTarget() : existing;
        t.setContestId(contestId);
        t.setTeamId(teamId);
        t.setQuestionId(questionId);
        t.setContainerId(inst.getContainerId());
        t.setIp(inst.getIp());
        t.setPort(inst.getPort());
        t.setSshPort(inst.getSshPort() > 0 ? inst.getSshPort() : null);
        t.setSshUser(sshUser);
        t.setSshPassword(sshReady ? sshPassword : null);
        t.setStatus(TargetStatus.RUNNING.name());
        t.setHealth(1);
        t.setLastCheckAt(LocalDateTime.now());

        if (existing == null) {
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            targetMapper.insert(t);
        } else {
            t.setUpdatedAt(LocalDateTime.now());
            targetMapper.updateById(t);
        }
        log.info("靶机就绪 contest={} team={} q={} {}:{} simulated={}",
                contestId, teamId, questionId, inst.getIp(), inst.getPort(), inst.isSimulated());
        return toVO(t);
    }

    /** 重置靶机（回到初始状态，flag 重新注入）。 */
    public TargetVO reset(Long contestId, Long teamId, Long questionId, String newFlag) {
        AwdTarget t = requireTarget(contestId, teamId, questionId);
        Map<String, String> env = new HashMap<>();
        if (newFlag != null) env.put("FLAG", newFlag);
        TargetSpec spec = TargetSpec.builder()
                .contestId(contestId)
                .teamId(teamId)
                .questionId(questionId)
                .image(defaultImage(questionId))
                .containerPort(t.getPort() == null ? 80 : 80)
                .sshPort(22)
                .env(env)
                .build();
        TargetInstance inst = scheduler.reset(spec);

        String sshUser = "root";
        String sshPassword = "Awd@" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        boolean sshReady = inst.getSshPort() > 0 && !inst.isSimulated()
                && initSsh(inst.getContainerId(), sshUser, sshPassword);

        t.setContainerId(inst.getContainerId());
        t.setIp(inst.getIp());
        t.setPort(inst.getPort());
        t.setSshPort(inst.getSshPort() > 0 ? inst.getSshPort() : null);
        t.setSshUser(sshUser);
        t.setSshPassword(sshReady ? sshPassword : null);
        t.setStatus(TargetStatus.RUNNING.name());
        t.setHealth(1);
        t.setLastCheckAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        targetMapper.updateById(t);
        return toVO(t);
    }

    /**
     * 轮次开局的 flag 轮换：为每个靶机生成新 flag、重启容器注入新 flag、并写入 Redis 供校验。
     *
     * <p>需求第 60 段要求“每轮自动：刷新 flag、执行 check 脚本、攻防计分”。
     * 此前 {@code transit(→RUNNING)} 只拉起靶机，从未按轮次签发/注入 flag，
     * 导致新轮次既无 flag 可攻、容器内也仍是上一轮的旧 flag。</p>
     *
     * @return 成功轮换的靶机数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int rotateFlagsForRound(Long contestId, Integer roundNo) {
        List<AwdTarget> targets = targetMapper.selectList(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId));
        int n = 0;
        for (AwdTarget t : targets) {
            try {
                String newFlag = "flag{awd_" + t.getTeamId() + "_" + t.getQuestionId() + "_r" + roundNo
                        + "_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "}";

                // 1) 写入 Redis（该轮 flag 校验源）
                String key = AwdKeys.flagOne(contestId, roundNo, t.getTeamId(), t.getQuestionId());
                // 必须与 AwdScoreService 的读取方式一致：纯字符串读写，避免 JSON 反序列化报错
                redisService.setString(key, newFlag, java.time.Duration.ofHours(24));
                redisService.hSetString(AwdKeys.roundFlags(contestId, roundNo),
                        t.getTeamId() + ":" + t.getQuestionId(), newFlag);

                // 2) 直接写入容器内的 flag 文件（不重启，保留运行状态与 SSH 端口）
                boolean written = writeFlagToContainer(t.getContainerId(), newFlag);

                // 3) 写文件失败才回退到重建容器并注入 FLAG 环境变量
                if (!written) {
                    Map<String, String> env = new HashMap<>();
                    env.put("FLAG", newFlag);
                    TargetSpec spec = TargetSpec.builder()
                            .contestId(contestId)
                            .teamId(t.getTeamId())
                            .questionId(t.getQuestionId())
                            .image(defaultImage(t.getQuestionId()))
                            .containerPort(80)
                            .sshPort(22)
                            .env(env)
                            .build();
                    TargetInstance inst = scheduler.reset(spec);
                    t.setContainerId(inst.getContainerId());
                    t.setIp(inst.getIp());
                    t.setPort(inst.getPort());
                    if (inst.getSshPort() > 0) {
                        t.setSshPort(inst.getSshPort());
                    }
                }
                t.setStatus(TargetStatus.RUNNING.name());
                t.setHealth(1);
                t.setLastCheckAt(LocalDateTime.now());
                t.setUpdatedAt(LocalDateTime.now());
                targetMapper.updateById(t);
                n++;
                log.info("轮次 flag 已轮换 contest={} round={} team={} q={} port={} 写文件={}",
                        contestId, roundNo, t.getTeamId(), t.getQuestionId(), t.getPort(), written);
            } catch (Exception e) {
                log.error("轮次 flag 轮换失败 contest={} round={} team={}",
                        contestId, roundNo, t.getTeamId(), e);
            }
        }
        return n;
    }

    /** 销毁单台靶机。 */
    public boolean destroy(Long contestId, Long teamId, Long questionId) {
        AwdTarget t = requireTarget(contestId, teamId, questionId);
        boolean ok = scheduler.destroy(containerName(t));
        t.setStatus(ok ? TargetStatus.STOPPED.name() : TargetStatus.ERROR.name());
        t.setUpdatedAt(LocalDateTime.now());
        targetMapper.updateById(t);
        return ok;
    }

    /** 销毁整个竞赛的全部靶机（赛事结束清理）。 */
    public int destroyContest(Long contestId) {
        List<AwdTarget> list = targetMapper.selectList(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId));
        int n = 0;
        for (AwdTarget t : list) {
            try {
                scheduler.destroy(containerName(t));
                t.setStatus(TargetStatus.STOPPED.name());
                t.setUpdatedAt(LocalDateTime.now());
                targetMapper.updateById(t);
                n++;
            } catch (Exception e) {
                log.warn("销毁靶机失败 id={}: {}", t.getId(), e.getMessage());
            }
        }
        return n;
    }

    /**
     * 健康巡检：刷新所有 RUNNING 靶机状态与资源用量，用于 SLA 判定与大盘展示。
     * 返回健康台数。
     */
    public int healthCheck(Long contestId) {
        List<AwdTarget> list = targetMapper.selectList(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId)
                .eq(AwdTarget::getStatus, TargetStatus.RUNNING.name()));
        int healthy = 0;
        for (AwdTarget t : list) {
            String state = scheduler.status(containerName(t));
            boolean up = "running".equalsIgnoreCase(state) || "SIMULATED".equalsIgnoreCase(state);
            double[] stats = up ? scheduler.stats(t.getContainerId()) : new double[]{0, 0};
            t.setHealth(up ? 1 : 0);
            t.setCpuPercent(stats[0]);
            t.setMemUsedMb(stats[1]);
            t.setLastCheckAt(LocalDateTime.now());
            if (!up) {
                t.setStatus(TargetStatus.ERROR.name());
            } else {
                healthy++;
            }
            t.setUpdatedAt(LocalDateTime.now());
            targetMapper.updateById(t);
        }
        return healthy;
    }

    /** 判断某队某题靶机是否在线（供 SLA 计分使用）。 */
    public boolean isHealthy(Long contestId, Long teamId, Long questionId) {
        AwdTarget t = targetMapper.selectOne(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId)
                .eq(AwdTarget::getTeamId, teamId)
                .eq(AwdTarget::getQuestionId, questionId)
                .last("LIMIT 1"));
        return t != null && TargetStatus.RUNNING.name().equals(t.getStatus())
                && (t.getHealth() == null || t.getHealth() == 1);
    }

    /** 战队当前可用靶机列表（参赛端只读）。 */
    public List<TargetVO> teamTargets(Long contestId, Long teamId) {
        return toVOs(contestId, findByTeam(contestId, teamId));
    }

    private List<TargetVO> toVOs(Long contestId, List<AwdTarget> rows) {
        if (rows == null || rows.isEmpty()) return List.of();
        Map<Long, String> teams = nameResolver.teamNames(contestId,
                rows.stream().map(AwdTarget::getTeamId).toList());
        Map<Long, String> titles = nameResolver.questionTitles(
                rows.stream().map(AwdTarget::getQuestionId).toList());
        return rows.stream()
                .filter(t -> teams.containsKey(t.getTeamId()))
                .map(t -> toVO(t, teams.get(t.getTeamId()), titles.get(t.getQuestionId())))
                .toList();
    }

    // ------------------------------------------------------------------ 内部

    private String containerName(AwdTarget t) {
        return "csp-c" + t.getContestId() + "-t" + t.getTeamId() + "-q" + t.getQuestionId();
    }

    /** 取题目配置的靶场镜像；未配置时回退到基础 Web 靶场镜像。 */
    private String defaultImage(Long questionId) {
        if (questionId != null) {
            try {
                String img = jdbcTemplate.query(
                        "SELECT image_name FROM question WHERE id = ? AND deleted = 0",
                        rs -> rs.next() ? rs.getString(1) : null, questionId);
                if (img != null && !img.isBlank()) return img;
            } catch (Exception e) {
                log.warn("读取题目镜像失败 questionId={}: {}", questionId, e.getMessage());
            }
        }
        return "csp/arena-basic-web:1.0";
    }

    /** 拼装可访问地址；未就绪时返回 null。 */
    /** 对外访问地址：未配置 public-host 时回退 127.0.0.1，避免拼出 http://:port。 */
    private String hostOrLocal() {
        return publicHost == null || publicHost.isBlank() ? "127.0.0.1" : publicHost.trim();
    }

    private String accessUrlOf(AwdTarget t) {
        if (t.getPort() == null || t.getPort() <= 0) return null;
        if (!TargetStatus.RUNNING.name().equals(t.getStatus())) return null;
        return "http://" + hostOrLocal() + ":" + t.getPort();
    }

    private TargetVO toVO(AwdTarget t) {
        Map<Long, String> teams = t.getTeamId() == null ? Map.of()
                : nameResolver.teamNames(t.getContestId(), List.of(t.getTeamId()));
        Map<Long, String> titles = t.getQuestionId() == null ? Map.of()
                : nameResolver.questionTitles(List.of(t.getQuestionId()));
        return toVO(t, teams.get(t.getTeamId()), titles.get(t.getQuestionId()));
    }

    private TargetVO toVO(AwdTarget t, String teamName, String questionTitle) {
        return TargetVO.builder()
                .id(t.getId())
                .contestId(t.getContestId())
                .teamId(t.getTeamId())
                .teamName(teamName)
                .questionId(t.getQuestionId())
                .questionTitle(questionTitle)
                .ip(t.getIp())
                .port(t.getPort())
                .sshPort(t.getSshPort())
                .sshUser(t.getSshUser())
                .sshPassword(t.getSshPassword())
                .sshCommand(t.getSshPort() != null && t.getSshPort() > 0
                        ? "ssh " + (t.getSshUser() == null ? "root" : t.getSshUser())
                          + "@" + hostOrLocal() + " -p " + t.getSshPort()
                        : null)
                .serviceOk(t.getServiceOk())
                .lastCheckMsg(t.getLastCheckMsg())
                .status(t.getStatus())
                .health(t.getHealth())
                .cpuPercent(t.getCpuPercent())
                .memUsedMb(t.getMemUsedMb())
                .lastCheckAt(t.getLastCheckAt())
                .accessUrl(accessUrlOf(t))
                .build();
    }

    /** 初始化靶机 SSH：设置 root 密码并拉起 sshd。镜像未内置 sshd 时返回 false。 */
    private boolean initSsh(String containerId, String user, String password) {
        try {
            String script = "echo '" + user + ":" + password + "' | chpasswd 2>/dev/null; "
                    + "(service ssh start || service sshd start || /usr/sbin/sshd) >/dev/null 2>&1; "
                    + "sleep 1; "
                    + "(ss -lnt 2>/dev/null || netstat -lnt 2>/dev/null) | grep -q ':22 '";
            TargetScheduler.ExecResult r = scheduler.exec(containerId, new String[]{"sh", "-c", script}, 20);
            boolean ok = r != null && r.getExitCode() == 0;
            if (!ok) {
                log.warn("靶机 SSH 未就绪（镜像可能未内置 sshd） container={}", containerId);
            }
            return ok;
        } catch (Exception e) {
            log.warn("靶机 SSH 初始化失败 container={}: {}", containerId, e.getMessage());
            return false;
        }
    }

    /** 把新 flag 写入容器内 /flag 与 /flag.txt，成功返回 true。 */
    private boolean writeFlagToContainer(String containerId, String flag) {
        if (containerId == null || containerId.isBlank() || containerId.startsWith("sim-")) {
            return false;
        }
        try {
            String script = "printf '%s\\n' '" + flag + "' > /flag 2>/dev/null; "
                    + "printf '%s\\n' '" + flag + "' > /flag.txt 2>/dev/null; "
                    + "chmod 444 /flag /flag.txt 2>/dev/null; "
                    + "grep -q . /flag 2>/dev/null || grep -q . /flag.txt 2>/dev/null";
            TargetScheduler.ExecResult r = scheduler.exec(containerId, new String[]{"sh", "-c", script}, 10);
            return r != null && r.getExitCode() == 0;
        } catch (Exception e) {
            log.warn("flag 写入容器失败 container={}: {}", containerId, e.getMessage());
            return false;
        }
    }
}