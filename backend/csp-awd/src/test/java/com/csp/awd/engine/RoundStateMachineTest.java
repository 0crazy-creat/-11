package com.csp.awd.engine;

import com.csp.awd.enums.RoundStatus;
import com.csp.common.exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** 轮次状态机：合法/非法迁移、终态、自动推进。 */
class RoundStateMachineTest {

    @Test
    @DisplayName("合法链路 PENDING→RUNNING→CHECKING→SETTLING→FINISHED 全部通过")
    void legalHappyPath() {
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.PENDING, RoundStatus.RUNNING));
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.RUNNING, RoundStatus.CHECKING));
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.CHECKING, RoundStatus.SETTLING));
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.SETTLING, RoundStatus.FINISHED));
    }

    @Test
    @DisplayName("任意非终态可中止")
    void abortFromAnyNonTerminal() {
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.PENDING, RoundStatus.ABORTED));
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.RUNNING, RoundStatus.ABORTED));
        assertDoesNotThrow(() -> RoundStateMachine.requireTransition(RoundStatus.CHECKING, RoundStatus.ABORTED));
    }

    @Test
    @DisplayName("跳跃式迁移非法：PENDING→CHECKING / RUNNING→FINISHED")
    void illegalJumps() {
        assertThrows(BizException.class, () -> RoundStateMachine.requireTransition(RoundStatus.PENDING, RoundStatus.CHECKING));
        assertThrows(BizException.class, () -> RoundStateMachine.requireTransition(RoundStatus.RUNNING, RoundStatus.FINISHED));
        assertThrows(BizException.class, () -> RoundStateMachine.requireTransition(RoundStatus.PENDING, RoundStatus.FINISHED));
    }

    @Test
    @DisplayName("终态不可再迁移")
    void terminalIsFinal() {
        assertTrue(RoundStatus.FINISHED.isTerminal());
        assertTrue(RoundStatus.ABORTED.isTerminal());
        assertThrows(BizException.class, () -> RoundStateMachine.requireTransition(RoundStatus.FINISHED, RoundStatus.RUNNING));
        assertThrows(BizException.class, () -> RoundStateMachine.requireTransition(RoundStatus.ABORTED, RoundStatus.RUNNING));
        assertTrue(RoundStateMachine.nextStates(RoundStatus.FINISHED).isEmpty());
    }

    @Test
    @DisplayName("同状态迁移被拒绝（防重复推进）")
    void sameStateRejected() {
        assertThrows(BizException.class, () -> RoundStateMachine.requireTransition(RoundStatus.RUNNING, RoundStatus.RUNNING));
        assertFalse(RoundStateMachine.canTransition(RoundStatus.RUNNING, RoundStatus.RUNNING));
    }

    @Test
    @DisplayName("nextStates 返回正确集合")
    void nextStates() {
        assertEquals(Set.of(RoundStatus.RUNNING, RoundStatus.ABORTED),
                RoundStateMachine.nextStates(RoundStatus.PENDING));
        assertEquals(Set.of(RoundStatus.CHECKING, RoundStatus.ABORTED),
                RoundStateMachine.nextStates(RoundStatus.RUNNING));
        assertEquals(Set.of(RoundStatus.SETTLING, RoundStatus.ABORTED),
                RoundStateMachine.nextStates(RoundStatus.CHECKING));
    }

    @Test
    @DisplayName("自动推进：RUNNING 到点 → CHECKING；CHECKING 完成 → SETTLING")
    void autoAdvance() {
        assertEquals(RoundStatus.CHECKING, RoundStateMachine.autoAdvance(RoundStatus.RUNNING, 0, false));
        assertNull(RoundStateMachine.autoAdvance(RoundStatus.RUNNING, 10, false));
        assertEquals(RoundStatus.SETTLING, RoundStateMachine.autoAdvance(RoundStatus.CHECKING, -1, true));
        assertNull(RoundStateMachine.autoAdvance(RoundStatus.CHECKING, -1, false));
        assertNull(RoundStateMachine.autoAdvance(RoundStatus.PENDING, 100, true));
    }

    @Test
    @DisplayName("parse 大小写容错与非法值")
    void parse() {
        assertEquals(RoundStatus.RUNNING, RoundStateMachine.parse("running"));
        assertThrows(BizException.class, () -> RoundStateMachine.parse("foo"));
        assertThrows(BizException.class, () -> RoundStateMachine.parse(null));
    }
}