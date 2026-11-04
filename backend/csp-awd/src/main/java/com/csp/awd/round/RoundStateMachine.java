package com.csp.awd.round;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;

import java.util.EnumSet;
import java.util.Set;

/**
 * 轮次状态机：对 {@link RoundStatus} 的迁移做统一守卫，是轮次控制接口的唯一校验入口。
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>状态迁移表以 {@link RoundStatus#canTransitTo} 为唯一权威，本类只做校验与异常包装；</li>
 *   <li>所有控制面接口（begin/running/settle/interrupt/dispute/arbitrate）必须经过本类校验，
 *       禁止在 Controller/Service 中手写 if 判断状态；</li>
 *   <li>软中断（softInterrupt）<b>不迁移状态</b>，仅冻结攻击窗口，因此不经过
 *       {@link #requireTransition}，而走 {@link #requireNotTerminal}；</li>
 *   <li>硬中断（hardInterrupt）从任意非终态迁移到 ABORTED。</li>
 * </ul>
 */
public final class RoundStateMachine {

    /** 硬中断允许的源状态：除终态外全部。 */
    private static final Set<RoundStatus> ABORTABLE = EnumSet.complementOf(
            EnumSet.of(RoundStatus.FINISHED, RoundStatus.ABORTED));

    private RoundStateMachine() {
    }

    /**
     * 解析并校验外部传入的状态字符串（大小写不敏感）。
     *
     * @param raw 状态字符串，如 "running"
     * @throws BizException 为空或非法枚举值时
     */
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
     * 宽松解析：非法值/空值返回 {@code fallback}，供读库兼容与降级使用。
     */
    public static RoundStatus parseOrDefault(String raw, RoundStatus fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return RoundStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    /**
     * 校验迁移合法，非法时抛业务异常。这是控制面接口进入下一步之前的必经关卡。
     *
     * @param from 当前状态
     * @param to   目标状态
     * @throws BizException 状态为空 / 原地迁移 / 非法迁移
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
                    "非法状态迁移：" + from + " → " + to + "（当前可迁移至：" + nextStates(from) + "）");
        }
    }

    /** 判断迁移是否合法，不抛异常；供编排分支使用。 */
    public static boolean canTransition(RoundStatus from, RoundStatus to) {
        return from != null && from.canTransitTo(to);
    }

    /** 从当前状态可到达的下一步状态集合，用于错误提示与文档。 */
    public static Set<RoundStatus> nextStates(RoundStatus from) {
        Set<RoundStatus> out = EnumSet.noneOf(RoundStatus.class);
        if (from == null) {
            return out;
        }
        for (RoundStatus s : RoundStatus.values()) {
            if (from.canTransitTo(s)) {
                out.add(s);
            }
        }
        return out;
    }

    /**
     * 软中断前置校验：轮次必须存在且非终态（软中断不改变状态）。
     */
    public static void requireNotTerminal(RoundStatus current, String opName) {
        if (current == null) {
            throw BizException.of("轮次当前状态为空，无法执行：" + opName);
        }
        if (current.isTerminal()) {
            throw new BizException(ResultCode.CONFLICT,
                    "轮次已处于终态 " + current + "，无法执行：" + opName);
        }
    }

    /**
     * 硬中断校验：任意非终态都可中止；终态不可重复中止。
     */
    public static void requireAbortable(RoundStatus current) {
        if (current == null) {
            throw BizException.of("轮次当前状态为空，无法中断");
        }
        if (!ABORTABLE.contains(current)) {
            throw new BizException(ResultCode.CONFLICT,
                    "轮次已处于终态 " + current + "，无法再次中断");
        }
    }

    /**
     * 攻击提交准入校验：只有攻击窗口开启且未被软中断冻结时才接受提交。
     *
     * @param current      当前状态
     * @param frozen       是否处于软中断冻结
     * @param roundNo      轮次号（仅用于报错文案）
     */
    public static void requireAttackWindowOpen(RoundStatus current, boolean frozen, int roundNo) {
        if (current == null) {
            throw BizException.of("轮次当前状态为空");
        }
        if (current.isTerminal()) {
            throw new BizException(ResultCode.CONFLICT,
                    "第 " + roundNo + " 轮已结束（" + current + "），不再接受提交");
        }
        if (frozen) {
            throw new BizException(ResultCode.CONFLICT,
                    "第 " + roundNo + " 轮已被软中断冻结，攻击窗口暂停，暂不接受提交");
        }
        if (!current.isAttackWindowOpen()) {
            throw new BizException(ResultCode.CONFLICT,
                    "第 " + roundNo + " 轮当前状态为 " + current + "，攻击窗口未开启");
        }
    }
}