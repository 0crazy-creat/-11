package com.csp.awd.round;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.awd.entity.AwdRound;
import com.csp.awd.round.entity.AwdRoundDispute;
import com.csp.awd.round.mapper.AwdRoundDisputeMapper;
import com.csp.awd.service.AwdKeys;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 申诉仲裁服务：选手提交申诉 → JUDGE 仲裁 approve / reject → 通过则回滚该队本轮扣分并记审计。
 *
 * <p>仲裁语义（对齐 Cardinal 的 dispute 机制）：</p>
 * <ul>
 *   <li><b>提交</b>：轮次必须存在；同一队同一轮针对同一题目只允许一条 PENDING 申诉（防刷）。</li>
 *   <li><b>approve</b>：回滚该队本轮扣分——把本轮 SLA 扣分从累计 SLA ZSet 中加回，
 *       同时把总分补回；回滚分值写入 {@code rollback_score}，并记审计。</li>
 *   <li><b>reject</b>：仅记录仲裁意见，不动分数。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeService {

    public static final String ST_PENDING = "PENDING";
    public static final String ST_APPROVED = "APPROVED";
    public static final String ST_REJECTED = "REJECTED";

    private final AwdRoundDisputeMapper disputeMapper;
    private final RoundControlService controlService;
    private final RedisService redisService;
    private final WsPublisher wsPublisher;

    // ================================================================== 提交申诉

    /**
     * 选手提交申诉。
     *
     * @param contestId   竞赛
     * @param roundNo     轮次
     * @param teamId      申诉队伍（服务端从登录用户解析，不信任前端传值）
     * @param questionId  关联题目，null 表示整轮
     * @param disputeType SCORE / SLA / FLAG / OTHER
     * @param reason      申诉理由
     * @param evidence    证据
     * @param applicantId 申请人用户ID
     */
    /** 从赛事战队成员关系解析当前用户所属战队（一人多队时取最早加入的一支）。 */
    public Long resolveMyTeamId(Long contestId, Long userId) {
        try {
            List<java.util.Map<String, Object>> rows = disputeMapper.selectTeamIdByUser(contestId, userId);
            if (rows == null || rows.isEmpty()) return null;
            Object v = rows.get(0).get("team_id");
            return v == null ? null : Long.valueOf(String.valueOf(v));
        } catch (Exception e) {
            log.warn("解析战队失败 contest={} user={}", contestId, userId, e);
            return null;
        }
    }

    /** 校验该队伍是否属于当前用户（防越权代提）。 */
    public boolean isMyTeam(Long contestId, Long teamId, Long userId) {
        try {
            Long mine = resolveMyTeamId(contestId, userId);
            return mine != null && mine.equals(teamId);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public AwdRoundDispute submit(Long contestId, Integer roundNo, Long teamId, Long questionId,
                                  String disputeType, String reason, String evidence, Long applicantId) {
        if (teamId == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "申诉队伍不能为空");
        }
        if (reason == null || reason.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "申诉理由不能为空");
        }
        AwdRound round = controlService.requireRound(contestId, roundNo);
        RoundStatus st = controlService.statusOf(round);
        if (st == RoundStatus.PENDING) {
            throw new BizException(ResultCode.CONFLICT, "轮次尚未开始，无法申诉");
        }

        // 防刷：同队同轮同题只允许一条 PENDING
        Long pending = disputeMapper.selectCount(new LambdaQueryWrapper<AwdRoundDispute>()
                .eq(AwdRoundDispute::getContestId, contestId)
                .eq(AwdRoundDispute::getRoundNo, roundNo)
                .eq(AwdRoundDispute::getTeamId, teamId)
                .eq(questionId != null, AwdRoundDispute::getQuestionId, questionId)
                .isNull(questionId == null, AwdRoundDispute::getQuestionId)
                .eq(AwdRoundDispute::getStatus, ST_PENDING)
                .eq(AwdRoundDispute::getDeleted, 0));
        if (pending != null && pending > 0) {
            throw new BizException(ResultCode.CONFLICT, "该队本轮已存在待仲裁的申诉，请等待裁判处理");
        }

        AwdRoundDispute d = new AwdRoundDispute();
        d.setContestId(contestId);
        d.setRoundNo(roundNo);
        d.setTeamId(teamId);
        d.setQuestionId(questionId);
        d.setDisputeType(disputeType == null || disputeType.isBlank() ? "SCORE" : disputeType.trim().toUpperCase());
        d.setReason(reason);
        d.setEvidence(evidence);
        d.setStatus(ST_PENDING);
        d.setApplicantId(applicantId);
        d.setCreateTime(LocalDateTime.now());
        d.setUpdateTime(LocalDateTime.now());
        d.setDeleted(0);
        disputeMapper.insert(d);

        // Redis 索引，便于轮次内快速枚举
        redisService.hSet(AwdKeys.appeals(contestId, roundNo), String.valueOf(d.getId()),
                Map.of("teamId", teamId, "type", d.getDisputeType(), "status", ST_PENDING));

        controlService.audit(contestId, roundNo, "DISPUTE", d.getId(), applicantId, null,
                "队伍 " + teamId + " 提交申诉：" + d.getDisputeType());
        log.info("申诉已提交 contest={} round={} team={} disputeId={}", contestId, roundNo, teamId, d.getId());
        return d;
    }

    // ================================================================== 仲裁

    /**
     * JUDGE 仲裁。
     *
     * @param contestId   竞赛
     * @param roundNo     轮次
     * @param disputeId   申诉ID
     * @param approve     true=通过（回滚扣分），false=驳回
     * @param note        仲裁意见
     * @param arbiterId   仲裁人用户ID
     * @param arbiterName 仲裁人用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> arbitrate(Long contestId, Integer roundNo, Long disputeId,
                                         boolean approve, String note,
                                         Long arbiterId, String arbiterName) {
        AwdRoundDispute d = disputeMapper.selectById(disputeId);
        if (d == null || Integer.valueOf(1).equals(d.getDeleted())) {
            throw BizException.notFound("申诉 " + disputeId);
        }
        if (!contestId.equals(d.getContestId()) || !roundNo.equals(d.getRoundNo())) {
            throw new BizException(ResultCode.BAD_REQUEST, "申诉不属于该轮次");
        }
        if (!ST_PENDING.equals(d.getStatus())) {
            throw new BizException(ResultCode.CONFLICT,
                    "申诉已仲裁（当前状态 " + d.getStatus() + "），不可重复处理");
        }

        BigDecimal rollback = BigDecimal.ZERO;
        String newStatus;
        if (approve) {
            rollback = rollbackPenalty(contestId, roundNo, d.getTeamId());
            newStatus = ST_APPROVED;
        } else {
            newStatus = ST_REJECTED;
        }

        d.setStatus(newStatus);
        d.setArbiterId(arbiterId);
        d.setArbitrateNote(note);
        d.setArbitratedAt(LocalDateTime.now());
        d.setRollbackScore(rollback);
        d.setUpdateTime(LocalDateTime.now());
        disputeMapper.updateById(d);

        // 同步 Redis 申诉索引状态
        redisService.hSet(AwdKeys.appeals(contestId, roundNo), String.valueOf(d.getId()),
                Map.of("teamId", d.getTeamId(), "type", d.getDisputeType(), "status", newStatus));

        controlService.audit(contestId, roundNo, "ARBITRATE", d.getId(), arbiterId, arbiterName,
                (approve ? "通过" : "驳回") + "申诉 " + disputeId + "，回滚分值=" + rollback
                        + (note == null ? "" : "，意见：" + note));

        wsPublisher.publish("DISPUTE_ARBITRATED", contestId, Map.of(
                "contestId", contestId, "roundNo", roundNo, "disputeId", disputeId,
                "teamId", d.getTeamId(), "approve", approve, "rollbackScore", rollback));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("disputeId", disputeId);
        m.put("status", newStatus);
        m.put("approved", approve);
        m.put("rollbackScore", rollback);
        m.put("teamId", d.getTeamId());
        m.put("arbitratedAt", d.getArbitratedAt());
        return m;
    }

    /**
     * 回滚该队本轮扣分：把本轮 SLA 扣分（负值）从累计 SLA 与总分里加回。
     *
     * <p>SLA 扣分在 {@code csp:awd:sla:{contestId}} 与 {@code csp:awd:sla:r{n}:{contestId}} 以负值累计，
     * 回滚即取该队本轮 SLA 分数，取反后加到累计总分与累计 SLA 上，并把本轮 SLA 清零，防止重复回滚。</p>
     *
     * @return 实际回滚的分值（正数表示补回的分）
     */
    BigDecimal rollbackPenalty(Long contestId, Integer roundNo, Long teamId) {
        Double roundSla = redisService.zScore(AwdKeys.slaRound(contestId, roundNo), teamId);
        if (roundSla == null || roundSla == 0) {
            log.info("申诉通过但无可回滚 SLA 扣分 contest={} round={} team={}", contestId, roundNo, teamId);
            return BigDecimal.ZERO;
        }
        // roundSla 为负值（扣分），回滚 = 取反
        double delta = -roundSla;
        redisService.zIncrement(AwdKeys.sla(contestId), teamId, delta);
        redisService.zIncrement(AwdKeys.total(contestId), teamId, delta);
        // 清零本轮 SLA，保证幂等（重复仲裁已被状态机拦截，这里是二重保险）
        redisService.zAdd(AwdKeys.slaRound(contestId, roundNo), teamId, 0);

        log.info("申诉回滚成功 contest={} round={} team={} delta={}", contestId, roundNo, teamId, delta);
        return BigDecimal.valueOf(delta);
    }

    // ================================================================== 查询

    /** 轮次内全部申诉（按创建时间倒序）。 */
    public List<AwdRoundDispute> listByRound(Long contestId, Integer roundNo) {
        return disputeMapper.selectList(new LambdaQueryWrapper<AwdRoundDispute>()
                .eq(AwdRoundDispute::getContestId, contestId)
                .eq(AwdRoundDispute::getRoundNo, roundNo)
                .eq(AwdRoundDispute::getDeleted, 0)
                .orderByDesc(AwdRoundDispute::getCreateTime));
    }

    /** 某队在某轮的申诉。 */
    public List<AwdRoundDispute> listByTeam(Long contestId, Integer roundNo, Long teamId) {
        return disputeMapper.selectList(new LambdaQueryWrapper<AwdRoundDispute>()
                .eq(AwdRoundDispute::getContestId, contestId)
                .eq(AwdRoundDispute::getRoundNo, roundNo)
                .eq(AwdRoundDispute::getTeamId, teamId)
                .eq(AwdRoundDispute::getDeleted, 0)
                .orderByDesc(AwdRoundDispute::getCreateTime));
    }
}