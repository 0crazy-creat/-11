package com.csp.awd.engine;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 攻防计分策略（可配置，避免魔法数字散落）。
 *
 * <p>计分模型：</p>
 * <ul>
 *   <li><b>攻击得分</b>：攻破对手靶机 +{@code attackBase}，同一对手重复攻破递减（{@code repeatDecay}）。</li>
 *   <li><b>首杀奖励</b>：本题首个攻破该对手的队伍额外 +{@code firstBloodBonus}。</li>
 *   <li><b>SLA 扣分</b>：被攻破一次扣 {@code slaPenaltyPerHit}（对手得分的一半，向上取整），
 *       低于 {@code slaPenaltyFloor} 时不再扣分。</li>
 *   <li><b>防御得分</b>：AWDP 模式下每轮未被攻破 +{@code defenseBonusPerCleanRound}；
 *       提交有效修补 +{@code patchBonus}。</li>
 * </ul>
 */
@Data
@Component
@ConfigurationProperties(prefix = "csp.awd.score")
public class ScorePolicy {
    /** 攻破一次基础得分 */
    private long attackBase = 100;
    /** 重复攻破同一目标的衰减系数（第二次 = base * decay） */
    private double repeatDecay = 0.5;
    /** 重复攻破最低保留分 */
    private long repeatFloor = 10;
    /** 首杀额外奖励 */
    private long firstBloodBonus = 50;
    /** 被攻破一次的 SLA 扣分 */
    private long slaPenaltyPerHit = 30;
    /** SLA 累计扣分下限（负值），0 表示不限制 */
    private long slaPenaltyFloor = -300;
    /** check 脚本每失败一台靶机扣的 SLA 分（>0，结算时取负） */
    private long slaPenaltyPerFailedCheck = 20;
    /** AWDP：单轮零被攻破的防御加成 */
    private long defenseBonusPerCleanRound = 20;
    /** AWDP：有效修补奖励 */
    private long patchBonus = 50;
    /** 允许单队最高累计攻击分（0 表示不限制） */
    private long attackScoreCap = 0;
}