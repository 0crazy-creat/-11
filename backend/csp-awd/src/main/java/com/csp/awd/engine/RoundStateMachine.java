package com.csp.awd.engine;

import com.csp.awd.enums.RoundStatus;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;

import java.util.EnumSet;
import java.util.Set;

/**
 * 轮次状态机引擎：对 {@link RoundStatus} 迁移做统一守卫。
 *
 * <p>状态流转：PENDING → RUNNING → CHECKING → SETTLING → FINISHED，任意非终态可 ABORTED。
 * 状态机定义以 {@link RoundStatus#canTransitTo} 为唯一权威，本类只负责校验与异常包装。</p>
 */
public final class RoundStateMachine {

    private RoundStateMachine() {}

    /** 解析并校验外部传入的状态字符串。 */
    public static RoundStatus parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw BizException.of("轮次状态不能为空");
        }
        try {
            return RoundStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw BizException.of("非法的轮次状态：" + raw);
        }
    }

    /**
     * 校验迁移是否合法，非法时抛出业务异常。
     *
     * @param from 当前状态
     * @param to   目标状态
     */
    public static void requireTransition(RoundStatus from, RoundStatus to) {
        if (from == null) {
            throw BizException.of("轮次当前状态为空，无法迁移");
        }
        if (to == null) {
            throw BizException.of("目标状态不能为空");
        }
        if (from == to) {
            throw new BizException(ResultCode.BAD_REQUEST, "轮次已处于 " + to + " 状态，不可重复迁移");
        }
        if (!from.canTransitTo(to)) {
            throw new BizException(ResultCode.BAD_REQUEST,
                    "非法状态迁移：" + from + " → " + to);
        }
    }

    /** 判断迁移是否合法（不抛异常，供编排逻辑分支使用）。 */
    public static boolean canTransition(RoundStatus from, RoundStatus to) {
        return from != null && to != null && from != to && from.canTransitTo(to);
    }

    /** 从当前状态可到达的下一步状态集合。 */
    public static Set<RoundStatus> nextStates(RoundStatus from) {
        Set<RoundStatus> out = EnumSet.noneOf(RoundStatus.class);
        if (from == null) return out;
        for (RoundStatus s : RoundStatus.values()) {
            if (from.canTransitTo(s)) out.add(s);
        }
        return out;
    }

    /**
     * 自动推进判定：给定当前状态与剩余时间，返回应自动迁移到的状态（无则 null）。
     *
     * <p>供定时编排器调用，实现"到点收攻击窗口 / check 完成进入结算 / 结算完成转 FINISHED"。
     * 注意 SETTLING → FINISHED 的推进由编排器在结算成功后显式触发，
     * 这里给出判定语义以保证状态机的完备性。</p>
     *
     * @param current          当前状态
     * @param remainingSeconds 攻击窗口剩余秒数（仅 RUNNING 有意义）
     * @param checkDone        CHECKING 阶段的 check 脚本是否已执行完
     */
    public static RoundStatus autoAdvance(RoundStatus current, long remainingSeconds, boolean checkDone) {
        if (current == null) return null;
        return switch (current) {
            case RUNNING -> remainingSeconds <= 0 ? RoundStatus.CHECKING : null;
            case CHECKING -> checkDone ? RoundStatus.SETTLING : null;
            // 结算阶段由编排器在 settleRound 成功（幂等标记）后推进；
            // tick() 传入 checkDone=true 表示“可推进”，这里直接落到终态。
            case SETTLING -> checkDone ? RoundStatus.FINISHED : null;
            default -> null;
        };
    }
}