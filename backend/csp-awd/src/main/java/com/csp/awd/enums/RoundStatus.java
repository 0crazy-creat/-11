package com.csp.awd.enums;

import java.util.Set;

/**
 * AWD 轮次状态机。
 *
 * <pre>
 *  PENDING ──begin──▶ RUNNING ──(攻击窗口到点)──▶ CHECKING ──(check 脚本执行完)──▶ SETTLING ──(结算完)──▶ FINISHED
 *     │                  │                          │                                  │
 *     └──────────────────┴──────────────────────────┴──────────────────────────────────┴──▶ ABORTED（硬中断）
 * </pre>
 *
 * <p>状态语义（对齐 Cardinal / OpenClaw Round Orchestrator）：</p>
 * <ul>
 *   <li>PENDING  ：轮次已创建，尚未开始；此阶段可安全执行 flag 轮换。</li>
 *   <li>RUNNING  ：攻击窗口开启，接受攻击提交。</li>
 *   <li>CHECKING ：攻击窗口关闭，对每队靶机执行 check 脚本（HTTP 探活 + 功能校验）。</li>
 *   <li>SETTLING ：check 结果已落库，正在结算攻防分/SLA 分并推送排行榜（幂等）。</li>
 *   <li>FINISHED ：结算完成，终态。</li>
 *   <li>ABORTED  ：硬中断，终态。</li>
 * </ul>
 *
 * <p>软中断不改变状态，仅冻结攻击窗口（见 {@code AwdRoundService#softInterrupt}）。</p>
 */
public enum RoundStatus {
    PENDING, RUNNING, CHECKING, SETTLING, FINISHED, ABORTED;

    private static final Set<RoundStatus> TERMINAL = Set.of(FINISHED, ABORTED);

    public boolean isTerminal() { return TERMINAL.contains(this); }

    /** 合法迁移表：状态机唯一权威定义。 */
    public boolean canTransitTo(RoundStatus target) {
        return switch (this) {
            case PENDING -> target == RUNNING || target == ABORTED;
            case RUNNING -> target == CHECKING || target == ABORTED;
            case CHECKING -> target == SETTLING || target == ABORTED;
            case SETTLING -> target == FINISHED || target == ABORTED;
            case FINISHED, ABORTED -> false;
        };
    }
}