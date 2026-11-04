package com.csp.awd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.awd.dto.CreateRoundRequest;
import com.csp.awd.dto.RoundVO;
import com.csp.awd.engine.RoundStateMachine;
import com.csp.awd.entity.AwdRound;
import com.csp.awd.enums.RoundStatus;
import com.csp.awd.mapper.AwdRoundMapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AWD 轮次编排服务：轮次创建、状态机流转、自动推进。
 *
 * <p>状态机唯一权威在 {@link RoundStateMachine}，本服务只做持久化与并发守卫。
 * 每次迁移均使用 Redis 分布式锁 + 乐观条件更新（status 作为 CAS 期望值），
 * 保证多实例部署下同一轮次不会被重复推进。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwdRoundService {

    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final String LOCK_PREFIX = "csp:awd:round:lock:";

    private final AwdRoundMapper roundMapper;
    private final RedisService redisService;
    private final WsPublisher wsPublisher;
    private final AwdScoreService scoreService;
    private final AwdTargetService targetService;
    private final AwdCheckService checkService;

    // ------------------------------------------------------------------ 查询

    public AwdRound requireRound(Long contestId, Integer roundNo) {
        AwdRound r = roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                .eq(AwdRound::getContestId, contestId)
                .eq(AwdRound::getRoundNo, roundNo)
                .last("LIMIT 1"));
        if (r == null) {
            throw BizException.notFound("轮次 " + contestId + "#" + roundNo);
        }
        return r;
    }

    public List<RoundVO> listRounds(Long contestId) {
        List<AwdRound> list = roundMapper.selectList(new LambdaQueryWrapper<AwdRound>()
                .eq(AwdRound::getContestId, contestId)
                .orderByAsc(AwdRound::getRoundNo));
        return list.stream().map(this::toVO).toList();
    }

    /** 当前进行中的轮次（无则返回 null）。 */
    public AwdRound currentRunning(Long contestId) {
        return roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                .eq(AwdRound::getContestId, contestId)
                .in(AwdRound::getStatus, RoundStatus.RUNNING.name(), RoundStatus.CHECKING.name())
                .orderByDesc(AwdRound::getRoundNo)
                .last("LIMIT 1"));
    }

    /** 用于攻击提交时定位轮次：优先进行中，否则最近已结束的一轮。 */
    public AwdRound activeOrLatest(Long contestId) {
        AwdRound running = currentRunning(contestId);
        if (running != null) return running;
        return roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                .eq(AwdRound::getContestId, contestId)
                .orderByDesc(AwdRound::getRoundNo)
                .last("LIMIT 1"));
    }

    public RoundVO toVO(AwdRound r) {
        RoundStatus st = parseSafe(r.getStatus());
        Long remaining = null;
        if (st == RoundStatus.RUNNING && r.getEndAt() != null) {
            long s = Duration.between(LocalDateTime.now(), r.getEndAt()).getSeconds();
            remaining = Math.max(0, s);
        }
        return RoundVO.builder()
                .id(r.getId())
                .contestId(r.getContestId())
                .roundNo(r.getRoundNo())
                .status(r.getStatus())
                .startAt(r.getStartAt())
                .endAt(r.getEndAt())
                .durationS(r.getDurationS())
                .remainingSeconds(remaining)
                .build();
    }

    // ------------------------------------------------------------------ 编排

    /**
     * 创建轮次（PENDING）。若已存在同号轮次则直接返回，保证幂等。
     */
    @Transactional(rollbackFor = Exception.class)
    public RoundVO createRound(CreateRoundRequest req) {
        AwdRound exists = roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                .eq(AwdRound::getContestId, req.getContestId())
                .eq(AwdRound::getRoundNo, req.getRoundNo())
                .last("LIMIT 1"));
        if (exists != null) {
            log.info("轮次已存在，跳过创建 contest={} round={}", req.getContestId(), req.getRoundNo());
            return toVO(exists);
        }

        AwdRound r = new AwdRound();
        r.setContestId(req.getContestId());
        r.setRoundNo(req.getRoundNo());
        r.setStatus(RoundStatus.PENDING.name());
        r.setDurationS(req.getDurationS() == null ? 600 : req.getDurationS());
        try {
            roundMapper.insert(r);
        } catch (DuplicateKeyException e) {
            // 并发创建：唯一键 uk_round 兜底
            AwdRound again = roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                    .eq(AwdRound::getContestId, req.getContestId())
                    .eq(AwdRound::getRoundNo, req.getRoundNo())
                    .last("LIMIT 1"));
            if (again == null) throw e;
            return toVO(again);
        }
        log.info("创建 AWD 轮次 contest={} round={} duration={}s mode={}",
                req.getContestId(), req.getRoundNo(), r.getDurationS(), req.getMode());
        return toVO(r);
    }

    /**
     * 通用状态迁移入口：校验状态机 + 加锁 + CAS 更新 + 副作用。
     *
     * @param contestId 竞赛
     * @param roundNo   轮次号
     * @param target    目标状态
     */
    public RoundVO transit(Long contestId, Integer roundNo, RoundStatus target) {
        String lockKey = LOCK_PREFIX + contestId + ":" + roundNo;
        String owner = "awd-" + Thread.currentThread().getId() + "-" + System.nanoTime();
        if (!redisService.tryLock(lockKey, owner, LOCK_TTL)) {
            throw BizException.of("轮次正在被其他操作推进，请稍后重试");
        }
        try {
            AwdRound r = requireRound(contestId, roundNo);
            RoundStatus from = RoundStateMachine.parse(r.getStatus());
            RoundStateMachine.requireTransition(from, target);
            applyTransition(r, from, target);
            return toVO(r);
        } finally {
            redisService.unlock(lockKey);
        }
    }

    /** 校验并执行迁移（调用方需持有锁）。 */
    private void applyTransition(AwdRound r, RoundStatus from, RoundStatus to) {
        LocalDateTime now = LocalDateTime.now();
        AwdRound patch = new AwdRound();
        patch.setStatus(to.name());

        if (to == RoundStatus.RUNNING) {
            int dur = r.getDurationS() == null || r.getDurationS() <= 0 ? 600 : r.getDurationS();
            patch.setStartAt(now);
            patch.setEndAt(now.plusSeconds(dur));
        } else if (to == RoundStatus.CHECKING || to == RoundStatus.FINISHED || to == RoundStatus.ABORTED) {
            patch.setEndAt(now);
        }

        // CAS：仅当数据库仍是 from 时更新，避免锁失效窗口内的重复推进
        int rows = roundMapper.update(patch, new LambdaQueryWrapper<AwdRound>()
                .eq(AwdRound::getId, r.getId())
                .eq(AwdRound::getStatus, from.name()));
        if (rows == 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "轮次状态已被并发修改，请刷新后重试");
        }
        patch.setId(r.getId());
        patch.setContestId(r.getContestId());
        patch.setRoundNo(r.getRoundNo());
        patch.setDurationS(r.getDurationS());
        log.info("轮次迁移 contest={} round={} {} → {}", r.getContestId(), r.getRoundNo(), from, to);

        // ---- 副作用 ----
        if (to == RoundStatus.RUNNING) {
            // 开局：确保所有参赛战队靶机就绪
            try {
                targetService.provisionForContest(r.getContestId());
            } catch (Exception e) {
                log.error("开局靶场编排失败 contest={} round={}", r.getContestId(), r.getRoundNo(), e);
            }
            // 开局：为本轮签发动态 flag，并把新 flag 注入到各队靶机容器（每轮自动刷新 flag）。
            // 需求第 60 段要求“每轮自动：刷新 flag、执行 check 脚本、攻防计分”；
            // 此前仅 provision 靶机、从未按轮次签发/重置 flag，导致新轮次无 flag 可攻防。
            try {
                int n = targetService.rotateFlagsForRound(r.getContestId(), r.getRoundNo());
                log.info("轮次开局已刷新 flag contest={} round={} targets={}",
                        r.getContestId(), r.getRoundNo(), n);
            } catch (Exception e) {
                log.error("开局刷新 flag 失败 contest={} round={}", r.getContestId(), r.getRoundNo(), e);
            }
        }
        // CHECKING：攻击窗口已关闭，立即对每队靶机执行 check 脚本（容错：单队失败不阻塞整轮）
        if (to == RoundStatus.CHECKING) {
            runCheck(r.getContestId(), r.getRoundNo());
        }
        // SETTLING：check 结果已产生，结算攻防分 + SLA 扣分并推送排行榜
        if (to == RoundStatus.SETTLING) {
            try {
                scoreService.settleRound(r.getContestId(), r.getRoundNo());
            } catch (Exception e) {
                log.error("轮次结算失败 contest={} round={}", r.getContestId(), r.getRoundNo(), e);
            }
        }
        if (to.isTerminal()) {
            redisService.delete(AwdKeys.roundFlags(r.getContestId(), r.getRoundNo()));
        }

        wsPublisher.roundChange(r.getContestId(), Map.of(
                "roundNo", r.getRoundNo(),
                "from", from.name(),
                "to", to.name(),
                "startAt", patch.getStartAt() == null ? "" : patch.getStartAt().toString(),
                "endAt", patch.getEndAt() == null ? "" : patch.getEndAt().toString()));
    }

    /** 手动开局。 */
    public RoundVO start(Long contestId, Integer roundNo) {
        return transit(contestId, roundNo, RoundStatus.RUNNING);
    }

    /** 开始本轮（PENDING → RUNNING，等价 start，Cardinal 的 begin 语义）。 */
    public RoundVO begin(Long contestId, Integer roundNo) {
        return transit(contestId, roundNo, RoundStatus.RUNNING);
    }

    /** 当前攻击窗口中的轮次（RUNNING）；无则返回 null。 */
    public AwdRound running(Long contestId) {
        return roundMapper.selectOne(new LambdaQueryWrapper<AwdRound>()
                .eq(AwdRound::getContestId, contestId)
                .eq(AwdRound::getStatus, RoundStatus.RUNNING.name())
                .orderByDesc(AwdRound::getRoundNo)
                .last("LIMIT 1"));
    }

    /** 手动进入检查阶段（提前收卷）。 */
    public RoundVO check(Long contestId, Integer roundNo) {
        return transit(contestId, roundNo, RoundStatus.CHECKING);
    }

    /** 手动结算。 */
    public RoundVO finish(Long contestId, Integer roundNo) {
        return transit(contestId, roundNo, RoundStatus.FINISHED);
    }

    /** 中止轮次（裁判强制结束）。 */
    public RoundVO abort(Long contestId, Integer roundNo) {
        return transit(contestId, roundNo, RoundStatus.ABORTED);
    }

    // ------------------------------------------------------------------ 自动推进

    /**
     * 定时编排：扫描所有进行中/检查中的轮次，按时间自动推进。
     * 由 {@code AwdRoundScheduler} 周期调用，返回本次推进的轮次数。
     */
    public int tick() {
        List<AwdRound> active = roundMapper.selectList(new LambdaQueryWrapper<AwdRound>()
                .in(AwdRound::getStatus, RoundStatus.RUNNING.name(), RoundStatus.CHECKING.name(),
                        RoundStatus.SETTLING.name()));
        int advanced = 0;
        for (AwdRound r : active) {
            RoundStatus cur = parseSafe(r.getStatus());
            if (cur == null) continue;
            long remain = r.getEndAt() == null ? 0
                    : Duration.between(LocalDateTime.now(), r.getEndAt()).getSeconds();
            // CHECKING 阶段：以 runCheck 写入的 check-done 标记为准（真实 check 执行完成）；
            // SETTLING 阶段：结算已在进入 SETTLING 时同步完成，直接允许推进到 FINISHED。
            boolean checkDone = false;
            if (cur == RoundStatus.CHECKING) {
                Object done = redisService.get(AwdKeys.roundPhase(r.getContestId(), r.getRoundNo(), "check-done"));
                checkDone = done != null;
            } else if (cur == RoundStatus.SETTLING) {
                checkDone = true;
            }
            RoundStatus next = RoundStateMachine.autoAdvance(cur, remain, checkDone);
            if (next == null) continue;
            try {
                transit(r.getContestId(), r.getRoundNo(), next);
                advanced++;
            } catch (Exception e) {
                log.warn("自动推进轮次失败 contest={} round={}: {}", r.getContestId(), r.getRoundNo(), e.getMessage());
            }
        }
        return advanced;
    }

    /**
     * CHECKING 阶段：对全场每队靶机执行一次 check 脚本（AWDP 的核心校验步骤）。
     *
     * <p>容错：任一队 check 抛异常不影响其他队；结果由 {@link AwdCheckService#checkContest}
     * 统一产出（含 PASS/FAIL/TIMEOUT/UNREACHABLE 与 SLA 扣分），SETTLING 阶段据此结算。</p>
     */
    private void runCheck(Long contestId, Integer roundNo) {
        try {
            // 1) 对全场每队靶机执行 check（单队异常已由 AwdCheckService 隔离）
            var byTeam = checkService.checkContest(contestId, roundNo);
            int teams = byTeam == null ? 0 : byTeam.size();
            int fail = 0;
            if (byTeam != null) {
                for (var results : byTeam.values()) {
                    if (results != null) fail += AwdCheckService.failCount(results);
                }
            }
            // 2) 结算 SLA：仅对 check 失败靶机所属战队扣分，写入 awd_score + 排行榜（幂等）
            java.util.Map<Long, Long> deltas = java.util.Collections.emptyMap();
            try {
                deltas = scoreService.settleCheckResults(contestId, roundNo, byTeam);
            } catch (Exception se) {
                log.error("check SLA 结算失败（不影响轮次推进） contest={} round={}: {}",
                        contestId, roundNo, se.getMessage());
            }
            // 3) 标记本轮 check 已执行，供 tick() 判定 CHECKING 完成 → SETTLING
            redisService.set(AwdKeys.roundPhase(contestId, roundNo, "check-done"), "1",
                    java.time.Duration.ofHours(24));

            log.info("轮次 check 完成 contest={} round={} teams={} failTargets={} slaDeltas={}",
                    contestId, roundNo, teams, fail, deltas);
            wsPublisher.publish("ROUND_CHECK_DONE", contestId, java.util.Map.of(
                    "roundNo", roundNo, "teams", teams, "failTargets", fail, "slaDeltas", deltas));
        } catch (Exception e) {
            log.error("轮次 check 执行失败 contest={} round={}: {}", contestId, roundNo, e.getMessage());
            // 失败也要标记完成，避免整轮卡在 CHECKING（宁可漏扣分也不能阻塞调度）
            try {
                redisService.set(AwdKeys.roundPhase(contestId, roundNo, "check-done"), "1",
                        java.time.Duration.ofHours(24));
            } catch (Exception ignore) { /* ignore */ }
        }
    }

    private RoundStatus parseSafe(String s) {
        if (s == null) return null;
        try {
            return RoundStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}