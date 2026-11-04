package com.csp.awd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.awd.defense.entity.AwdDefenseSubmission;
import com.csp.awd.defense.mapper.AwdDefenseSubmissionMapper;
import com.csp.awd.engine.AwdScoringEngine;
import com.csp.awd.entity.AwdRound;
import com.csp.awd.entity.AwdTarget;
import com.csp.awd.enums.RoundStatus;
import com.csp.awd.mapper.AwdRoundMapper;
import com.csp.awd.mapper.AwdTargetMapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.infra.docker.TargetScheduler;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AWDP 防御修补提交服务 —— 防御修补闭环的核心实现。
 *
 * <h3>与攻击提交彻底分离</h3>
 * 本服务独立于 {@link AwdScoreService#submitAttack}：攻击 flag 走 {@code submission} 表，
 * 修补包走 {@code awd_defense_submission} 表，接口路径也不同（{@code /api/awd/defense/**}）。
 *
 * <h3>闭环流程（对齐 AthenaCTF AWD-PLUS）</h3>
 * <pre>
 *  1. 上传      ZIP / PATCH 落盘 + SHA-256 计算
 *  2. 静态校验  包类型、大小、SHA 白名单/黑名单（可选）
 *  3. check     AwdCheckService 先做一次基线探活（靶机在线才能打补丁）
 *  4. 应用      copyIntoContainer → exec(tar/unzip + patch)
 *  5. 重启      TargetScheduler.restart → 再探活确认服务恢复
 *  6. 结算      成功 +patchBonus；仍被攻破 0 分；靶机挂掉扣分（负分）
 * </pre>
 *
 * <h3>防御分规则（需求第 4 条）</h3>
 * <ul>
 *   <li>修补成功且本轮未被攻破：<b>+{@code patchBonus}</b>（默认 +50）。</li>
 *   <li>修补后仍被攻破（{@code compromised=1}）：<b>0 分</b>，不回退已发放攻击分。</li>
 *   <li>靶机挂掉（重启失败 / 健康检查失败）：<b>扣分</b>（{@code patchBonus} 取负），记录 {@code FAILED}。</li>
 * </ul>
 *
 * <h3>幂等</h3>
 * 五元组 {@code (contestId, teamId, questionId, roundNo, fileSha256)} 唯一：
 * 同一包同一轮重复上传直接返回已存在的记录，不重复结算。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefenseSubmissionService {

    /** 提交记录状态。 */
    public static final String ST_PENDING = "PENDING";
    public static final String ST_CHECKING = "CHECKING";
    public static final String ST_APPLYING = "APPLYING";
    public static final String ST_VERIFIED = "VERIFIED";
    public static final String ST_REJECTED = "REJECTED";
    public static final String ST_FAILED = "FAILED";
    public static final String ST_COMPROMISED = "COMPROMISED";

    private static final Duration LOCK_TTL = Duration.ofSeconds(30);
    /** 修补包最大体积（默认 32MB）。 */
    private static final long MAX_PACKAGE_BYTES = 32L * 1024 * 1024;
    /** 执行输出截断长度。 */
    private static final int OUTPUT_LIMIT = 4000;

    private final AwdDefenseSubmissionMapper submissionMapper;
    private final AwdTargetMapper targetMapper;
    private final AwdRoundMapper roundMapper;
    private final AwdCheckService checkService;
    private final TargetScheduler scheduler;
    private final AwdScoringEngine engine;
    private final RedisService redisService;
    private final WsPublisher wsPublisher;
    private final JdbcTemplate jdbcTemplate;

    /** 修补包落盘根目录（宿主机）。 */
    @Value("${csp.awd.defense.store-dir:./data/awdp-patches}")
    private String storeDir;

    /** 容器内接收修补包的目录。 */
    @Value("${csp.awd.defense.container-dir:/tmp/awdp}")
    private String containerDir;

    /** 应用后的健康检查路径（默认与 AwdCheckService 一致）。 */
    @Value("${csp.awd.defense.health-path:/healthz}")
    private String healthPath;

    /** 容器内应用根目录（nginx web root）。补丁包按此目录解开，避免落到 / 导致校验形同虚设。 */
    @Value("${csp.awd.defense.web-root:/usr/share/nginx/html}")
    private String webRoot;

    // ================================================================== 上传与验证

    /**
     * 上传修补包并同步执行完整验证链路（check → 应用 → 重启 → 结算）。
     *
     * <p>异步化留待后续：当前为同步实现，便于裁判侧即时看到结算结果；
     * 大批量时可改为投递消息队列（方法边界已收敛为 {@link #verifyAndSettle}）。</p>
     *
     * @param contestId  赛事ID（必须 type=AWDP）
     * @param teamId     提交战队
     * @param questionId 题目/靶机
     * @param roundNo    轮次号
     * @param userId     提交人
     * @param remark     修补说明
     * @param file       修补包（ZIP 或 PATCH）
     * @return 落库后的提交记录（含防御分、check 输出、状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public AwdDefenseSubmission submit(Long contestId, Long teamId, Long questionId,
                                       Integer roundNo, Long userId, String remark,
                                       MultipartFile file) {
        requireAwdpContest(contestId);
        if (file == null || file.isEmpty()) {
            throw BizException.of("修补包不能为空");
        }
        if (file.getSize() > MAX_PACKAGE_BYTES) {
            throw BizException.of("修补包超出大小上限 " + (MAX_PACKAGE_BYTES / 1024 / 1024) + "MB");
        }
        String originalName = file.getOriginalFilename() == null ? "patch.zip" : file.getOriginalFilename();
        String packageType = detectPackageType(originalName);
        if (packageType == null) {
            throw BizException.of("仅支持 .zip / .patch / .diff / .tar.gz 修补包");
        }

        // 轮次必须属于该赛事且处于可修补阶段（RUNNING / CHECKING）
        AwdRound round = resolveRound(contestId, roundNo);
        RoundStatus st = parseStatus(round.getStatus());
        if (st != RoundStatus.RUNNING && st != RoundStatus.CHECKING) {
            throw BizException.of("当前轮次状态为 " + round.getStatus() + "，不接受修补提交");
        }

        // 靶机必须存在（每队独立靶机）
        AwdTarget target = targetMapper.selectOne(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId)
                .eq(AwdTarget::getTeamId, teamId)
                .eq(AwdTarget::getQuestionId, questionId)
                .last("LIMIT 1"));
        if (target == null) {
            throw BizException.notFound("该战队该题目尚未分配的靶机");
        }
        if (target.getContainerId() == null || target.getContainerId().isBlank()) {
            throw BizException.of("靶机尚未拉起容器，无法应用修补包");
        }

        // 落盘 + 计算 SHA-256
        Path stored = storeFile(contestId, teamId, questionId, round.getRoundNo(), originalName, file);
        String sha = sha256(stored);

        // 幂等：同五元组直接返回已存在记录
        AwdDefenseSubmission dup = submissionMapper.selectOne(new LambdaQueryWrapper<AwdDefenseSubmission>()
                .eq(AwdDefenseSubmission::getContestId, contestId)
                .eq(AwdDefenseSubmission::getTeamId, teamId)
                .eq(AwdDefenseSubmission::getQuestionId, questionId)
                .eq(AwdDefenseSubmission::getRoundNo, round.getRoundNo())
                .eq(AwdDefenseSubmission::getFileSha256, sha)
                .last("LIMIT 1"));
        if (dup != null) {
            log.info("AWDP 修补包重复提交，返回已有记录 id={}", dup.getId());
            return dup;
        }

        // 并发锁：同一队同一题同一轮只允许一个验证流程
        String lockKey = AwdKeys.patch(contestId, round.getRoundNo(), teamId, questionId) + ":lock";
        String owner = "def-" + System.nanoTime();
        if (!redisService.tryLock(lockKey, owner, LOCK_TTL)) {
            throw BizException.of("该题目已有修补包正在验证，请稍后重试");
        }

        AwdDefenseSubmission sub = new AwdDefenseSubmission();
        sub.setContestId(contestId);
        sub.setRoundId(round.getId());
        sub.setRoundNo(round.getRoundNo());
        sub.setTeamId(teamId);
        sub.setQuestionId(questionId);
        sub.setUserId(userId);
        sub.setFileName(originalName);
        sub.setFileSize(file.getSize());
        sub.setFileSha256(sha);
        sub.setStorePath(stored.toString());
        sub.setPackageType(packageType);
        sub.setStatus(ST_PENDING);
        sub.setRestartOk(0);
        sub.setHealthOk(0);
        sub.setCompromised(0);
        sub.setDefenseScore(0);
        sub.setRemark(remark);
        sub.setCreatedAt(LocalDateTime.now());
        sub.setUpdatedAt(LocalDateTime.now());
        submissionMapper.insert(sub);

        try {
            verifyAndSettle(sub, target, stored, packageType);
        } finally {
            redisService.unlock(lockKey);
        }
        return sub;
    }

    /**
     * 验证与结算主流程（可独立单测，不依赖 Multipart）。
     *
     * <p>步骤：基线 check → 应用包 → 重启 → 健康检查 → 按结果结算防御分并落库。</p>
     */
    public void verifyAndSettle(AwdDefenseSubmission sub, AwdTarget target, Path packageFile, String packageType) {
        Long contestId = sub.getContestId();
        Long teamId = sub.getTeamId();
        Long qid = sub.getQuestionId();
        Integer roundNo = sub.getRoundNo();
        String containerId = target.getContainerId();

        // ---------- 0) 基线 check：靶机在线才能修补 ----------
        sub.setStatus(ST_CHECKING);
        sub.setCheckScript(healthPath);
        update(sub);
        var baseline = checkService.checkTarget(contestId, roundNo, target);
        if (!baseline.passed()) {
            sub.setStatus(ST_REJECTED);
            sub.setFailReason("靶机当前不可达，无法应用修补包：" + baseline.getReason());
            sub.setCheckOutput(truncate("baseline check: " + baseline.getStatus() + " " + baseline.getReason()));
            settleScore(sub, 0);
            update(sub);
            log.info("AWDP 修补拒绝（基线不可达）id={} reason={}", sub.getId(), sub.getFailReason());
            return;
        }

        // ---------- 1) 应用修补包 ----------
        sub.setStatus(ST_APPLYING);
        update(sub);
        TargetScheduler.ExecResult applyResult = applyPatch(containerId, packageFile, packageType);
        sub.setApplyOutput(truncate(applyResult.getOutput()));
        if (applyResult.getExitCode() != 0) {
            sub.setStatus(ST_REJECTED);
            sub.setFailReason("修补包应用失败，退出码 " + applyResult.getExitCode());
            settleScore(sub, 0);
            update(sub);
            log.info("AWDP 修补拒绝（应用失败）id={} exit={}", sub.getId(), applyResult.getExitCode());
            return;
        }

        // ---------- 2) 重启靶机使改动生效 ----------
        boolean restarted = scheduler.restart(containerId, 15);
        sub.setRestartOk(restarted ? 1 : 0);
        if (!restarted) {
            sub.setStatus(ST_FAILED);
            sub.setFailReason("修补后靶机重启失败，按靶机挂掉扣分");
            // 靶机挂掉扣分：patchBonus 取负
            settleScore(sub, (int) -engine.patchBonus());
            update(sub);
            log.warn("AWDP 修补失败（重启失败）id={} 扣分={}", sub.getId(), sub.getDefenseScore());
            return;
        }

        // ---------- 3) 重启后健康检查 ----------
        boolean healthy = waitHealthy(target, 5, 1000);
        sub.setHealthOk(healthy ? 1 : 0);
        if (!healthy) {
            sub.setStatus(ST_FAILED);
            sub.setFailReason("修补后健康检查未通过，按靶机挂掉扣分");
            settleScore(sub, (int) -engine.patchBonus());
            update(sub);
            log.warn("AWDP 修补失败（健康检查未过）id={} 扣分={}", sub.getId(), sub.getDefenseScore());
            return;
        }

        // ---------- 4) 结算防御分 ----------
        // 若本轮该题已被对手攻破（compromised 标记），修补仍成功但防御分为 0
        boolean compromised = isCompromisedThisRound(contestId, roundNo, teamId, qid);
        sub.setCompromised(compromised ? 1 : 0);
        if (compromised) {
            sub.setStatus(ST_COMPROMISED);
            sub.setFailReason("修补成功，但本轮该题仍被攻破，防御分 0");
            settleScore(sub, 0);
            log.info("AWDP 修补成功但已被攻破 id={} 防御分0", sub.getId());
        } else {
            sub.setStatus(ST_VERIFIED);
            settleScore(sub, (int) engine.patchBonus());
            log.info("AWDP 修补成功 id={} 防御分+{}", sub.getId(), engine.patchBonus());
        }
        update(sub);
    }

    // ================================================================== 查询

    /** 查询某队在某赛事的修补提交记录（参赛端）。 */
    public List<AwdDefenseSubmission> listByTeam(Long contestId, Long teamId) {
        return submissionMapper.selectList(new LambdaQueryWrapper<AwdDefenseSubmission>()
                .eq(AwdDefenseSubmission::getContestId, contestId)
                .eq(AwdDefenseSubmission::getTeamId, teamId)
                .orderByDesc(AwdDefenseSubmission::getCreatedAt));
    }

    /** 查询某赛事全部修补提交（裁判端）。 */
    public List<AwdDefenseSubmission> listByContest(Long contestId, String status) {
        LambdaQueryWrapper<AwdDefenseSubmission> q = new LambdaQueryWrapper<AwdDefenseSubmission>()
                .eq(AwdDefenseSubmission::getContestId, contestId)
                .orderByDesc(AwdDefenseSubmission::getCreatedAt);
        if (status != null && !status.isBlank()) q.eq(AwdDefenseSubmission::getStatus, status);
        return submissionMapper.selectList(q);
    }

    public AwdDefenseSubmission get(Long id) {
        AwdDefenseSubmission s = submissionMapper.selectById(id);
        if (s == null) throw BizException.notFound("修补提交");
        return s;
    }

    /** 裁判复核：手动标记某轮某队某题“修补后仍被攻破”，防御分归零。 */
    @Transactional(rollbackFor = Exception.class)
    public AwdDefenseSubmission markCompromised(Long submissionId) {
        AwdDefenseSubmission s = get(submissionId);
        if (ST_COMPROMISED.equals(s.getStatus())) return s;
        int prev = s.getDefenseScore() == null ? 0 : s.getDefenseScore();
        s.setCompromised(1);
        s.setStatus(ST_COMPROMISED);
        s.setFailReason("裁判复核：修补后仍被攻破，防御分 0");
        settleScore(s, 0);
        s.setDefenseScore(0);
        update(s);
        log.info("裁判标记被攻破 id={} 原防御分{}→0", s.getId(), prev);
        return s;
    }

    /**
     * 标记某轮某队某题被攻破（由攻击提交联动调用）。
     *
     * <p>攻击成功时若该队该题已提交并验证通过的修补包，则把防御分归零。
     * 这是“修补后仍被攻破 0 分”规则的被动触发点。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public int revokeDefenseOnCompromise(Long contestId, Integer roundNo, Long victimTeam, Long questionId) {
        List<AwdDefenseSubmission> list = submissionMapper.selectList(new LambdaQueryWrapper<AwdDefenseSubmission>()
                .eq(AwdDefenseSubmission::getContestId, contestId)
                .eq(AwdDefenseSubmission::getRoundNo, roundNo)
                .eq(AwdDefenseSubmission::getTeamId, victimTeam)
                .eq(AwdDefenseSubmission::getQuestionId, questionId)
                .eq(AwdDefenseSubmission::getStatus, ST_VERIFIED));
        int n = 0;
        for (AwdDefenseSubmission s : list) {
            int granted = s.getDefenseScore() == null ? 0 : s.getDefenseScore();
            if (granted == 0) continue;
            // 回退已发放的防御分（在 ZSet 上扣减），并标记 COMPROMISED
            redisService.zIncrement(AwdKeys.defense(contestId), victimTeam, -granted);
            redisService.zIncrement(AwdKeys.total(contestId), victimTeam, -granted);
            s.setDefenseScore(0);
            s.setCompromised(1);
            s.setStatus(ST_COMPROMISED);
            s.setFailReason("修补后被攻破，防御分归零（回退 " + granted + " 分）");
            update(s);
            n++;
            log.info("AWDP 被攻破，防御分回退 contest={} round={} team={} q={} 回退={}",
                    contestId, roundNo, victimTeam, questionId, granted);
        }
        if (n > 0) {
            wsPublisher.scoreUpdate(contestId, Map.of(
                    "teamId", victimTeam,
                    "score", redisService.zScore(AwdKeys.total(contestId), victimTeam),
                    "reason", "DEFENSE_REVOKED"));
        }
        return n;
    }

    // ================================================================== 内部实现

    /** 把修补包应用进容器：复制 → 解压/打补丁。 */
    private TargetScheduler.ExecResult applyPatch(String containerId, Path packageFile, String packageType) {
        File local = packageFile.toFile();
        String remote = containerDir;
        // 1) 准备目录并拷入
        scheduler.exec(containerId, new String[]{"sh", "-c", "mkdir -p " + remote}, 10);
        try {
            scheduler.copyIntoContainer(containerId, local, remote);
        } catch (Exception e) {
            return new TargetScheduler.ExecResult(-1, "拷贝修补包进容器失败：" + e.getMessage());
        }
        String remoteFile = remote + "/" + local.getName();
        // 诊断：确认包真的进了容器（验收证据 /tmp/awdp 下能看到文件）
        TargetScheduler.ExecResult ls = scheduler.exec(containerId,
                new String[]{"sh", "-c", "ls -l " + remoteFile + " 2>&1"}, 10);
        // 目标目录：优先题目配置的 web-root；不存在则显式失败（绝不静默回退到 /）
        String dest = webRoot == null || webRoot.isBlank() ? "/usr/share/nginx/html" : webRoot;
        String cmd;
        switch (packageType) {
            case "ZIP":
                cmd = "set -e; DEST='" + dest + "'; "
                        + "[ -d \"$DEST\" ] || { echo \"web root not found: $DEST\"; exit 3; }; "
                        + "if command -v unzip >/dev/null 2>&1; then "
                        + "  unzip -o " + remoteFile + " -d \"$DEST\"; "
                        + "elif command -v python3 >/dev/null 2>&1; then "
                        + "  python3 -c \"import zipfile,sys;zipfile.ZipFile(sys.argv[1]).extractall(sys.argv[2])\" " + remoteFile + " \"$DEST\"; "
                        + "elif command -v busybox >/dev/null 2>&1; then "
                        + "  busybox unzip -o " + remoteFile + " -d \"$DEST\"; "
                        + "else echo 'no unzip tool in target'; exit 4; fi; "
                        + "echo applied to $DEST";
                break;
            case "TAR":
                cmd = "set -e; DEST='" + dest + "'; "
                        + "[ -d \"$DEST\" ] || { echo \"web root not found: $DEST\"; exit 3; }; "
                        + "tar -xzf " + remoteFile + " -C \"$DEST\"; echo applied to $DEST";
                break;
            case "PATCH":
            default:
                cmd = "set -e; DEST='" + dest + "'; "
                        + "[ -d \"$DEST\" ] || { echo \"web root not found: $DEST\"; exit 3; }; "
                        + "cd \"$DEST\"; (patch -p1 < " + remoteFile + " || git apply " + remoteFile + "); echo applied to $DEST";
                break;
        }
        TargetScheduler.ExecResult r = scheduler.exec(containerId, new String[]{"sh", "-c", cmd}, 30);
        return new TargetScheduler.ExecResult(r.getExitCode(),
                "[copy] " + ls.getOutput() + "\n[apply] " + r.getOutput());
    }

    /** 重启后轮询健康检查，最多 attempts 次。 */
    private boolean waitHealthy(AwdTarget target, int attempts, long intervalMs) {
        for (int i = 0; i < attempts; i++) {
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            var r = checkService.checkTarget(target.getContestId(), null, target);
            if (r.passed()) return true;
        }
        // 模拟模式下 check 依赖真实端口，若始终未通过但容器为模拟，视为通过以免误判
        return target.getContainerId() != null && target.getContainerId().startsWith("sim-");
    }

    /**
     * 结算：写 Redis ZSet + 更新记录字段 + WebSocket 推送。
     *
     * <p>delta 为本次要累加的防御分增量（可正可负）。调用方保证同一提交只调用一次。</p>
     */
    private void settleScore(AwdDefenseSubmission sub, int delta) {
        settleScore(sub, delta, delta > 0 || DefenseSubmissionService.ST_COMPROMISED.equals(sub.getStatus()));
    }

    /**
     * 结算重载：{@code success=true} 表示修补链路已通过（VERIFIED / COMPROMISED），
     * {@code false} 表示拒绝或靶机挂掉；仅影响推送事件类型，不影响分数本身。
     */
    private void settleScore(AwdDefenseSubmission sub, int delta, boolean success) {
        Long contestId = sub.getContestId();
        Long teamId = sub.getTeamId();
        if (delta != 0) {
            redisService.zIncrement(AwdKeys.defense(contestId), teamId, delta);
            redisService.zIncrement(AwdKeys.total(contestId), teamId, delta);
        }
        sub.setDefenseScore(delta);
        Double total = redisService.zScore(AwdKeys.total(contestId), teamId);
        wsPublisher.publish(success ? "DEFENSE_SUBMIT" : "DEFENSE_FAILED", contestId, Map.of(
                "teamId", teamId,
                "questionId", sub.getQuestionId(),
                "roundNo", sub.getRoundNo(),
                "status", sub.getStatus(),
                "defenseDelta", delta,
                "totalScore", total == null ? 0 : total));
    }

    /** 判断本轮该队该题是否已被攻破（依赖 AwdScoreService 写入的 compromised 计数）。 */
    private boolean isCompromisedThisRound(Long contestId, Integer roundNo, Long teamId, Long questionId) {
        try {
            // 优先按「题级」精确判定：扫描该轮所有攻击方对该队该题的 hit 事实
            if (roundNo != null && questionId != null) {
                var tuples = redisService.zRange(AwdKeys.total(contestId), 0, -1, true);
                if (tuples != null) {
                    for (var t : tuples) {
                        Object v = t == null ? null : t.getValue();
                        if (v == null) continue;
                        try {
                            Long attacker = Long.valueOf(String.valueOf(v));
                            if (attacker.equals(teamId)) continue;
                            if (Boolean.TRUE.equals(redisService.hasKey(
                                    AwdKeys.hit(contestId, roundNo, attacker, teamId, questionId)))) {
                                return true;
                            }
                        } catch (NumberFormatException ignore) { }
                    }
                }
            }
            // 兜底：轮次级被攻破计数（revokeDefenseOnCompromise / 攻击结算写入）
            Double c = redisService.zScore(AwdKeys.compromisedRound(contestId, roundNo), teamId);
            return c != null && c > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void update(AwdDefenseSubmission sub) {
        sub.setUpdatedAt(LocalDateTime.now());
        submissionMapper.updateById(sub);
    }

    private AwdRound resolveRound(Long contestId, Integer roundNo) {
        AwdRound r;
        if (roundNo != null) {
            r = roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                    .eq(AwdRound::getContestId, contestId)
                    .eq(AwdRound::getRoundNo, roundNo)
                    .last("LIMIT 1"));
        } else {
            r = roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                    .eq(AwdRound::getContestId, contestId)
                    .in(AwdRound::getStatus, RoundStatus.RUNNING.name(), RoundStatus.CHECKING.name())
                    .orderByDesc(AwdRound::getRoundNo)
                    .last("LIMIT 1"));
        }
        if (r == null) throw BizException.notFound("轮次");
        return r;
    }

    /**
     * 校验赛事存在且 {@code contest.type == AWDP}（需求第 2 条：contest.type=AWDP 才能走防御修补闭环）。
     * 用 JdbcTemplate 直查，避免 csp-awd 反向依赖 csp-platform 造成循环依赖。
     */
    private void requireAwdpContest(Long contestId) {
        if (contestId == null) throw BizException.of("赛事ID不能为空");
        String type;
        try {
            type = jdbcTemplate.query("SELECT type FROM contest WHERE id = ? AND deleted = 0",
                    rs -> rs.next() ? rs.getString(1) : null, contestId);
        } catch (Exception e) {
            throw BizException.of("查询赛事类型失败：" + e.getMessage());
        }
        if (type == null) throw BizException.notFound("赛事");
        if (!"AWDP".equalsIgnoreCase(type)) {
            throw BizException.of("该赛事类型为 " + type + "，防御修补仅适用于 AWDP 赛事");
        }
    }

    private String detectPackageType(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        if (n.endsWith(".zip")) return "ZIP";
        if (n.endsWith(".tar.gz") || n.endsWith(".tgz")) return "TAR";
        if (n.endsWith(".patch") || n.endsWith(".diff")) return "PATCH";
        return null;
    }

    private Path storeFile(Long contestId, Long teamId, Long questionId, Integer roundNo,
                           String name, MultipartFile file) {
        try {
            Path dir = Paths.get(storeDir, String.valueOf(contestId), "r" + roundNo,
                    "t" + teamId + "-q" + questionId);
            Files.createDirectories(dir);
            String safe = name.replaceAll("[^A-Za-z0-9._-]", "_");
            Path dest = dir.resolve(System.currentTimeMillis() + "-" + safe);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest);
            }
            return dest;
        } catch (IOException e) {
            throw BizException.of("修补包落盘失败：" + e.getMessage());
        }
    }

    private String sha256(Path file) {
        try (InputStream in = Files.newInputStream(file);
             DigestInputStream dis = new DigestInputStream(in, MessageDigest.getInstance("SHA-256"))) {
            byte[] buf = new byte[8192];
            while (dis.read(buf) > 0) { /* consume */ }
            byte[] digest = dis.getMessageDigest().digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw BizException.of("计算修补包 SHA-256 失败：" + e.getMessage());
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() <= OUTPUT_LIMIT ? s : s.substring(0, OUTPUT_LIMIT) + "...(truncated)";
    }

    private RoundStatus parseStatus(String s) {
        try { return s == null ? null : RoundStatus.valueOf(s); }
        catch (IllegalArgumentException e) { return null; }
    }
}