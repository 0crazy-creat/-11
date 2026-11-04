package com.csp.awd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.awd.dto.AttackResultVO;
import com.csp.awd.dto.CheckResultVO;
import com.csp.awd.dto.ScoreVO;
import com.csp.awd.engine.AwdScoringEngine;
import com.csp.awd.engine.ScorePolicy;
import com.csp.awd.entity.AwdRound;
import com.csp.awd.mapper.AwdRoundMapper;
import com.csp.awd.enums.RoundStatus;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.common.util.FlagUtils;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.context.annotation.Lazy;

/**
 * AWD/AWDP 攻防计分服务。
 *
 * <h3>计分模型</h3>
 * <ul>
 *   <li><b>攻击得分</b>：提交对手靶机 flag 正确 +基础分；对同一目标重复攻破衰减；首杀额外奖励。</li>
 *   <li><b>SLA 扣分</b>：被攻破方按次扣分（有下限）。</li>
 *   <li><b>防御得分（AWDP）</b>：提交有效修补 +奖励；单轮零被攻破 +加成。</li>
 * </ul>
 *
 * <h3>并发与幂等</h3>
 * 每次攻击提交走 Redis 分布式锁 + 计数器：同一攻击方对同一目标同一题只计分一次。
 * 排行榜用 ZSet INCR 原子累加，避免读改写丢分。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwdScoreService {

    private static final Duration LOCK_TTL = Duration.ofSeconds(8);
    private static final Duration FLAG_TTL = Duration.ofHours(24);

    private final AwdScoringEngine engine;
    private final RedisService redisService;
    private final WsPublisher wsPublisher;
    private final AwdRoundMapper roundMapper;
    private final ScorePolicy scorePolicy;

    /** AWDP 防御分回退（修补后被攻破 0 分）；用 @Lazy 防止与防御服务初始化顺序耦合。 */
    @Lazy
    private final DefenseSubmissionService defenseSubmissionService;

    // ================================================================== 攻击

    /**
     * 攻击提交：校验 flag 归属，正确则计分。
     *
     * @param contestId    竞赛
     * @param attackerTeam 攻击方战队
     * @param victimTeam   被攻击方战队
     * @param questionId   题目（靶机）ID
     * @param submitted    提交的 flag
     * @param roundNo      轮次号；为空则取当前进行中轮次
     */
    public AttackResultVO submitAttack(Long contestId, Long attackerTeam, Long victimTeam,
                                       Long questionId, String submitted, Integer roundNo) {
        if (Objects.equals(attackerTeam, victimTeam)) {
            throw BizException.of("不能攻击自己的靶机");
        }

        AwdRound round = resolveRound(contestId, roundNo);
        RoundStatus st = parseStatus(round.getStatus());
        if (st != RoundStatus.RUNNING) {
            throw new BizException(ResultCode.BAD_REQUEST,
                    "当前轮次状态为 " + round.getStatus() + "，不接受攻击提交");
        }
        int rn = round.getRoundNo();

        String flag = FlagUtils.normalize(submitted);
        if (flag.isEmpty()) {
            throw BizException.of("flag 不能为空");
        }

        // 1) flag 归属校验
        if (!verifyFlag(contestId, rn, victimTeam, questionId, flag)) {
            log.info("攻击 flag 校验失败 contest={} attacker={} victim={} q={}",
                    contestId, attackerTeam, victimTeam, questionId);
            return result(false, "flag 不正确或已过期", 0, false, 0, contestId, attackerTeam);
        }

        // 2) 幂等 + 并发守卫
        String hitKey = AwdKeys.hit(contestId, rn, attackerTeam, victimTeam, questionId);
        String lockKey = hitKey + ":lock";
        String owner = "atk-" + System.nanoTime();
        if (!redisService.tryLock(lockKey, owner, LOCK_TTL)) {
            throw BizException.of("提交过于频繁，请稍后重试");
        }
        try {
            Long hits = redisService.increment(hitKey);
            if (hits != null && hits == 1L) {
                redisService.expire(hitKey, FLAG_TTL);
            }
            if (hits != null && hits > 1L) {
                return result(true, "flag 正确，但该目标已被你攻破过，本次不重复计分",
                        0, false, 0, contestId, attackerTeam);
            }

            // 3) 首杀：本轮该题该目标首个被攻破
            String fbKey = AwdKeys.firstBlood(contestId, rn, victimTeam, questionId);
            boolean firstBlood = redisService.tryLock(fbKey, String.valueOf(attackerTeam), FLAG_TTL);

            // 4) 攻击方计分（首次攻破，previousHits = 0）
            long gain = engine.attackGain(0, firstBlood);
            redisService.zIncrement(AwdKeys.attack(contestId), attackerTeam, gain);
            redisService.zIncrement(AwdKeys.total(contestId), attackerTeam, gain);
            redisService.zIncrement(AwdKeys.attackHits(contestId), attackerTeam, 1);

            // 5) 被攻击方 SLA 扣分
            long victimPenalty = applySlaPenalty(contestId, victimTeam);
            redisService.zIncrement(AwdKeys.compromised(contestId), victimTeam, 1);
            redisService.zIncrement(AwdKeys.compromisedRound(contestId, rn), victimTeam, 1);
            // 需求第4条：该队该题本轮已提交并通过验证的修补包 → 防御分归零（修补后仍被攻破 0 分）
            try {
                defenseSubmissionService.revokeDefenseOnCompromise(contestId, rn, victimTeam, questionId);
            } catch (Exception e) {
                log.warn("AWDP 防御分回退失败 contest={} round={} victim={} q={}: {}",
                        contestId, rn, victimTeam, questionId, e.getMessage());
            }

            long attackerTotal = totalScore(contestId, attackerTeam);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("roundNo", rn);
            payload.put("attackerTeamId", attackerTeam);
            payload.put("victimTeamId", victimTeam);
            payload.put("questionId", questionId);
            payload.put("gainedScore", gain);
            payload.put("firstBlood", firstBlood);
            payload.put("attackerTotal", attackerTotal);
            wsPublisher.attackEvent(contestId, payload);
            if (firstBlood) {
                wsPublisher.firstBlood(contestId, payload);
            }
            wsPublisher.scoreUpdate(contestId, Map.of("teamId", attackerTeam, "score", attackerTotal));

            log.info("攻击成功 contest={} round={} attacker={} victim={} q={} gain={} firstBlood={}",
                    contestId, rn, attackerTeam, victimTeam, questionId, gain, firstBlood);

            return result(true, firstBlood ? "首杀！flag 正确" : "flag 正确，攻击得分",
                    gain, firstBlood, victimPenalty, contestId, attackerTeam);
        } finally {
            redisService.unlock(lockKey);
        }
    }

    private AttackResultVO result(boolean success, String msg, long gain, boolean fb,
                                  long victimPenalty, Long contestId, Long attackerTeam) {
        return AttackResultVO.builder()
                .success(success)
                .message(msg)
                .gainedScore(gain)
                .firstBlood(fb)
                .victimPenalty(victimPenalty)
                .attackerTotalScore(totalScore(contestId, attackerTeam))
                .build();
    }

    // ================================================================== 防御 / AWDP

    /**
     * AWDP 修补奖励：提交有效修复说明后加防御分。
     * 同一轮同一题目只能获得一次修补分（防刷分）。
     */
    public long submitPatch(Long contestId, Long teamId, Integer roundNo, Long questionId, String description) {
        if (description == null || description.isBlank()) {
            throw BizException.of("修复说明不能为空");
        }
        String key = AwdKeys.patch(contestId, roundNo, teamId, questionId);
        // tryLock 即 setIfAbsent：首次写入返回 true
        boolean first = redisService.tryLock(key, "PATCHED", FLAG_TTL);
        if (!first) {
            throw BizException.of("本轮该题已获得过修补加分");
        }
        long bonus = engine.patchBonus();
        if (bonus > 0) {
            redisService.zIncrement(AwdKeys.defense(contestId), teamId, bonus);
            redisService.zIncrement(AwdKeys.total(contestId), teamId, bonus);
        }
        long total = totalScore(contestId, teamId);
        wsPublisher.scoreUpdate(contestId, Map.of("teamId", teamId, "score", total));
        log.info("AWDP 修补加分 contest={} round={} team={} q={} bonus={}",
                contestId, roundNo, teamId, questionId, bonus);
        return bonus;
    }

    /** 为被攻破方累计一次 SLA 扣分，返回本次扣分增量（负值）。 */
    private long applySlaPenalty(Long contestId, Long victimTeam) {
        long current = (long) readScore(AwdKeys.sla(contestId), victimTeam);
        long delta = engine.slaPenalty(current);
        if (delta != 0) {
            redisService.zIncrement(AwdKeys.sla(contestId), victimTeam, delta);
            redisService.zIncrement(AwdKeys.total(contestId), victimTeam, delta);
        }
        return delta;
    }

    // ================================================================== 结算

    /**
     * 轮次结算：AWDP 模式下给本轮零被攻破的战队发放防御加成，并推送最终排行。
     * 幂等：同一轮次重复调用不会重复发放（以结算标记 key 守卫）。
     */
    public void settleRound(Long contestId, Integer roundNo) {
        String settleKey = "csp:awd:settled:" + contestId + ":r" + roundNo;
        if (!redisService.tryLock(settleKey, "1", FLAG_TTL)) {
            log.info("轮次已结算，跳过 contest={} round={}", contestId, roundNo);
            return;
        }

        Set<Object> teams = teamsOf(contestId);
        if (teams.isEmpty()) {
            log.info("轮次结算跳过：无参赛队 contest={} round={}", contestId, roundNo);
            return;
        }

        long bonus = engine.cleanRoundBonus();
        int awarded = 0;
        if (bonus > 0) {
            for (Object teamObj : teams) {
                long tid = toLong(teamObj);
                Double compromised = redisService.zScore(AwdKeys.compromisedRound(contestId, roundNo), tid);
                if (compromised == null || compromised == 0) {
                    redisService.zIncrement(AwdKeys.defense(contestId), tid, bonus);
                    redisService.zIncrement(AwdKeys.total(contestId), tid, bonus);
                    awarded++;
                }
            }
        }
        log.info("轮次结算完成 contest={} round={} 参赛队={} 防御加成发放={} 每队+{}",
                contestId, roundNo, teams.size(), awarded, bonus);

        wsPublisher.scoreUpdate(contestId, leaderboard(contestId, Math.max(teams.size(), 50)));
    }

    // ================================================================== check 结算（SLA）

    /**
     * 按本轮 check 结果结算 SLA 扣分，写入 {@code awd_score}（总分榜与 SLA 分榜）。
     *
     * <h3>容错语义（对齐 Cardinal）</h3>
     * <ul>
     *   <li>只对<b>失败靶机所属队伍</b>扣分，通过 check 的队伍不受任何影响；</li>
     *   <li>任一队全部靶机 PASS 则本轮 0 扣分（仍记录一条 0 值明细便于审计）；</li>
     *   <li>单队处理异常被隔离，不影响其他队结算。</li>
     * </ul>
     *
     * <h3>幂等</h3>
     * 以 {@link AwdKeys#roundPhase(Long, int, String)} 中 {@code settle-check} 标记守卫：
     * 同一轮重复调用只结算一次，避免重复扣分。
     *
     * @param contestId 竞赛
     * @param roundNo   轮次
     * @param byTeam    check 结果（teamId -> 该队全部靶机结果）
     * @return 本队扣分明细（teamId -> 本轮 SLA 增量，负值或 0）
     */
    public Map<Long, Long> settleCheckResults(Long contestId, Integer roundNo,
                                              Map<Long, List<CheckResultVO>> byTeam) {
        Map<Long, Long> deltas = new LinkedHashMap<>();
        if (byTeam == null || byTeam.isEmpty()) {
            log.info("check 结算跳过：无 check 结果 contest={} round={}", contestId, roundNo);
            return deltas;
        }

        String guard = AwdKeys.roundPhase(contestId, roundNo, "settle-check");
        if (!redisService.tryLock(guard, "1", FLAG_TTL)) {
            log.info("check 结算已执行，跳过 contest={} round={}", contestId, roundNo);
            return deltas;
        }

        long perFail = Math.max(0, scorePolicy.getSlaPenaltyPerFailedCheck());
        int failedTeams = 0;
        long totalPenalty = 0;

        for (Map.Entry<Long, List<CheckResultVO>> e : byTeam.entrySet()) {
            Long teamId = e.getKey();
            long delta = 0;
            try {
                int fails = AwdCheckService.failCount(e.getValue());
                if (fails > 0 && perFail > 0) {
                    delta = -perFail * fails;
                    // SLA 下限保护：累计扣分不低于 floor
                    long currentSla = (long) readScore(AwdKeys.sla(contestId), teamId);
                    long floor = scorePolicy.getSlaPenaltyFloor();
                    if (floor < 0 && currentSla + delta < floor) {
                        delta = floor - currentSla;   // 只扣到下限为止
                    }
                }
                if (delta != 0) {
                    redisService.zIncrement(AwdKeys.sla(contestId), teamId, delta);
                    redisService.zIncrement(AwdKeys.total(contestId), teamId, delta);
                    redisService.zIncrement(AwdKeys.slaRound(contestId, roundNo), teamId, delta);
                    failedTeams++;
                    totalPenalty += delta;
                }
                // 明细落 Redis：按队存 JSON 数组
                redisService.hSet(AwdKeys.checkResults(contestId, roundNo), String.valueOf(teamId),
                        toJsonSafe(e.getValue()));
            } catch (Exception ex) {
                // 单队结算异常隔离
                log.warn("check 结算异常（已隔离） contest={} round={} team={}: {}",
                        contestId, roundNo, teamId, ex.toString());
            }
            deltas.put(teamId, delta);
        }

        log.info("check 结算完成 contest={} round={} 队伍={} 失败队={} SLA 总扣分={} 每失败靶机扣={}",
                contestId, roundNo, byTeam.size(), failedTeams, totalPenalty, perFail);

        // 推送排行榜（失败队分数已变化）
        try {
            wsPublisher.scoreUpdate(contestId, leaderboard(contestId, 100));
        } catch (Exception ex) {
            log.warn("check 结算推送排行榜失败 contest={} round={}: {}", contestId, roundNo, ex.toString());
        }
        return deltas;
    }

    private String toJsonSafe(Object o) {
        try {
            return OBJECT_MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            return "[]";
        }
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ================================================================== 查询

    /** 单队积分明细。 */
    public ScoreVO teamScore(Long contestId, Long teamId) {
        long attack = (long) readScore(AwdKeys.attack(contestId), teamId);
        long defense = (long) readScore(AwdKeys.defense(contestId), teamId);
        long sla = (long) readScore(AwdKeys.sla(contestId), teamId);
        long compromised = (long) readScore(AwdKeys.compromised(contestId), teamId);
        long hits = (long) readScore(AwdKeys.attackHits(contestId), teamId);
        Long rank = redisService.zRank(AwdKeys.total(contestId), teamId, true);
        return ScoreVO.builder()
                .contestId(contestId)
                .teamId(teamId)
                .score(engine.total(attack, defense, sla))
                .attackScore(attack)
                .defenseScore(defense)
                .slaPenalty(sla)
                .compromisedCount(compromised)
                .attackSuccessCount(hits)
                .rank(rank)
                .build();
    }

    /** 排行榜（按总分降序，实时分数取自 ZSet）。 */
    public List<ScoreVO> leaderboard(Long contestId, int topN) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisService.zTop(AwdKeys.total(contestId), Math.max(1, topN));
        List<ScoreVO> out = new ArrayList<>();
        if (tuples == null) return out;
        long rank = 1;
        for (ZSetOperations.TypedTuple<Object> t : tuples) {
            if (t.getValue() == null) continue;
            long tid = toLong(t.getValue());
            ScoreVO vo = teamScore(contestId, tid);
            vo.setRank(rank++);
            if (t.getScore() != null) {
                vo.setScore(t.getScore().longValue());
            }
            out.add(vo);
        }
        return out;
    }

    /** 战队总分。 */
    public long totalScore(Long contestId, Long teamId) {
        return (long) readScore(AwdKeys.total(contestId), teamId);
    }

    /** 参赛队集合（各分项 ZSet 并集）。 */
    public Set<Object> teamsOf(Long contestId) {
        Set<Object> all = new LinkedHashSet<>();
        for (String key : List.of(AwdKeys.total(contestId), AwdKeys.attack(contestId),
                AwdKeys.defense(contestId), AwdKeys.compromised(contestId))) {
            Set<ZSetOperations.TypedTuple<Object>> tuples = redisService.zTop(key, 10000);
            if (tuples == null) continue;
            for (ZSetOperations.TypedTuple<Object> t : tuples) {
                if (t.getValue() != null) all.add(t.getValue());
            }
        }
        return all;
    }

    /** 登记参赛队（开赛时为每队初始化 0 分，保证排行榜可见）。 */
    public void registerTeam(Long contestId, Long teamId) {
        redisService.zAdd(AwdKeys.total(contestId), teamId, 0);
    }

    // ================================================================== flag

    /** 生成并缓存某队某题的动态 flag（幂等：已存在直接返回）。 */
    public String issueFlag(Long contestId, Integer roundNo, Long teamId, Long questionId, String prefix) {
        String key = AwdKeys.flagOne(contestId, roundNo, teamId, questionId);
        String cached = redisService.get(key);
        if (cached != null) return cached;
        String flag = FlagUtils.dynamicFlag(prefix == null || prefix.isBlank() ? "awd" : prefix,
                teamId, roundNo);
        redisService.set(key, flag, FLAG_TTL);
        redisService.hSet(AwdKeys.roundFlags(contestId, roundNo),
                teamId + ":" + questionId, flag);
        return flag;
    }

    /** 为整个竞赛某轮所有战队批量注入 flag（开局编排调用）。 */
    public int issueFlagsForRound(Long contestId, Integer roundNo, Collection<Long> teamIds,
                                  Collection<Long> questionIds, String prefix) {
        int n = 0;
        for (Long teamId : teamIds) {
            for (Long qid : questionIds) {
                issueFlag(contestId, roundNo, teamId, qid, prefix);
                n++;
            }
        }
        log.info("flag 注入完成 contest={} round={} 共 {} 条", contestId, roundNo, n);
        return n;
    }

    /** 校验提交的 flag 是否匹配。 */
    private boolean verifyFlag(Long contestId, Integer roundNo, Long teamId, Long questionId, String submitted) {
        String expected = redisService.get(AwdKeys.flagOne(contestId, roundNo, teamId, questionId));
        if (expected == null) {
            log.debug("flag 未签发 contest={} round={} team={} q={}", contestId, roundNo, teamId, questionId);
            return false;
        }
        return FlagUtils.safeEquals(expected, submitted);
    }

    // ================================================================== 内部工具

    /**
     * 解析目标轮次：指定轮次则精确取，否则取进行中轮次，再退化为最近一轮。
     *
     * <p>此处直接用 Mapper 查询，避免与 AwdRoundService 形成构造器循环依赖
     * （轮次推进需要调用本服务结算，本服务也需要轮次信息）。</p>
     */
    private AwdRound resolveRound(Long contestId, Integer roundNo) {
        if (roundNo != null) {
            AwdRound r = roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                    .eq(AwdRound::getContestId, contestId)
                    .eq(AwdRound::getRoundNo, roundNo)
                    .last("LIMIT 1"));
            if (r == null) throw BizException.notFound("轮次 " + contestId + "#" + roundNo);
            return r;
        }
        AwdRound running = roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                .eq(AwdRound::getContestId, contestId)
                .eq(AwdRound::getStatus, RoundStatus.RUNNING.name())
                .orderByDesc(AwdRound::getRoundNo)
                .last("LIMIT 1"));
        if (running != null) return running;
        AwdRound latest = roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                .eq(AwdRound::getContestId, contestId)
                .orderByDesc(AwdRound::getRoundNo)
                .last("LIMIT 1"));
        if (latest == null) throw BizException.of("该竞赛尚未编排 AWD 轮次");
        return latest;
    }

    private double readScore(String key, Long teamId) {
        Double d = redisService.zScore(key, teamId);
        return d == null ? 0 : d;
    }

    private long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0L; }
    }

    private RoundStatus parseStatus(String s) {
        try { return s == null ? null : RoundStatus.valueOf(s); }
        catch (IllegalArgumentException e) { return null; }
    }

    /** 轮次剩余秒数（展示用）。 */
    public long remainingSeconds(AwdRound r) {
        if (r == null || r.getEndAt() == null) return 0;
        return Math.max(0, Duration.between(LocalDateTime.now(), r.getEndAt()).getSeconds());
    }
}