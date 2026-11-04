package com.csp.awd.round;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 轮次控制域状态机测试：合法链路、非法跃迁、终态保护、中断准入、解析容错。
 *
 * <p>纯函数无 IO，不依赖 Spring 上下文。</p>
 */
class RoundStateMachineTest {

    // ================================================================ 合法链路

    @Test
    @DisplayName("合法主链路 PENDING→RUNNING→SETTLING→FINISHED 全部通过")
    void legalHappyPath() {
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.PENDING, RoundStatus.RUNNING));
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.RUNNING, RoundStatus.SETTLING));
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.SETTLING, RoundStatus.FINISHED));
    }

    @Test
    @DisplayName("任意非终态都可硬中断到 ABORTED")
    void abortFromAnyNonTerminal() {
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.PENDING, RoundStatus.ABORTED));
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.RUNNING, RoundStatus.ABORTED));
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.SETTLING, RoundStatus.ABORTED));
    }

    @Test
    @DisplayName("SETTLING 是合法中间态，枚举中存在")
    void settlingExists() {
        assertNotNull(RoundStatus.valueOf("SETTLING"));
        assertFalse(RoundStatus.SETTLING.isTerminal());
        assertFalse(RoundStatus.SETTLING.isAttackWindowOpen());
    }

    // ================================================================ 非法跃迁

    @Test
    @DisplayName("跨越 RUNNING 直接 PENDING→SETTLING 被拒绝")
    void skipRunningRejected() {
        BizException e = assertThrows(BizException.class,
                () -> RoundStateMachine.requireTransition(RoundStatus.PENDING, RoundStatus.SETTLING));
        assertEquals(ResultCode.BAD_REQUEST.getCode(), e.getCode());
        assertTrue(e.getMessage().contains("非法状态迁移"));
    }

    @Test
    @DisplayName("SETTLING 回退到 RUNNING 被拒绝")
    void backwardTransitionRejected() {
        assertThrows(BizException.class,
                () -> RoundStateMachine.requireTransition(RoundStatus.SETTLING, RoundStatus.RUNNING));
    }

    @Test
    @DisplayName("RUNNING 直接到 FINISHED 被拒绝（必须先结算 SETTLING）")
    void runningToFinishedRejected() {
        assertThrows(BizException.class,
                () -> RoundStateMachine.requireTransition(RoundStatus.RUNNING, RoundStatus.FINISHED));
    }

    @Test
    @DisplayName("终态不可再迁移：FINISHED / ABORTED 作为源全部拒绝")
    void terminalCannotTransit() {
        for (RoundStatus to : RoundStatus.values()) {
            assertFalse(RoundStatus.FINISHED.canTransitTo(to), "FINISHED 不应能迁移到 " + to);
            assertFalse(RoundStatus.ABORTED.canTransitTo(to), "ABORTED 不应能迁移到 " + to);
        }
        assertThrows(BizException.class,
                () -> RoundStateMachine.requireTransition(RoundStatus.FINISHED, RoundStatus.RUNNING));
        assertThrows(BizException.class,
                () -> RoundStateMachine.requireTransition(RoundStatus.ABORTED, RoundStatus.RUNNING));
    }

    @Test
    @DisplayName("原地迁移被拒绝，且错误码为 400")
    void selfTransitionRejected() {
        BizException e = assertThrows(BizException.class,
                () -> RoundStateMachine.requireTransition(RoundStatus.RUNNING, RoundStatus.RUNNING));
        assertEquals(ResultCode.BAD_REQUEST.getCode(), e.getCode());
        assertTrue(e.getMessage().contains("不可重复迁移"));
    }

    @Test
    @DisplayName("空状态被拒绝")
    void nullRejected() {
        assertThrows(BizException.class, () -> RoundStateMachine.requireTransition(null, RoundStatus.RUNNING));
        assertThrows(BizException.class, () -> RoundStateMachine.requireTransition(RoundStatus.RUNNING, null));
    }

    // ================================================================ nextStates

    @Test
    @DisplayName("nextStates 与迁移表一致")
    void nextStatesConsistent() {
        assertEquals(Set.of(RoundStatus.RUNNING, RoundStatus.ABORTED),
                RoundStateMachine.nextStates(RoundStatus.PENDING));
        assertEquals(Set.of(RoundStatus.SETTLING, RoundStatus.ABORTED),
                RoundStateMachine.nextStates(RoundStatus.RUNNING));
        assertEquals(Set.of(RoundStatus.FINISHED, RoundStatus.ABORTED),
                RoundStateMachine.nextStates(RoundStatus.SETTLING));
        assertTrue(RoundStateMachine.nextStates(RoundStatus.FINISHED).isEmpty());
        assertTrue(RoundStateMachine.nextStates(RoundStatus.ABORTED).isEmpty());
        assertTrue(RoundStateMachine.nextStates(null).isEmpty());
    }

    // ================================================================ 软中断：不迁移状态

    @Test
    @DisplayName("软中断：非终态允许（不改变状态）")
    void softInterruptAllowedOnNonTerminal() {
        assertDoesNotThrow(() -> RoundStateMachine.requireNotTerminal(RoundStatus.PENDING, "软中断"));
        assertDoesNotThrow(() -> RoundStateMachine.requireNotTerminal(RoundStatus.RUNNING, "软中断"));
        assertDoesNotThrow(() -> RoundStateMachine.requireNotTerminal(RoundStatus.SETTLING, "软中断"));
    }

    @Test
    @DisplayName("软中断：终态被拒绝，错误码 409")
    void softInterruptRejectedOnTerminal() {
        BizException e1 = assertThrows(BizException.class,
                () -> RoundStateMachine.requireNotTerminal(RoundStatus.FINISHED, "软中断"));
        assertEquals(ResultCode.CONFLICT.getCode(), e1.getCode());
        assertThrows(BizException.class,
                () -> RoundStateMachine.requireNotTerminal(RoundStatus.ABORTED, "软中断"));
    }

    // ================================================================ 硬中断准入

    @Test
    @DisplayName("硬中断：非终态可中止")
    void hardInterruptAllowedOnNonTerminal() {
        assertDoesNotThrow(() -> RoundStateMachine.requireAbortable(RoundStatus.PENDING));
        assertDoesNotThrow(() -> RoundStateMachine.requireAbortable(RoundStatus.RUNNING));
        assertDoesNotThrow(() -> RoundStateMachine.requireAbortable(RoundStatus.SETTLING));
    }

    @Test
    @DisplayName("硬中断：终态重复中止被拒绝，错误码 409")
    void hardInterruptRejectedOnTerminal() {
        BizException e = assertThrows(BizException.class,
                () -> RoundStateMachine.requireAbortable(RoundStatus.ABORTED));
        assertEquals(ResultCode.CONFLICT.getCode(), e.getCode());
        assertTrue(e.getMessage().contains("无法再次中断"));
        assertThrows(BizException.class, () -> RoundStateMachine.requireAbortable(RoundStatus.FINISHED));
    }

    // ================================================================ 攻击窗口准入

    @Test
    @DisplayName("攻击窗口：RUNNING 且未冻结才接受提交")
    void attackWindowOpenOnlyWhenRunningAndNotFrozen() {
        assertDoesNotThrow(() ->
                RoundStateMachine.requireAttackWindowOpen(RoundStatus.RUNNING, false, 1));

        // 冻结中 → 拒绝
        BizException frozen = assertThrows(BizException.class,
                () -> RoundStateMachine.requireAttackWindowOpen(RoundStatus.RUNNING, true, 1));
        assertTrue(frozen.getMessage().contains("软中断冻结"));

        // 非 RUNNING → 拒绝
        assertThrows(BizException.class,
                () -> RoundStateMachine.requireAttackWindowOpen(RoundStatus.PENDING, false, 1));
        assertThrows(BizException.class,
                () -> RoundStateMachine.requireAttackWindowOpen(RoundStatus.SETTLING, false, 1));

        // 终态 → 拒绝
        BizException done = assertThrows(BizException.class,
                () -> RoundStateMachine.requireAttackWindowOpen(RoundStatus.FINISHED, false, 3));
        assertTrue(done.getMessage().contains("已结束"));
    }

    // ================================================================ 解析容错

    @Test
    @DisplayName("parse 大小写不敏感，非法值报错")
    void parseWorks() {
        assertEquals(RoundStatus.SETTLING, RoundStateMachine.parse("settling"));
        assertEquals(RoundStatus.RUNNING, RoundStateMachine.parse("  RUNNING "));
        assertThrows(BizException.class, () -> RoundStateMachine.parse("CHECKING")); // 控制域无此态
        assertThrows(BizException.class, () -> RoundStateMachine.parse(null));
        assertThrows(BizException.class, () -> RoundStateMachine.parse("  "));
    }

    @Test
    @DisplayName("parseOrDefault 对非法值降级不抛异常")
    void parseOrDefaultDegrades() {
        assertEquals(RoundStatus.RUNNING, RoundStateMachine.parseOrDefault("running", RoundStatus.PENDING));
        assertEquals(RoundStatus.PENDING, RoundStateMachine.parseOrDefault("CHECKING", RoundStatus.PENDING));
        assertEquals(RoundStatus.PENDING, RoundStateMachine.parseOrDefault(null, RoundStatus.PENDING));
    }

    @Test
    @DisplayName("canTransition 为非抛异常版本，与 requireTransition 结论一致")
    void canTransitionMatches() {
        assertTrue(RoundStateMachine.canTransition(RoundStatus.PENDING, RoundStatus.RUNNING));
        assertFalse(RoundStateMachine.canTransition(RoundStatus.PENDING, RoundStatus.SETTLING));
        assertFalse(RoundStateMachine.canTransition(RoundStatus.RUNNING, RoundStatus.RUNNING));
        assertFalse(RoundStateMachine.canTransition(null, RoundStatus.RUNNING));
        assertFalse(RoundStateMachine.canTransition(RoundStatus.RUNNING, null));
    }
}