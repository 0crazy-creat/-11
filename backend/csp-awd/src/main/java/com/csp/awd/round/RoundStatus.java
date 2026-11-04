package com.csp.awd.round;

import java.util.Set;

/**
 * AWD 轮次状态枚举（轮次控制域独立实现）。
 *
 * <pre>
 *  PENDING ──begin──▶ RUNNING ──settle──▶ SETTLING ──finish──▶ FINISHED
 *     │                  │                   │                    │
 *     └──────────────────┴───────────────────┴────────────────────┘
 *                          （硬中断）──▶ ABORTED
 * </pre>
 *
 * <p>与 {@code com.csp.awd.enums.RoundStatus} 的区别：本枚举是 {@code com.csp.awd.round}
 * 包内轮次控制接口（begin/running/settle/interrupt/dispute/arbitrate）的权威状态定义，
 * 迁移表按「控制面」收敛：PENDING→RUNNING→SETTLING→FINISHED，任意非终态可 ABORTED。
 * 攻击窗口到点自动收窗（RUNNING→CHECKING）属时序编排域，由定时器负责，本状态机不涉及。</p>
 */
public enum RoundStatus {

    /** 已创建，未开始。此阶段可安全轮换 flag。 */
    PENDING,
    /** 攻击窗口开启中，接受攻击提交。 */
    RUNNING,
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
            case RUNNING -> target == SETTLING || target == ABORTED;
            case SETTLING -> target == FINISHED || target == ABORTED;
            case FINISHED, ABORTED -> false;
        };
    }
}