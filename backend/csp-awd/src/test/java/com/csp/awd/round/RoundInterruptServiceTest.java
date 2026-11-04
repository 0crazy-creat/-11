package com.csp.awd.round;

import com.csp.awd.entity.AwdRound;
import com.csp.awd.round.entity.AwdRoundDispute;
import com.csp.awd.round.mapper.AwdRoundDisputeMapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 轮次中断测试：软中断冻结语义、硬中断中止并结算、恢复、重复中断保护。
 *
 * <p>纯 Mockito 单测，不启动 Spring、不连 Redis/MySQL。</p>
 */
class RoundInterruptServiceTest {

    private RoundControlService controlService;
    private RedisService redisService;
    private WsPublisher wsPublisher;
    private com.csp.awd.service.AwdScoreService scoreService;
    private RoundInterruptService service;

    private static final long CID = 9001L;
    private static final int RNO = 1;

    @BeforeEach
    void setUp() {
        controlService = mock(RoundControlService.class);
        redisService = mock(RedisService.class);
        wsPublisher = mock(WsPublisher.class);
        scoreService = mock(com.csp.awd.service.AwdScoreService.class);
        service = new RoundInterruptService(controlService, redisService, wsPublisher, scoreService);
    }

    private AwdRound round(String status) {
        AwdRound r = new AwdRound();
        r.setId(1L);
        r.setContestId(CID);
        r.setRoundNo(RNO);
        r.setStatus(status);
        return r;
    }

    private void stubView() {
        when(controlService.view(anyLong(), anyInt())).thenReturn(new java.util.LinkedHashMap<>());
    }

    // ================================================================ 软中断

    @Test
    @DisplayName("软中断：不迁移状态，只写 frozen 标记，保留已提交成绩")
    void softInterruptFreezesWithoutTransition() {
        AwdRound r = round("RUNNING");
        when(controlService.requireRound(CID, RNO)).thenReturn(r);
        when(controlService.statusOf(r)).thenReturn(RoundStatus.RUNNING);
        stubView();

        Map<String, Object> out = service.softInterrupt(CID, RNO, "靶机网络抖动排查", 7L, "judge01");

        // 关键断言：没有调用任何状态迁移（软中断不改变状态）
        verify(controlService, never()).casStatus(any(), any(), any(), any(), any());
        // frozen 标记写入 Redis 控制 Hash
        verify(redisService).hSet(contains("round:ctl"), eq("frozen"), eq("true"));
        verify(redisService).hSet(contains("round:ctl"), eq("frozenReason"), contains("靶机网络抖动排查"));
        // 中断原因落库，类型 SOFT
        verify(controlService).writeInterruptReason(eq(r), eq("SOFT"), contains("靶机网络抖动排查"));
        // 审计与 WS 推送
        verify(controlService).audit(eq(CID), eq(RNO), eq("INTERRUPT_SOFT"), isNull(), eq(7L), eq("judge01"), anyString());
        verify(wsPublisher).publish(eq("ROUND_INTERRUPT"), eq(CID), any());

        assertEquals(Boolean.TRUE, out.get("frozen"));
        assertEquals("SOFT", out.get("interruptType"));
        assertEquals(RoundStatus.RUNNING.name(), r.getStatus(), "软中断后状态必须仍是 RUNNING");
    }

    @Test
    @DisplayName("软中断：终态轮次被拒绝（409）")
    void softInterruptRejectedOnTerminal() {
        AwdRound r = round("FINISHED");
        when(controlService.requireRound(CID, RNO)).thenReturn(r);
        when(controlService.statusOf(r)).thenReturn(RoundStatus.FINISHED);

        BizException e = assertThrows(BizException.class,
                () -> service.softInterrupt(CID, RNO, "赛后补操作", 7L, "judge01"));
        assertEquals(ResultCode.CONFLICT.getCode(), e.getCode());
        verify(redisService, never()).hSet(anyString(), eq("frozen"), any());
    }

    @Test
    @DisplayName("软中断：原因为空被拒绝（400）")
    void softInterruptRequiresReason() {
        BizException e = assertThrows(BizException.class,
                () -> service.softInterrupt(CID, RNO, "  ", 7L, "judge01"));
        assertEquals(ResultCode.BAD_REQUEST.getCode(), e.getCode());
        verifyNoInteractions(redisService, wsPublisher);
    }

    @Test
    @DisplayName("恢复：未冻结时拒绝；冻结时清除标记")
    void resumeBehaviour() {
        AwdRound r = round("RUNNING");
        when(controlService.requireRound(CID, RNO)).thenReturn(r);
        when(controlService.statusOf(r)).thenReturn(RoundStatus.RUNNING);

        // 未冻结 → 409
        when(controlService.isFrozen(CID, RNO)).thenReturn(false);
        BizException e = assertThrows(BizException.class, () -> service.resume(CID, RNO, 7L, "judge01"));
        assertEquals(ResultCode.CONFLICT.getCode(), e.getCode());

        // 已冻结 → 清除
        when(controlService.isFrozen(CID, RNO)).thenReturn(true);
        stubView();
        Map<String, Object> out = service.resume(CID, RNO, 7L, "judge01");
        verify(redisService).hDelete(contains("round:ctl"), eq("frozen"), eq("frozenReason"), any(), any());
        verify(controlService).audit(eq(CID), eq(RNO), eq("RESUME"), isNull(), eq(7L), eq("judge01"), anyString());
        assertEquals(Boolean.FALSE, out.get("frozen"));
    }

    // ================================================================ 硬中断

    @Test
    @DisplayName("硬中断：RUNNING → ABORTED，写中断原因并尽力结算")
    void hardInterruptAbortsAndSettles() {
        AwdRound r = round("RUNNING");
        when(controlService.requireRound(CID, RNO)).thenReturn(r);
        when(controlService.statusOf(r)).thenReturn(RoundStatus.RUNNING);
        stubView();

        Map<String, Object> out = service.hardInterrupt(CID, RNO, "靶场机器批量失联", 9L, "judge02");

        // 状态迁移到 ABORTED，类型 HARD
        verify(controlService).casStatus(eq(r), eq(RoundStatus.RUNNING), eq(RoundStatus.ABORTED), eq("HARD"), isNull());
        verify(controlService).writeInterruptReason(eq(r), eq("HARD"), contains("靶场机器批量失联"));
        // 清除软中断标记
        verify(redisService).hDelete(contains("round:ctl"), eq("frozen"), eq("frozenReason"));
        // 结算被调用
        verify(scoreService).settleRound(CID, RNO);
        // 审计 + WS
        verify(controlService).audit(eq(CID), eq(RNO), eq("INTERRUPT_HARD"), isNull(), eq(9L), eq("judge02"), contains("ABORTED"));
        verify(wsPublisher).publish(eq("ROUND_INTERRUPT"), eq(CID), any());

        assertEquals(Boolean.TRUE, out.get("settled"));
        assertEquals("HARD", out.get("interruptType"));
        assertEquals("ABORTED", out.get("to"));
    }

    @Test
    @DisplayName("硬中断：结算抛异常不阻断中止（中止优先级高于结算）")
    void hardInterruptSurvivesSettleFailure() {
        AwdRound r = round("RUNNING");
        when(controlService.requireRound(CID, RNO)).thenReturn(r);
        when(controlService.statusOf(r)).thenReturn(RoundStatus.RUNNING);
        doThrow(new RuntimeException("Redis 超时")).when(scoreService).settleRound(CID, RNO);
        stubView();

        Map<String, Object> out = assertDoesNotThrow(
                () -> service.hardInterrupt(CID, RNO, "紧急中止", 9L, "judge02"));

        // 中止事实已落库
        verify(controlService).casStatus(eq(r), eq(RoundStatus.RUNNING), eq(RoundStatus.ABORTED), eq("HARD"), isNull());
        assertEquals(Boolean.FALSE, out.get("settled"));
        assertEquals("Redis 超时", out.get("settleError"));
        assertEquals("ABORTED", out.get("to"));
    }

    @Test
    @DisplayName("硬中断：终态轮次不可再次中止（409），且不触发结算")
    void hardInterruptRejectedOnTerminal() {
        AwdRound r = round("ABORTED");
        when(controlService.requireRound(CID, RNO)).thenReturn(r);
        when(controlService.statusOf(r)).thenReturn(RoundStatus.ABORTED);

        BizException e = assertThrows(BizException.class,
                () -> service.hardInterrupt(CID, RNO, "重复中止", 9L, "judge02"));
        assertEquals(ResultCode.CONFLICT.getCode(), e.getCode());
        verify(scoreService, never()).settleRound(anyLong(), anyInt());
        verify(controlService, never()).casStatus(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("硬中断：原因为空被拒绝（400）")
    void hardInterruptRequiresReason() {
        BizException e = assertThrows(BizException.class,
                () -> service.hardInterrupt(CID, RNO, null, 9L, "judge02"));
        assertEquals(ResultCode.BAD_REQUEST.getCode(), e.getCode());
    }

    // ================================================================ 统一入口分发

    @Test
    @DisplayName("interrupt 按 type 分发，非法类型被拒绝")
    void interruptDispatch() {
        AwdRound r = round("RUNNING");
        when(controlService.requireRound(CID, RNO)).thenReturn(r);
        when(controlService.statusOf(r)).thenReturn(RoundStatus.RUNNING);
        stubView();

        // SOFT 分支
        assertDoesNotThrow(() -> service.interrupt(CID, RNO, "soft", "原因A", 1L, "op"));
        // HARD 分支
        assertDoesNotThrow(() -> service.interrupt(CID, RNO, "HARD", "原因B", 1L, "op"));
        // 非法类型
        BizException e = assertThrows(BizException.class,
                () -> service.interrupt(CID, RNO, "PANIC", "原因", 1L, "op"));
        assertTrue(e.getMessage().contains("非法的中断类型"));
    }

    // ================================================================ 申诉回滚

    @Test
    @DisplayName("申诉回滚：本轮 SLA 扣分被加回总分与累计 SLA，且本轮 SLA 清零防重复")
    void rollbackPenaltyRestoresScore() {
        DisputeService ds = new DisputeService(
                mock(AwdRoundDisputeMapper.class), controlService, redisService, wsPublisher);

        // 本轮该队 SLA 为 -30（扣分）
        when(redisService.zScore(contains("sla:r1"), eq(55L))).thenReturn(-30.0);

        BigDecimal delta = ds.rollbackPenalty(CID, RNO, 55L);

        assertEquals(0, delta.compareTo(BigDecimal.valueOf(30)), "回滚分值应为 30");
        // 累计 SLA 与总分各 +30
        verify(redisService).zIncrement(contains("csp:awd:sla:"), eq(55L), eq(30.0));
        verify(redisService).zIncrement(contains("csp:awd:total:"), eq(55L), eq(30.0));
        // 本轮 SLA 清零
        verify(redisService).zAdd(contains("sla:r1"), eq(55L), eq(0.0));
    }

    @Test
    @DisplayName("申诉回滚：本轮无扣分时回滚 0，不写分数")
    void rollbackPenaltyNoopWhenNoPenalty() {
        DisputeService ds = new DisputeService(
                mock(AwdRoundDisputeMapper.class), controlService, redisService, wsPublisher);

        when(redisService.zScore(contains("sla:r1"), eq(55L))).thenReturn(null);

        BigDecimal delta = ds.rollbackPenalty(CID, RNO, 55L);

        assertEquals(0, delta.compareTo(BigDecimal.ZERO));
        verify(redisService, never()).zIncrement(anyString(), any(), anyDouble());
    }

    @Test
    @DisplayName("申诉仲裁：非 PENDING 的申诉不可重复仲裁")
    void arbitrateRejectsNonPending() {
        AwdRoundDisputeMapper mapper = mock(AwdRoundDisputeMapper.class);
        DisputeService ds = new DisputeService(mapper, controlService, redisService, wsPublisher);

        AwdRoundDispute d = new AwdRoundDispute();
        d.setId(100L);
        d.setContestId(CID);
        d.setRoundNo(RNO);
        d.setTeamId(55L);
        d.setStatus(DisputeService.ST_APPROVED); // 已仲裁
        d.setDeleted(0);
        when(mapper.selectById(100L)).thenReturn(d);

        BizException e = assertThrows(BizException.class,
                () -> ds.arbitrate(CID, RNO, 100L, true, "同意", 1L, "judge"));
        assertEquals(ResultCode.CONFLICT.getCode(), e.getCode());
        verify(mapper, never()).updateById(any(AwdRoundDispute.class));
    }

    @Test
    @DisplayName("申诉仲裁 approve：回滚扣分并写入 rollback_score + 审计")
    void arbitrateApproveRollsBack() {
        AwdRoundDisputeMapper mapper = mock(AwdRoundDisputeMapper.class);
        DisputeService ds = new DisputeService(mapper, controlService, redisService, wsPublisher);

        AwdRoundDispute d = new AwdRoundDispute();
        d.setId(101L);
        d.setContestId(CID);
        d.setRoundNo(RNO);
        d.setTeamId(55L);
        d.setDisputeType("SLA");
        d.setStatus(DisputeService.ST_PENDING);
        d.setDeleted(0);
        when(mapper.selectById(101L)).thenReturn(d);
        when(redisService.zScore(contains("sla:r1"), eq(55L))).thenReturn(-20.0);

        Map<String, Object> out = ds.arbitrate(CID, RNO, 101L, true, "check 误判，予以回滚", 3L, "chief-judge");

        assertEquals(DisputeService.ST_APPROVED, out.get("status"));
        assertEquals(Boolean.TRUE, out.get("approved"));
        assertEquals(0, ((BigDecimal) out.get("rollbackScore")).compareTo(BigDecimal.valueOf(20)));
        // 分数回滚
        verify(redisService).zIncrement(contains("csp:awd:total:"), eq(55L), eq(20.0));
        // 审计记录 ARBITRATE
        verify(controlService).audit(eq(CID), eq(RNO), eq("ARBITRATE"), eq(101L), eq(3L), eq("chief-judge"), contains("通过"));
        // 落库更新
        verify(mapper).updateById(argThat((AwdRoundDispute x) ->
                DisputeService.ST_APPROVED.equals(x.getStatus()) && x.getRollbackScore() != null));
    }

    @Test
    @DisplayName("申诉仲裁 reject：只记意见，不动分数")
    void arbitrateRejectDoesNotTouchScore() {
        AwdRoundDisputeMapper mapper = mock(AwdRoundDisputeMapper.class);
        DisputeService ds = new DisputeService(mapper, controlService, redisService, wsPublisher);

        AwdRoundDispute d = new AwdRoundDispute();
        d.setId(102L);
        d.setContestId(CID);
        d.setRoundNo(RNO);
        d.setTeamId(55L);
        d.setDisputeType("SCORE");
        d.setStatus(DisputeService.ST_PENDING);
        d.setDeleted(0);
        when(mapper.selectById(102L)).thenReturn(d);

        Map<String, Object> out = ds.arbitrate(CID, RNO, 102L, false, "证据不足", 3L, "chief-judge");

        assertEquals(DisputeService.ST_REJECTED, out.get("status"));
        assertEquals(Boolean.FALSE, out.get("approved"));
        verify(redisService, never()).zIncrement(anyString(), any(), anyDouble());
        verify(controlService).audit(eq(CID), eq(RNO), eq("ARBITRATE"), eq(102L), eq(3L), eq("chief-judge"), contains("驳回"));
    }
}