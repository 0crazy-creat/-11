package com.csp.awd.engine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 攻防计分核心算法（纯函数，无 IO，便于单测与复用）。
 *
 * <p>所有方法返回<b>增量</b>分值，由 Service 层负责累加到 Redis 排行榜与落库。</p>
 */
@Component
@RequiredArgsConstructor
public class AwdScoringEngine {

    private final ScorePolicy policy;

    /**
     * 计算一次成功攻击的攻击方得分。
     *
     * @param previousHits 攻击方此前对同一目标的成功攻破次数（0 表示首次）
     * @param firstBlood   是否为该目标的首杀
     */
    public long attackGain(int previousHits, boolean firstBlood) {
        if (previousHits < 0) previousHits = 0;
        double factor = Math.pow(policy.getRepeatDecay(), previousHits);
        long base = Math.max(policy.getRepeatFloor(), Math.round(policy.getAttackBase() * factor));
        long gain = base + (firstBlood ? policy.getFirstBloodBonus() : 0);
        if (policy.getAttackScoreCap() > 0) {
            gain = Math.min(gain, policy.getAttackScoreCap());
        }
        return gain;
    }

    /**
     * 计算被攻破方的 SLA 扣分（负值）。
     *
     * @param currentPenalty 该队已累计的 SLA 扣分（负数或 0）
     */
    public long slaPenalty(long currentPenalty) {
        long floor = policy.getSlaPenaltyFloor();
        // 已到达（或已越过）扣分下限：不再扣分，返回 0，绝不允许出现“正增量回血”
        if (floor < 0 && currentPenalty <= floor) {
            return 0;
        }
        long next = currentPenalty - Math.max(0, policy.getSlaPenaltyPerHit());
        if (floor < 0 && next < floor) {
            next = floor;
        }
        return next - currentPenalty;
    }

    /** AWDP：单轮零被攻破的防御加成。 */
    public long cleanRoundBonus() {
        return Math.max(0, policy.getDefenseBonusPerCleanRound());
    }

    /** AWDP：有效修补奖励。 */
    public long patchBonus() {
        return Math.max(0, policy.getPatchBonus());
    }

    /** 总分 = 攻击分 + 防御分 + SLA 扣分（扣分为负）。 */
    public long total(long attackScore, long defenseScore, long slaPenalty) {
        return attackScore + defenseScore + slaPenalty;
    }

    /** 暴露策略供上层读取（如展示规则说明）。 */
    public ScorePolicy policy() {
        return policy;
    }
}