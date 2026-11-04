package com.csp.ctf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.common.util.FlagUtils;
import com.csp.ctf.dto.SubmitRequest;
import com.csp.ctf.dto.SubmitResult;
import com.csp.ctf.entity.ContestQuestion;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.CtfQuestion;
import com.csp.ctf.entity.Scoreboard;
import com.csp.ctf.entity.Submission;
import com.csp.ctf.judge.FlagValidator;
import com.csp.ctf.judge.FlagValidatorRegistry;
import com.csp.ctf.judge.JudgeContext;
import com.csp.ctf.judge.QuestionExt;
import com.csp.ctf.score.DynamicScorePolicy;
import com.csp.ctf.mapper.ContestQuestionMapper;
import com.csp.ctf.mapper.CtfQuestionMapper;
import com.csp.ctf.mapper.ScoreboardMapper;
import com.csp.ctf.mapper.SubmissionMapper;
import com.csp.infra.redis.RedisKeys;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 答题与判分：
 * <ul>
 *   <li>CTF 题：flag 常量时间比对（支持静态/动态 flag），首杀加分；</li>
 *   <li>理论题：标准答案归一化后比对（忽略大小写与空白），支持多选排序无关；</li>
 *   <li>同一战队对同一题只计分一次，重复提交仅记录不重复加分；</li>
 *   <li>全流程接入反作弊前置/后置校验。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private static final String TYPE_FLAG = "FLAG";
    private static final String TYPE_ANSWER = "ANSWER";
    /** 首杀额外奖励默认值（赛事未配置时）。 */
    private static final int DEFAULT_FIRST_BLOOD_BONUS = 0;
    private static final Duration FIRST_BLOOD_TTL = Duration.ofDays(7);
    /** 排行榜冻结缓存 TTL。 */
    private static final Duration FROZEN_TTL = Duration.ofMinutes(5);

    private final ContestGuard guard;
    private final ContestEnrollService enrollService;
    private final AntiCheatService antiCheat;
    private final AntiCheatAnalysisService antiCheatAnalysis;
    private final ScoreboardService scoreboardService;
    private final SubmissionMapper submissionMapper;
    private final ContestQuestionMapper contestQuestionMapper;
    private final CtfQuestionMapper questionMapper;
    private final ScoreboardMapper scoreboardMapper;
    private final RedisService redisService;
    private final WsPublisher wsPublisher;
    /** 判题策略路由（STATIC/DYNAMIC/SCRIPT）。 */
    private final FlagValidatorRegistry validatorRegistry;
    /** 战队表：用于统计参赛队伍总数（动态积分衰减的分母）。 */
    private final com.csp.ctf.mapper.ContestTeamMapper contestTeamMapper;

    /** 提交 flag / 理论题答案并即时判分。 */
    @Transactional(rollbackFor = Exception.class)
    public SubmitResult submit(Long userId, SubmitRequest req, HttpServletRequest httpRequest) {
        var contest = guard.requireAnswering(req.getContestId());

        ContestTeam team = enrollService.myTeam(userId, contest.getId());
        if (team == null) throw new BizException(ResultCode.FORBIDDEN, "未报名该赛事，无法答题");
        if (!"APPROVED".equals(team.getStatus())) {
            throw new BizException(ResultCode.FORBIDDEN, "战队尚未通过审核：" + team.getStatus());
        }

        // 题目必须属于本赛事且可见
        ContestQuestion cq = contestQuestionMapper.selectOne(new LambdaQueryWrapper<ContestQuestion>()
                .eq(ContestQuestion::getContestId, contest.getId())
                .eq(ContestQuestion::getQuestionId, req.getQuestionId()));
        if (cq == null) throw BizException.notFound("赛事题目");
        if (cq.getVisible() != null && cq.getVisible() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "题目当前不可见");
        }

        CtfQuestion question = questionMapper.selectById(req.getQuestionId());
        if (question == null) throw BizException.notFound("题目");

        String submitType = normalizeType(req.getSubmitType(), question.getBankType());
        String ip = antiCheat.resolveIp(httpRequest);

        // 反作弊前置：限流 + 暴力猜解
        antiCheat.preCheck(contest.getId(), userId);

        // 记录设备指纹（用于多账号检测）
        String userAgent = httpRequest.getHeader("User-Agent");
        antiCheatAnalysis.recordFingerprint(contest.getId(), userId, req.getDeviceId(), 
                null, ip, userAgent);

        // calcElapsed 在未传 startTs 时返回 null，这里用 0 兜底，避免拆箱 NPE
        Long elapsedOrNull = calcElapsed(req.getStartTs());
        long elapsedMs = elapsedOrNull == null ? 0L : elapsedOrNull;
        boolean alreadySolved = alreadySolved(contest.getId(), team.getId(), question.getId());

        QuestionExt ext = QuestionExt.parse(question.getExt());
        boolean correct = checkAnswer(question, submitType, req.getContent(),
                contest.getId(), team.getId(), userId, ext);

        int score = 0;
        boolean firstBlood = false;
        long totalScore = currentScore(contest.getId(), team.getId());
        int solved = currentSolved(contest.getId(), team.getId());

        if (correct) {
            if (alreadySolved) {
                // 已解出：不重复加分，仍记录提交用于风控
                recordSubmission(contest.getId(), team, userId, question.getId(), submitType,
                        req.getContent(), 1, 0, ip, req.getDeviceId(), elapsedMs);
                antiCheat.postCheckOnCorrect(contest.getId(), userId, ip, elapsedMs);
                return SubmitResult.builder()
                        .correct(true).score(0).firstBlood(false)
                        .message("该题已解出，本次不重复计分")
                        .totalScore(totalScore).solved(solved).elapsedMs(elapsedMs)
                        .build();
            }

            // 解出顺序（血位）：用于一血/二血/三血奖励
            int solveOrder = currentSolveCount(contest.getId(), question.getId()) + 1;
            firstBlood = solveOrder == 1 && tryMarkFirstBlood(contest.getId(), question.getId());
            int staticScore = resolveScore(cq, question);
            int totalTeams = contestTeamCount(contest.getId());
            score = DynamicScorePolicy.award(ext, staticScore,
                    solveOrder - 1, totalTeams, solveOrder);
            if (firstBlood) {
                // 赛事级首杀奖励（contest.first_blood）与题目级血奖励叠加，兼容旧配置
                score += resolveFirstBloodBonus(contest);
            }

            recordSubmission(contest.getId(), team, userId, question.getId(), submitType,
                    req.getContent(), 1, score, ip, req.getDeviceId(), elapsedMs);

            totalScore = scoreboardService.applyScore(contest.getId(), team.getId(), team.getName(), score);
            solved = currentSolved(contest.getId(), team.getId());

            if (firstBlood) {
                wsPublisher.firstBlood(contest.getId(), Map.of(
                        "teamId", team.getId(), "teamName", team.getName(),
                        "questionId", question.getId(), "questionTitle", question.getTitle(),
                        "score", score));
            }
            antiCheat.postCheckOnCorrect(contest.getId(), userId, ip, elapsedMs);

            return SubmitResult.builder()
                    .correct(true).score(score).firstBlood(firstBlood)
                    .message(firstBlood ? "答案正确！恭喜一血 +" + score + " 分" : "答案正确 +" + score + " 分")
                    .totalScore(totalScore).solved(solved).elapsedMs(elapsedMs)
                    .build();
        }

        // 错误提交：记录但不加分
        recordSubmission(contest.getId(), team, userId, question.getId(), submitType,
                req.getContent(), 0, 0, ip, req.getDeviceId(), elapsedMs);
        return SubmitResult.builder()
                .correct(false).score(0).firstBlood(false)
                .message(TYPE_ANSWER.equals(submitType) ? "答案错误" : "flag 错误")
                .totalScore(totalScore).solved(solved).elapsedMs(elapsedMs)
                .build();
    }

    /** 当前用户在赛事内已解出的题目ID列表。 */
    public List<Long> solvedQuestionIds(Long contestId, Long teamId) {
        return submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getContestId, contestId)
                        .eq(Submission::getTeamId, teamId)
                        .eq(Submission::getCorrect, 1)
                        .select(Submission::getQuestionId))
                .stream().map(Submission::getQuestionId).distinct().toList();
    }

    /** 个人/战队提交记录（分页由调用方限制条数）。 */
    public List<Submission> recentSubmissions(Long contestId, Long teamId, int limit) {
        return submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getContestId, contestId)
                .eq(Submission::getTeamId, teamId)
                .orderByDesc(Submission::getCreatedAt)
                .last("LIMIT " + Math.max(1, Math.min(limit, 200))));
    }

    // ---------- 判分核心 ----------

    /**
     * 判题入口：理论题走答案比对，CTF 题按 {@code question.ext.judgeMode}
     * 路由到 STATIC / DYNAMIC / SCRIPT 策略（见 {@link FlagValidatorRegistry}）。
     */
    private boolean checkAnswer(CtfQuestion question, String submitType, String rawContent,
                                Long contestId, Long teamId, Long userId, QuestionExt ext) {
        String submitted = FlagUtils.normalize(rawContent);
        if (submitted.isEmpty()) return false;
        if (TYPE_ANSWER.equals(submitType)) {
            return theoryMatch(question.getAnswer(), submitted);
        }
        FlagValidator validator = validatorRegistry.of(ext);
        return validator.validate(JudgeContext.builder()
                .contestId(contestId)
                .teamId(teamId)
                .userId(userId)
                .question(question)
                .submitted(submitted)
                .ext(ext)
                .build());
    }

    /** 理论题判分：忽略大小写、空白；多选用逗号/分号分隔时忽略顺序。 */
    static boolean theoryMatch(String standard, String submitted) {
        if (standard == null || standard.isBlank()) return false;
        String a = normalizeTheory(standard);
        String b = normalizeTheory(submitted);
        if (a.equals(b)) return true;
        // 多选：排序后逐项比较
        List<String> as = splitMulti(a);
        List<String> bs = splitMulti(b);
        if (as.size() > 1 || bs.size() > 1) return as.equals(bs);
        return false;
    }

    private static String normalizeTheory(String s) {
        // 统一分隔符：中文逗号/分号、英文分号 → 英文逗号；去除全部空白（含全角空格）；忽略大小写
        return s.replaceAll("[\\s　]+", "")
                .replace('，', ',').replace('；', ',').replace(';', ',')
                .toLowerCase();
    }

    private static List<String> splitMulti(String s) {
        return java.util.Arrays.stream(s.split("[,]"))
                .filter(x -> !x.isEmpty()).sorted().toList();
    }

    private String normalizeType(String requested, String bankType) {
        String t = requested == null ? "" : requested.trim().toUpperCase();
        if (TYPE_ANSWER.equals(t) || TYPE_FLAG.equals(t)) return t;
        if ("THEORY".equalsIgnoreCase(bankType)) return TYPE_ANSWER;
        return TYPE_FLAG;
    }

    private int resolveScore(ContestQuestion cq, CtfQuestion question) {
        if (cq.getScore() != null && cq.getScore() > 0) return cq.getScore();
        return question.getScore() == null || question.getScore() <= 0 ? 100 : question.getScore();
    }

    private int resolveFirstBloodBonus(com.csp.ctf.entity.Contest contest) {
        return contest.getFirstBlood() == null ? DEFAULT_FIRST_BLOOD_BONUS : contest.getFirstBlood();
    }

    /** 首杀标记：SET NX，成功即为首杀。Redis 故障时降级为 DB 判定。 */
    private boolean tryMarkFirstBlood(Long contestId, Long questionId) {
        String key = RedisKeys.firstBlood(contestId) + ":" + questionId;
        try {
            return redisService.tryLock(key, "1", FIRST_BLOOD_TTL);
        } catch (Exception e) {
            log.debug("首杀判定降级: {}", e.getMessage());
            Long n = submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                    .eq(Submission::getContestId, contestId)
                    .eq(Submission::getQuestionId, questionId)
                    .eq(Submission::getCorrect, 1));
            return n == null || n == 0;
        }
    }

    /** 该题当前已解出的队伍数（动态积分衰减的分母分子）。 */
    private int currentSolveCount(Long contestId, Long questionId) {
        Long n = submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getContestId, contestId)
                .eq(Submission::getQuestionId, questionId)
                .eq(Submission::getCorrect, 1));
        return n == null ? 0 : n.intValue();
    }

    /** 赛事参赛队伍总数（审核通过的队伍；至少按 1 处理）。 */
    private int contestTeamCount(Long contestId) {
        try {
            Long n = contestTeamMapper.selectCount(new LambdaQueryWrapper<com.csp.ctf.entity.ContestTeam>()
                    .eq(com.csp.ctf.entity.ContestTeam::getContestId, contestId)
                    .eq(com.csp.ctf.entity.ContestTeam::getStatus, "APPROVED"));
            return n == null || n <= 0 ? 1 : n.intValue();
        } catch (Exception e) {
            log.debug("统计参赛队伍失败，动态积分按单队处理: {}", e.getMessage());
            return 1;
        }
    }

    private boolean alreadySolved(Long contestId, Long teamId, Long questionId) {
        Long n = submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getContestId, contestId)
                .eq(Submission::getTeamId, teamId)
                .eq(Submission::getQuestionId, questionId)
                .eq(Submission::getCorrect, 1));
        return n != null && n > 0;
    }

    private long currentScore(Long contestId, Long teamId) {
        Scoreboard sb = scoreboardService.get(contestId, teamId);
        return sb == null || sb.getScore() == null ? 0L : sb.getScore();
    }

    private int currentSolved(Long contestId, Long teamId) {
        Scoreboard sb = scoreboardService.get(contestId, teamId);
        return sb == null || sb.getSolved() == null ? 0 : sb.getSolved();
    }

    private void recordSubmission(Long contestId, ContestTeam team, Long userId, Long questionId,
                                  String submitType, String content, int correct, int score,
                                  String ip, String deviceId, Long elapsedMs) {
        Submission s = new Submission();
        s.setContestId(contestId);
        s.setTeamId(team.getId());
        s.setUserId(userId);
        s.setQuestionId(questionId);
        s.setSubmitType(submitType);
        // 脱敏：错误提交仅存摘要，正确提交存摘要，避免明文 flag 落库
        s.setContent(FlagUtils.sha256(FlagUtils.normalize(content)));
        s.setCorrect(correct);
        s.setScore(score);
        s.setIp(ip);
        s.setDeviceId(deviceId);
        s.setElapsedMs(elapsedMs);
        s.setCreatedAt(LocalDateTime.now());
        submissionMapper.insert(s);
    }

    private Long calcElapsed(Long startTs) {
        if (startTs == null || startTs <= 0) return null;
        long now = System.currentTimeMillis();
        long el = now - startTs;
        return el < 0 ? 0L : el;
    }
}