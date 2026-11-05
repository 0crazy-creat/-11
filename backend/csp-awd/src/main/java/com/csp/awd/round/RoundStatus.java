package com.csp.awd.round;

import java.util.Set;

/**
 * AWD 轮次状态枚举（轮次控制域独立实现）。
 *
 * <pre>
 *  PENDING ──begin──▶ RUNNING ──check──▶ CHECKING ──settle──▶ SETTLING ──finish──▶ FINISHED
 *     │                  │                   │                    │
 *     └──────────────────┴───────────────────┴────────────────────┘
 *                          （硬中断）──▶ ABORTED
 * </pre>
 *
 * <p>与 {@code com.csp.awd.enums.RoundStatus} 的区别：本枚举是 {@code com.csp.awd.round}
 * 包内轮次控制接口（begin/running/settle/interrupt/dispute/arbitrate）的权威状态定义，
 * 迁移表按「控制面」收敛，额外允许 RUNNING→SETTLING（裁判提前收卷直接结算）。
 * 两侧共用的状态（PENDING/RUNNING/CHECKING/SETTLING/FINISHED/ABORTED）取值必须保持一致，
 * 否则读到的库值会在本侧解析失败并被降级成 PENDING。</p>
 */
public enum RoundStatus {

    /** 已创建，未开始。此阶段可安全轮换 flag。 */
    PENDING,
    /** 攻击窗口开启中，接受攻击提交。 */
    RUNNING,
    /**
     * 攻击窗口已关闭，check 脚本执行中。
     *
     * <p>{@code AwdRoundService}（{@code com.csp.awd.enums.RoundStatus}）会写入该状态，
     * 控制面必须识别它：否则 {@code RoundStateMachine.parseOrDefault} 把无法解析的值降级成
     * PENDING，裁判在收卷后点「结算」会收到一句与真实状态无关的
     * 「非法状态迁移：PENDING → SETTLING」。</p>
     */
    CHECKING,
    /** 攻击窗口已关闭，结算攻防分/SLA 分并推送排行榜（幂等）。 */
    SETTLING,
    /** 结算完成，终态。 */
    FINISHED,
    /** 硬中断立即中止，终态。 */
    ABORTED;

    private static final Set<RoundStatus> TERMINAL = Set.of(FINISHED, ABORTED);

    /** 是否终态。终态不可再迁移，也不可被中断/申诉影响。 */
    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }

    /** 是否处于「攻击窗口开启、允许提交」的状态。 */
    public boolean isAttackWindowOpen() {
        return this == RUNNING;
    }

    /** 合法迁移表：状态机的唯一权威定义。 */
    public boolean canTransitTo(RoundStatus target) {
        if (target == null || this == target) {
            return false;
        }
        return switch (this) {
            case PENDING -> target == RUNNING || target == ABORTED;
            // RUNNING 可直接 settle（裁判提前收卷结算），也可先 check 再 settle
            case RUNNING -> target == CHECKING || target == SETTLING || target == ABORTED;
            case CHECKING -> target == SETTLING || target == ABORTED;
            case SETTLING -> target == FINISHED || target == ABORTED;
            case FINISHED, ABORTED -> false;
        };
    }
}