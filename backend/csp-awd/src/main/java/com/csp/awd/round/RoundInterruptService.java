package com.csp.awd.round;

import com.csp.awd.entity.AwdRound;
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

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 轮次中断服务：软中断 / 硬中断 / 恢复。
 *
 * <h3>软中断（softInterrupt）</h3>
 * <p>语义：<b>暂停本轮攻击窗口但保留已提交成绩</b>。实现方式是不迁移轮次状态
 * （仍是 RUNNING），而是在 Redis 控制标记 {@code csp:awd:round:ctl:{cid}:r{n}} 上置
 * {@code frozen=true}。攻击提交入口通过 {@link RoundStateMachine#requireAttackWindowOpen}
 * 读到 frozen 即拒绝新提交；已完成提交与已计分不受影响。可随时 {@link #resume} 恢复。</p>
 *
 * <h3>硬中断（hardInterrupt）</h3>
 * <p>语义：<b>立即中止本轮</b>。状态机迁到 ABORTED（任意非终态可中止），写
 * {@code interrupt_type=HARD}、{@code interrupt_reason}（含原因与操作人），
 * 随后尽力做一次结算（{@code settleRound} 内部幂等），保证已产生的攻击分不丢失，
 * 但结算失败不阻断中止——中止的优先级高于结算。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoundInterruptService {

    public static final String TYPE_SOFT = "SOFT";
    public static final String TYPE_HARD = "HARD";

    private final RoundControlService controlService;
    private final RedisService redisService;
    private final WsPublisher wsPublisher;
    private final AwdScoreService scoreService;

    // ================================================================== 软中断

    /**
     * 软中断：暂停攻击窗口，保留已提交成绩。
     *
     * @param contestId  竞赛
     * @param roundNo    轮次
     * @param reason     中断原因（必填，便于赛后复盘）
     * @param operatorId 操作人用户ID
     * @param operator   操作人用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> softInterrupt(Long contestId, Integer roundNo, String reason,
                                             Long operatorId, String operator) {
        if (reason == null || reason.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "软中断必须填写原因");
        }
        AwdRound r = controlService.requireRound(contestId, roundNo);
        RoundStatus st = controlService.statusOf(r);

        // 软中断不迁移状态：只要求非终态
        RoundStateMachine.requireNotTerminal(st, "软中断");

        String ctlKey = AwdKeys.roundControl(contestId, roundNo);
        String fullReason = "SOFT:" + reason + " | by " + operator + "(" + operatorId + ")";

        redisService.hSet(ctlKey, "frozen", "true");
        redisService.hSet(ctlKey, "frozenReason", fullReason);
        redisService.hSet(ctlKey, "frozenAt", LocalDateTime.now().toString());
        redisService.hSet(ctlKey, "frozenBy", operator == null ? String.valueOf(operatorId) : operator);

        // 中断原因也落到 awd_round.interrupt_reason（interrupt_type 记 SOFT，状态保持 RUNNING）
        controlService.writeInterruptReason(r, TYPE_SOFT, fullReason);
        controlService.audit(contestId, roundNo, "INTERRUPT_SOFT", null, operatorId, operator,
                "软中断冻结攻击窗口：" + reason);

        wsPublisher.publish("ROUND_INTERRUPT", contestId, Map.of(
                "contestId", contestId, "roundNo", roundNo,
                "type", TYPE_SOFT, "frozen", true, "reason", reason));

        log.warn("轮次软中断 contest={} round={} reason={} by={}", contestId, roundNo, reason, operator);
        Map<String, Object> m = controlService.view(contestId, roundNo);
        m.put("interruptType", TYPE_SOFT);
        m.put("interruptReason", fullReason);
        m.put("frozen", true);
        m.put("message", "软中断已生效：攻击窗口暂停，已提交成绩保留");
        return m;
    }

    /**
     * 恢复：解除软中断冻结。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> resume(Long contestId, Integer roundNo, Long operatorId, String operator) {
        AwdRound r = controlService.requireRound(contestId, roundNo);
        RoundStatus st = controlService.statusOf(r);
        RoundStateMachine.requireNotTerminal(st, "恢复");

        String ctlKey = AwdKeys.roundControl(contestId, roundNo);
        if (!controlService.isFrozen(contestId, roundNo)) {
            throw new BizException(ResultCode.CONFLICT, "轮次当前未处于软中断冻结状态");
        }
        redisService.hDelete(ctlKey, "frozen", "frozenReason", "frozenAt", "frozenBy");
        controlService.audit(contestId, roundNo, "RESUME", null, operatorId, operator, "解除软中断冻结");

        wsPublisher.publish("ROUND_INTERRUPT", contestId, Map.of(
                "contestId", contestId, "roundNo", roundNo,
                "type", TYPE_SOFT, "frozen", false, "reason", "resumed"));

        log.info("轮次软中断已恢复 contest={} round={} by={}", contestId, roundNo, operator);
        Map<String, Object> m = controlService.view(contestId, roundNo);
        m.put("frozen", false);
        m.put("message", "软中断已解除，攻击窗口恢复");
        return m;
    }

    // ================================================================== 硬中断

    /**
     * 硬中断：立即中止本轮并尽力结算。
     *
     * <p>顺序：状态机校验可中止 → 迁 ABORTED（写 interrupt_type/reason）→ 尽力结算。
     * 结算失败只记录日志，不改变「已中止」的事实。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> hardInterrupt(Long contestId, Integer roundNo, String reason,
                                             Long operatorId, String operator) {
        if (reason == null || reason.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "硬中断必须填写原因");
        }
        AwdRound r = controlService.requireRound(contestId, roundNo);
        RoundStatus from = controlService.statusOf(r);

        // 硬中断是状态迁移：任意非终态 → ABORTED
        RoundStateMachine.requireAbortable(from);
        RoundStateMachine.requireTransition(from, RoundStatus.ABORTED);

        String fullReason = "HARD:" + reason + " | by " + operator + "(" + operatorId + ")";

        // 1) 迁 ABORTED 并写中断字段
        controlService.casStatus(r, from, RoundStatus.ABORTED, TYPE_HARD, null);
        controlService.writeInterruptReason(r, TYPE_HARD, fullReason);

        // 2) 清除可能的软中断冻结标记
        redisService.hDelete(AwdKeys.roundControl(contestId, roundNo), "frozen", "frozenReason");

        // 3) 尽力结算（幂等）。失败不阻断中止。
        boolean settled = false;
        String settleError = null;
        try {
            scoreService.settleRound(contestId, roundNo);
            settled = true;
        } catch (Exception e) {
            settleError = e.getMessage();
            log.error("硬中断后的结算失败（不影响中止结果）contest={} round={}", contestId, roundNo, e);
        }

        controlService.audit(contestId, roundNo, "INTERRUPT_HARD", null, operatorId, operator,
                "硬中断中止：" + from + " → ABORTED，" + reason + "，结算=" + (settled ? "成功" : "失败:" + settleError));

        wsPublisher.publish("ROUND_INTERRUPT", contestId, Map.of(
                "contestId", contestId, "roundNo", roundNo,
                "type", TYPE_HARD, "aborted", true, "reason", reason, "settled", settled));

        log.warn("轮次硬中断 contest={} round={} from={} reason={} by={} settled={}",
                contestId, roundNo, from, reason, operator, settled);

        Map<String, Object> m = controlService.view(contestId, roundNo);
        m.put("interruptType", TYPE_HARD);
        m.put("interruptReason", fullReason);
        m.put("from", from.name());
        m.put("to", RoundStatus.ABORTED.name());
        m.put("settled", settled);
        m.put("settleError", settleError);
        m.put("message", "硬中断已生效：本轮立即中止" + (settled ? "，已完成结算" : "，结算待人工补做"));
        return m;
    }

    /**
     * 统一入口：按 type 分发软/硬中断。
     */
    public Map<String, Object> interrupt(Long contestId, Integer roundNo, String type, String reason,
                                         Long operatorId, String operator) {
        if (type == null || type.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "中断类型不能为空（SOFT / HARD）");
        }
        String t = type.trim().toUpperCase();
        return switch (t) {
            case TYPE_SOFT -> softInterrupt(contestId, roundNo, reason, operatorId, operator);
            case TYPE_HARD -> hardInterrupt(contestId, roundNo, reason, operatorId, operator);
            default -> throw new BizException(ResultCode.BAD_REQUEST,
                    "非法的中断类型：" + type + "（仅支持 SOFT / HARD）");
        };
    }

    /** 中断状态查询。 */
    public Map<String, Object> status(Long contestId, Integer roundNo) {
        Map<String, Object> m = controlService.view(contestId, roundNo);
        m.put("interrupted", m.get("interruptType") != null);
        m.put("frozen", controlService.isFrozen(contestId, roundNo));
        return new LinkedHashMap<>(m);
    }
}