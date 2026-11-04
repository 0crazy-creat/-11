package com.csp.platform.format;

import com.csp.platform.entity.ContestInfo;

import java.util.Set;

/**
 * 赛制处理器策略接口 —— 「底座共享、处理器隔离」的统一路由层。
 *
 * <p>六大赛制（CTF/AWD/AWDP/THEORY/SCENARIO/CFS）各提供一个实现，
 * 由 {@link ContestFormatRouter} 按 {@link ContestInfo#getType()} 路由。
 * 平台通用能力（创建/组卷/报名/状态机）仍由 ContestService 承担，
 * 赛制特有钩子（靶机是否动态、开赛拉起、计分入口、结束清理）由本接口承担，
 * 从而把 ContestService 里散落的 if-else 收敛为策略分发。</p>
 *
 * <p>所有钩子默认空实现，实现类只覆盖自己关心的钩子，避免误伤。</p>
 */
public interface ContestFormatHandler {

    /** 本处理器负责的赛制类型码（大写），供路由索引。 */
    String type();

    /** 是否动态靶机（决定选手端是否出现「启动容器/靶机」入口）。 */
    default boolean isTargetsDynamic() {
        return false;
    }

    /**
     * 赛制对组卷题库类型的约束。
     * <p>默认单场赛事题库类型须一致；CFS 允许跨题库混合。返回 true 表示允许跨 bank_type。</p>
     */
    default boolean allowCrossBankTypes() {
        return false;
    }

    /**
     * 开赛钩子：赛事状态迁移到 RUNNING 后调用。
     * <p>CTF 无需动作；AWD/AWDP 拉起首轮；CFS 下发拓扑并实例化各队拓扑；THEORY/SCENARIO 下发答卷/场景。</p>
     *
     * @param contest 已置为 RUNNING 的赛事
     */
    default void onStart(ContestInfo contest) {
        // no-op by default
    }

    /**
     * 计分钩子：赛制内一次得分事件后的统一回调（例如刷新排行榜缓存、推送大屏）。
     *
     * @param contestId 赛事
     * @param teamId    战队（个人赛可为 null）
     * @param delta     本次得分增量
     * @param reason    得分原因（用于审计/大屏）
     */
    default void onScore(Long contestId, Long teamId, int delta, String reason) {
        // no-op by default
    }

    /**
     * 结束钩子：赛事状态迁移到 FINISHED 后调用（回收靶机、封盘、快照排名）。
     */
    default void onFinish(ContestInfo contest) {
        // no-op by default
    }

    /** 供日志/审计：本赛制声明的能力标签。 */
    default Set<String> capabilities() {
        return Set.of();
    }
}