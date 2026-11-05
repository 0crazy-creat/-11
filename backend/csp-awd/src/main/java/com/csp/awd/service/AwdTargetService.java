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
        return targetMapper.selectList(q).stream().map(this::toVO).toList();
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
     * 为整个竞赛编排靶机（幂等）：已 RUNNING 的保持不变，缺失的按「参赛战队 × 赛事题目」笛卡尔积补齐。
     *
     * <p>数据来源通过 {@link JdbcTemplate} 直读 {@code contest_team} / {@code contest_question}，
     * 不依赖赛事模块的 Service，避免 csp-awd → csp-ctf 的循环依赖。</p>
     *
     * <p>历史实现只遍历已有 awd_target 行做自愈，首次开赛（表为空）时恒返回 0，
     * 导致前端「一键部署全部靶机」按钮点了没有任何效果。</p>
     */
    public int provisionForContest(Long contestId) {
        List<Long> teamIds;
        try {
            teamIds = jdbcTemplate.queryForList(
                    "SELECT id FROM contest_team WHERE contest_id = ? AND deleted = 0 AND status = 'APPROVED'",
                    Long.class, contestId);
        } catch (Exception e) {
            log.warn("查询参赛战队失败 contest={}: {}", contestId, e.getMessage());
            teamIds = List.of();
        }

        List<Long> questionIds;
        try {
            questionIds = jdbcTemplate.queryForList(
                    "SELECT question_id FROM contest_question WHERE contest_id = ? AND deleted = 0",
                    Long.class, contestId);
        } catch (Exception e) {
            log.warn("查询赛事题目失败 contest={}: {}", contestId, e.getMessage());
            questionIds = List.of();
        }

        if (teamIds.isEmpty() || questionIds.isEmpty()) {
            // 没有任何参赛战队或题目时无事可做，保持与旧行为一致返回 0
            log.info("靶场编排跳过 contest={}：战队 {} 支，题目 {} 道",
                    contestId, teamIds.size(), questionIds.size());
            return 0;
        }

        int provisioned = 0;
        for (Long teamId : teamIds) {
            for (Long questionId : questionIds) {
                try {
                    provisionOne(contestId, teamId, questionId, null, null, null);
                    provisioned++;
                } catch (Exception e) {
                    log.warn("靶机编排失败 contest={} team={} q={}: {}",
                            contestId, teamId, questionId, e.getMessage());
                }
            }
        }
        log.info("靶场编排完成 contest={} 战队 {} 支 × 题目 {} 道，成功 {} 台",
                contestId, teamIds.size(), questionIds.size(), provisioned);
        return provisioned;
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
                .env(envFinal)
                .cpuLimit("1.0")
                .memoryLimit("512m")
                .internalOnly(true)
                .build();

        TargetInstance inst = scheduler.create(spec);

        AwdTarget t = existing == null ? new AwdTarget() : existing;
        t.setContestId(contestId);
        t.setTeamId(teamId);
        t.setQuestionId(questionId);
        t.setContainerId(inst.getContainerId());
        t.setIp(inst.getIp());
        t.setPort(inst.getPort());
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
                .env(env)
                .build();
        TargetInstance inst = scheduler.reset(spec);
        t.setContainerId(inst.getContainerId());
        t.setIp(inst.getIp());
        t.setPort(inst.getPort());
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

                // 2) 重启容器并注入新 flag
                Map<String, String> env = new HashMap<>();
                env.put("FLAG", newFlag);
                TargetSpec spec = TargetSpec.builder()
                        .contestId(contestId)
                        .teamId(t.getTeamId())
                        .questionId(t.getQuestionId())
                        .image(defaultImage(t.getQuestionId()))
                        .containerPort(80)
                        .env(env)
                        .build();
                TargetInstance inst = scheduler.reset(spec);
                t.setContainerId(inst.getContainerId());
                t.setIp(inst.getIp());
                t.setPort(inst.getPort());
                t.setStatus(TargetStatus.RUNNING.name());
                t.setHealth(1);
                t.setLastCheckAt(LocalDateTime.now());
                t.setUpdatedAt(LocalDateTime.now());
                targetMapper.updateById(t);
                n++;
                log.info("轮次 flag 已轮换 contest={} round={} team={} q={} port={}",
                        contestId, roundNo, t.getTeamId(), t.getQuestionId(), inst.getPort());
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
        return findByTeam(contestId, teamId).stream().map(this::toVO).toList();
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
    private String accessUrlOf(AwdTarget t) {
        if (t.getPort() == null || t.getPort() <= 0) return null;
        if (!TargetStatus.RUNNING.name().equals(t.getStatus())) return null;
        return "http://" + publicHost + ":" + t.getPort();
    }

    private TargetVO toVO(AwdTarget t) {
        return TargetVO.builder()
                .id(t.getId())
                .contestId(t.getContestId())
                .teamId(t.getTeamId())
                .questionId(t.getQuestionId())
                .ip(t.getIp())
                .port(t.getPort())
                .status(t.getStatus())
                .health(t.getHealth())
                .cpuPercent(t.getCpuPercent())
                .memUsedMb(t.getMemUsedMb())
                .lastCheckAt(t.getLastCheckAt())
                .accessUrl(accessUrlOf(t))
                .build();
    }
}