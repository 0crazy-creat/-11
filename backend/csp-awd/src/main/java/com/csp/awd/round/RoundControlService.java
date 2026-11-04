package com.csp.awd.round;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.awd.entity.AwdRound;
import com.csp.awd.mapper.AwdRoundMapper;
import com.csp.awd.round.entity.AwdRoundAudit;
import com.csp.awd.round.mapper.AwdRoundAuditMapper;
import com.csp.awd.service.AwdKeys;
import com.csp.awd.service.AwdScoreService;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 轮次控制面服务：begin / running / settle 三个状态迁移接口的实现。
 *
 * <p>与 {@code AwdRoundService} 的分工：本服务是 {@code com.csp.awd.round} 包内的控制面入口，
 * 状态权威是 {@link RoundStatus} / {@link RoundStateMachine}（PENDING→RUNNING→SETTLING→FINISHED，
 * 任意非终态可 ABORTED）。它直接对 {@code awd_round} 做「Redis 锁 + status CAS」更新，
 * 不修改也不复用 {@code AwdRoundService} 的迁移方法体，避免与定时编排器（t8 负责）双写。</p>
 *
 * <p>并发安全：与 {@code AwdRoundService} 共用同一把锁前缀 {@code csp:awd:round:lock:}，
 * 保证控制面手工操作与定时器自动推进互斥。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoundControlService {

    /** 与 AwdRoundService 共用同一把锁，避免控制面与定时器并发推进同一轮。 */
    private static final String LOCK_PREFIX = "csp:awd:round:lock:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);

    private final AwdRoundMapper roundMapper;
    private final AwdRoundAuditMapper auditMapper;
    private final RedisService redisService;
    private final WsPublisher wsPublisher;
    private final AwdScoreService scoreService;

    // ================================================================== 查询

    /** 读取轮次实体，不存在抛 404。 */
    public AwdRound requireRound(Long contestId, Integer roundNo) {
        AwdRound r = roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                .eq(AwdRound::getContestId, contestId)
                .eq(AwdRound::getRoundNo, roundNo)
                .last("LIMIT 1"));
        if (r == null) {
            throw BizException.notFound("轮次 " + contestId + "#" + roundNo);
        }
        return r;
    }

    /** 读取并解析当前状态（非法值降级为 PENDING 并告警）。 */
    public RoundStatus statusOf(AwdRound r) {
        RoundStatus st = RoundStateMachine.parseOrDefault(r.getStatus(), RoundStatus.PENDING);
        if (st == null) {
            log.warn("轮次状态非法，按 PENDING 处理 contest={} round={} raw={}",
                    r.getContestId(), r.getRoundNo(), r.getStatus());
            return RoundStatus.PENDING;
        }
        return st;
    }

    /** 当前是否被软中断冻结（攻击窗口暂停，提交被拒）。 */
    public boolean isFrozen(Long contestId, Integer roundNo) {
        Object v = redisService.hGet(AwdKeys.roundControl(contestId, roundNo), "frozen");
        return v != null && !"false".equalsIgnoreCase(String.valueOf(v));
    }

    /** 软中断冻结原因（未冻结返回 null）。 */
    public String frozenReason(Long contestId, Integer roundNo) {
        Object v = redisService.hGet(AwdKeys.roundControl(contestId, roundNo), "frozenReason");
        return v == null ? null : String.valueOf(v);
    }

    /** 轮次控制视图（供接口返回）。 */
    public Map<String, Object> view(Long contestId, Integer roundNo) {
        AwdRound r = requireRound(contestId, roundNo);
        RoundStatus st = statusOf(r);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("contestId", r.getContestId());
        m.put("roundNo", r.getRoundNo());
        m.put("status", st.name());
        m.put("terminal", st.isTerminal());
        m.put("attackWindowOpen", st.isAttackWindowOpen());
        m.put("frozen", isFrozen(contestId, roundNo));
        m.put("frozenReason", frozenReason(contestId, roundNo));
        m.put("interruptType", readColumn(r, "interruptType"));
        m.put("interruptReason", readColumn(r, "interruptReason"));
        m.put("startAt", r.getStartAt());
        m.put("endAt", r.getEndAt());
        m.put("durationS", r.getDurationS());
        m.put("settledAt", readColumn(r, "settledAt"));
        m.put("nextStates", RoundStateMachine.nextStates(st).stream().map(Enum::name).toList());
        return m;
    }

    /**
     * 反射读取新增列（实体类保持最小改动面：若字段尚未加入 AwdRound，则返回 null）。
     *
     * <p>说明：{@code AwdRound} 实体归 backend-ctf 的轮次域维护，本任务不修改它；
     * 新增列通过 MyBatis-Plus 的 map 更新 / 反射读取兼容，避免冲突。</p>
     */
    private Object readColumn(AwdRound r, String getterSuffix) {
        try {
            var m = AwdRound.class.getMethod("get" + Character.toUpperCase(getterSuffix.charAt(0))
                    + getterSuffix.substring(1));
            return m.invoke(r);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    // ================================================================== 状态迁移

    /**
     * begin：PENDING → RUNNING，开启攻击窗口。
     *
     * <p>同时清空软中断冻结标记，并推送 ROUND_CHANGE 事件。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> begin(Long contestId, Integer roundNo, Long operatorId, String operator) {
        return transit(contestId, roundNo, RoundStatus.RUNNING, "BEGIN", operatorId, operator, null);
    }

    /**
     * running：查询当前攻击窗口运行态（不迁移状态，供前端轮询）。
     *
     * <p>返回剩余秒数、是否冻结、是否仍接受提交。</p>
     */
    public Map<String, Object> running(Long contestId, Integer roundNo) {
        AwdRound r = requireRound(contestId, roundNo);
        RoundStatus st = statusOf(r);
        long remaining = r.getEndAt() == null ? 0
                : Math.max(0, Duration.between(LocalDateTime.now(), r.getEndAt()).getSeconds());
        boolean frozen = isFrozen(contestId, roundNo);
        boolean accepting = st.isAttackWindowOpen() && !frozen && remaining > 0;

        Map<String, Object> m = view(contestId, roundNo);
        m.put("remainingSeconds", remaining);
        m.put("acceptingSubmission", accepting);
        if (!st.isAttackWindowOpen()) {
            m.put("closedReason", "轮次当前状态为 " + st + "，攻击窗口未开启");
        } else if (frozen) {
            m.put("closedReason", "软中断冻结中：" + frozenReason(contestId, roundNo));
        } else if (remaining <= 0) {
            m.put("closedReason", "攻击窗口已到点");
        }
        return m;
    }

    /**
     * settle：RUNNING（或 PENDING，用于异常收尾）→ SETTLING，执行结算并推进到 FINISHED。
     *
     * <p>结算调用 {@link AwdScoreService#settleRound}（其内部幂等），完成后 CAS 置 FINISHED
     * 并写 settled_at。任一环节失败不整轮崩溃：先落 ABORTED 前记录错误，由调用方重试。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> settle(Long contestId, Integer roundNo, Long operatorId, String operator) {
        String lockKey = LOCK_PREFIX + contestId + ":" + roundNo;
        String owner = lockOwner();
        if (!redisService.tryLock(lockKey, owner, LOCK_TTL)) {
            throw new BizException(ResultCode.CONFLICT, "轮次正在被其他操作推进，请稍后重试");
        }
        try {
            AwdRound r = requireRound(contestId, roundNo);
            RoundStatus from = statusOf(r);
            RoundStateMachine.requireTransition(from, RoundStatus.SETTLING);

            // 1) 落 SETTLING
            casStatus(r, from, RoundStatus.SETTLING, null, null);
            audit(contestId, roundNo, "SETTLING", null, operatorId, operator,
                    "进入结算：" + from + " → SETTLING");

            // 2) 结算（幂等）。单队异常不影响整轮：settleRound 内部按队聚合，异常在此兜底。
            boolean settled = false;
            String settleError = null;
            try {
                scoreService.settleRound(contestId, roundNo);
                settled = true;
            } catch (Exception e) {
                settleError = e.getMessage();
                log.error("轮次结算失败 contest={} round={}", contestId, roundNo, e);
            }

            if (!settled) {
                // 结算失败：回退到 RUNNING 以便重试，而不是让整轮崩溃在 SETTLING。
                casStatus(requireRound(contestId, roundNo), RoundStatus.SETTLING, RoundStatus.RUNNING, null, null);
                throw new BizException(ResultCode.CONFLICT,
                        "结算失败，轮次已回退到 RUNNING 可重试：" + settleError);
            }

            // 3) SETTLING → FINISHED，写 settled_at
            AwdRound cur = requireRound(contestId, roundNo);
            casStatus(cur, RoundStatus.SETTLING, RoundStatus.FINISHED, null, LocalDateTime.now());
            audit(contestId, roundNo, "SETTLE", null, operatorId, operator, "结算完成：" + from + " → FINISHED");
            wsPublisher.roundChange(contestId, Map.of(
                    "contestId", contestId,
                    "roundNo", roundNo,
                    "from", from.name(),
                    "to", RoundStatus.FINISHED.name(),
                    "event", "SETTLED"));

            Map<String, Object> m = view(contestId, roundNo);
            m.put("settled", true);
            return m;
        } finally {
            redisService.unlock(lockKey);
        }
    }

    // ================================================================== 内部工具

    /**
     * 通用控制面迁移：Redis 锁 + 状态机校验 + status CAS 更新 + 审计 + WS 推送。
     *
     * @param interruptType  非空时同时写 interrupt_type（软/硬中断用）
     * @param interruptReason 非空时同时写 interrupt_reason
     */
    Map<String, Object> transit(Long contestId, Integer roundNo, RoundStatus target,
                                String action, Long operatorId, String operator,
                                String reason) {
        String lockKey = LOCK_PREFIX + contestId + ":" + roundNo;
        String owner = lockOwner();
        if (!redisService.tryLock(lockKey, owner, LOCK_TTL)) {
            throw new BizException(ResultCode.CONFLICT, "轮次正在被其他操作推进，请稍后重试");
        }
        try {
            AwdRound r = requireRound(contestId, roundNo);
            RoundStatus from = statusOf(r);
            // 全部控制面接口的统一关卡
            RoundStateMachine.requireTransition(from, target);

            String interruptType = null;
            String interruptReason = null;
            if (target == RoundStatus.ABORTED && reason != null) {
                interruptType = reason.startsWith("SOFT:") ? "SOFT" : "HARD";
                interruptReason = reason;
            }
            casStatus(r, from, target, interruptType, target == RoundStatus.FINISHED ? LocalDateTime.now() : null);
            audit(contestId, roundNo, action, null, operatorId, operator,
                    from + " → " + target + (reason == null ? "" : "（" + reason + "）"));

            if (target == RoundStatus.RUNNING) {
                // begin 成功：清除软中断冻结标记
                redisService.hDelete(AwdKeys.roundControl(contestId, roundNo), "frozen", "frozenReason");
                // begin 时按 durationS 落攻击窗口结束时间；resume 时若窗口未到期则保留原 end_at。
                AwdRound cur0 = requireRound(contestId, roundNo);
                boolean needWindow = cur0.getEndAt() == null
                        || !cur0.getEndAt().isAfter(LocalDateTime.now());
                if (needWindow) {
                    int dur = cur0.getDurationS() == null || cur0.getDurationS() < 30 ? 600 : cur0.getDurationS();
                    var uw0 = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<AwdRound>()
                            .eq("id", cur0.getId())
                            .set("end_at", LocalDateTime.now().plusSeconds(dur));
                    roundMapper.update(null, uw0);
                }
            }
            wsPublisher.roundChange(contestId, Map.of(
                    "contestId", contestId, "roundNo", roundNo,
                    "from", from.name(), "to", target.name(), "action", action));

            Map<String, Object> m = view(contestId, roundNo);
            m.put("from", from.name());
            m.put("to", target.name());
            return m;
        } finally {
            redisService.unlock(lockKey);
        }
    }

    /**
     * status CAS 更新：仅当数据库状态仍为 {@code from} 时更新，防并发穿透。
     *
     * <p>新增列（interrupt_type / interrupt_reason / settled_at）不在实体字段内，
     * 因此使用 MyBatis-Plus 的 {@code UpdateWrapper.set} 显式写列，避免改动实体。</p>
     */
    void casStatus(AwdRound r, RoundStatus from, RoundStatus to,
                   String interruptType, LocalDateTime settledAt) {
        var uw = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<AwdRound>()
                .eq("id", r.getId())
                .eq("status", from.name())
                .set("status", to.name());
        if (interruptType != null) {
            uw.set("interrupt_type", interruptType);
        }
        if (settledAt != null) {
            uw.set("settled_at", settledAt);
        }
        if (to == RoundStatus.ABORTED) {
            uw.set("end_at", LocalDateTime.now());
        }
        int rows = roundMapper.update(null, uw);
        if (rows == 0) {
            throw new BizException(ResultCode.CONFLICT, "轮次状态已被并发修改，请刷新后重试");
        }
    }

    /** 写中断原因列（硬/软中断共用）。 */
    void writeInterruptReason(AwdRound r, String type, String reason) {
        var uw = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<AwdRound>()
                .eq("id", r.getId())
                .set("interrupt_type", type)
                .set("interrupt_reason", reason);
        roundMapper.update(null, uw);
    }

    /** 控制面审计落库（csp-awd 不能依赖平台 AuditService，故独立落库）。 */
    void audit(Long contestId, Integer roundNo, String action, Long targetId,
               Long operatorId, String operator, String detail) {
        try {
            AwdRoundAudit a = new AwdRoundAudit();
            a.setContestId(contestId);
            a.setRoundNo(roundNo);
            a.setAction(action);
            a.setTargetId(targetId);
            a.setOperatorId(operatorId);
            a.setOperator(operator);
            a.setDetail(detail != null && detail.length() > 1000 ? detail.substring(0, 1000) : detail);
            a.setCreateTime(LocalDateTime.now());
            auditMapper.insert(a);
        } catch (Exception e) {
            // 审计失败不阻断主流程，但必须留痕
            log.error("轮次控制审计写入失败 contest={} round={} action={}", contestId, roundNo, action, e);
        }
    }

    private String lockOwner() {
        return "awd-ctl-" + Thread.currentThread().getId() + "-" + System.nanoTime();
    }
}