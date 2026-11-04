package com.csp.awd.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 攻防计分算法：首杀、重复衰减、SLA 下限、总分。 */
class AwdScoringEngineTest {

    private AwdScoringEngine engine;

    @BeforeEach
    void setUp() {
        ScorePolicy p = new ScorePolicy();
        p.setAttackBase(100);
        p.setRepeatDecay(0.5);
        p.setRepeatFloor(10);
        p.setFirstBloodBonus(50);
        p.setSlaPenaltyPerHit(30);
        p.setSlaPenaltyFloor(-60);
        p.setDefenseBonusPerCleanRound(20);
        p.setPatchBonus(50);
        p.setAttackScoreCap(0);
        engine = new AwdScoringEngine(p);
    }

    @Test
    @DisplayName("首次攻破拿基础分；首杀额外加成")
    void attackBaseAndFirstBlood() {
        assertEquals(100, engine.attackGain(0, false));
        assertEquals(150, engine.attackGain(0, true));
    }

    @Test
    @DisplayName("重复攻破按衰减递减并保底")
    void repeatDecay() {
        assertEquals(50, engine.attackGain(1, false));
        assertEquals(25, engine.attackGain(2, false));
        assertEquals(13, engine.attackGain(3, false));   // 100*0.125=12.5 → Math.round=13
        assertEquals(10, engine.attackGain(10, false));  // 低于保底取 10
    }

    @Test
    @DisplayName("SLA 扣分累计到下限即停止")
    void slaPenaltyFloor() {
        assertEquals(-30, engine.slaPenalty(0));
        assertEquals(-30, engine.slaPenalty(-30));  // -30-30=-60 正好到下限
        assertEquals(0, engine.slaPenalty(-60));    // 已到下限，不再扣
        assertEquals(0, engine.slaPenalty(-70));    // 越过下限也不回补
    }

    @Test
    @DisplayName("AWDP 防御加成与修补加分")
    void defenseBonuses() {
        assertEquals(20, engine.cleanRoundBonus());
        assertEquals(50, engine.patchBonus());
    }

    @Test
    @DisplayName("总分 = 攻击 + 防御 + SLA（扣分为负）")
    void total() {
        assertEquals(120, engine.total(100, 50, -30));
        assertEquals(0, engine.total(0, 0, 0));
        assertEquals(-30, engine.total(0, 0, -30));
    }

    @Test
    @DisplayName("攻击分上限生效")
    void attackCap() {
        ScorePolicy p = new ScorePolicy();
        p.setAttackBase(100);
        p.setFirstBloodBonus(50);
        p.setAttackScoreCap(80);
        AwdScoringEngine capped = new AwdScoringEngine(p);
        assertEquals(80, capped.attackGain(0, true));
    }
}