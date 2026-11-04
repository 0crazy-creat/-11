package com.csp.ctf.scenario.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.common.util.FlagUtils;
import com.csp.ctf.entity.Contest;
import com.csp.ctf.entity.ContestQuestion;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.ContestTeamMember;
import com.csp.ctf.entity.CtfQuestion;
import com.csp.ctf.entity.Submission;
import com.csp.ctf.mapper.ContestQuestionMapper;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.ctf.mapper.ContestTeamMemberMapper;
import com.csp.ctf.mapper.CtfQuestionMapper;
import com.csp.ctf.mapper.SubmissionMapper;
import com.csp.ctf.scenario.dto.ScenarioArenaVO;
import com.csp.ctf.scenario.dto.ScenarioNodeVO;
import com.csp.ctf.scenario.dto.ScenarioSubmitCmd;
import com.csp.ctf.scenario.dto.ScenarioSubmitResult;
import com.csp.ctf.service.ContestGuard;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 场景实操赛（SCENARIO）核心服务。
 *
 * <p>模型：复用统一题库 {@code question}（bank_type=SCENARIO）作为「任务节点」，
 * 通过赛事组卷 {@code contest_question} 绑定到赛事，通过
 * {@code question.ext.preQuestionId} 表达前置依赖，形成
 * 「场景 → 目标 → 任务 → 关键证据」的树形结构。</p>
 *
 * <p>关键约束：</p>
 * <ol>
 *   <li><b>前置依赖</b>：{@code preQuestionId} 未完成的任务拒绝提交证据；</li>
 *   <li><b>幂等</b>：同一队伍同一任务只计一次分；</li>
 *   <li><b>场景完成</b>：该场景下全部任务完成时返回 {@code scenarioCompleted=true}；</li>
 *   <li><b>证据 Flag</b>：常量时间比对，不泄露正确答案。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ContestGuard contestGuard;
    private final CtfQuestionMapper questionMapper;
    private final ContestQuestionMapper contestQuestionMapper;
    private final ContestTeamMapper teamMapper;
    private final ContestTeamMemberMapper teamMemberMapper;
    private final SubmissionMapper submissionMapper;
    private final ObjectMapper objectMapper;

    private static final String BANK_TYPE = "SCENARIO";

    // =====================================================================
    // 场景树视图
    // =====================================================================

    /** 当前队伍的场景实操赛视图：场景树 + 我的进度 + 总分。 */
    public ScenarioArenaVO arena(Long contestId, Long userId, boolean privileged) {
        Contest contest = contestGuard.requireContest(contestId);

        Long teamId = resolveTeamId(contestId, userId, privileged);
        Set<Long> solved = solvedQuestionIds(contestId, teamId);

        List<ContestQuestion> bindings = contestQuestionMapper.selectList(
                new LambdaQueryWrapper<ContestQuestion>()
                        .eq(ContestQuestion::getContestId, contestId)
                        .eq(ContestQuestion::getVisible, 1)
                        .orderByAsc(ContestQuestion::getSortNo));
        if (bindings.isEmpty()) {
            return ScenarioArenaVO.builder()
                    .contestId(contestId).contestName(contest.getName())
                    .totalScore(0).myScore(0).solvedCount(0).totalCount(0)
                    .scenarios(List.of()).build();
        }

        Map<Long, Integer> scoreOverride = new HashMap<>();
        for (ContestQuestion b : bindings) {
            if (b.getScore() != null) scoreOverride.put(b.getQuestionId(), b.getScore());
        }
        List<Long> qids = bindings.stream().map(ContestQuestion::getQuestionId).toList();
        List<CtfQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<CtfQuestion>()
                        .in(CtfQuestion::getId, qids)
                        .eq(CtfQuestion::getBankType, BANK_TYPE)
                        .eq(CtfQuestion::getStatus, 1));
        Map<Long, CtfQuestion> qById = questions.stream()
                .collect(Collectors.toMap(CtfQuestion::getId, q -> q, (a, b) -> a));

        // 分组：ext.sceneGroup 缺省用 category 作为场景名
        Map<String, List<CtfQuestion>> grouped = new LinkedHashMap<>();
        for (Long qid : qids) {
            CtfQuestion q = qById.get(qid);
            if (q == null) continue;
            grouped.computeIfAbsent(sceneGroupOf(q), k -> new ArrayList<>()).add(q);
        }

        int totalScore = 0, myScore = 0, solvedCount = 0, totalCount = 0;
        List<ScenarioNodeVO> scenarios = new ArrayList<>();
        for (Map.Entry<String, List<CtfQuestion>> e : grouped.entrySet()) {
            List<ScenarioNodeVO> tasks = new ArrayList<>();
            int sSolved = 0;
            for (CtfQuestion q : e.getValue()) {
                int sc = scoreOverride.getOrDefault(q.getId(), q.getScore() == null ? 100 : q.getScore());
                boolean ok = solved.contains(q.getId());
                totalScore += sc;
                totalCount++;
                if (ok) { myScore += sc; solvedCount++; sSolved++; }
                tasks.add(toNode(q, sc, ok, 0, 0));
            }
            scenarios.add(ScenarioNodeVO.builder()
                    .questionId(null)
                    .title(e.getKey())
                    .category(e.getKey())
                    .content(null)
                    .score(tasks.stream().mapToInt(n -> n.getScore() == null ? 0 : n.getScore()).sum())
                    .difficulty(null)
                    .docker(false)
                    .solved(sSolved == tasks.size() && !tasks.isEmpty())
                    .solvedTasks(sSolved)
                    .totalTasks(tasks.size())
                    .children(tasks)
                    .build());
        }

        return ScenarioArenaVO.builder()
                .contestId(contestId).contestName(contest.getName())
                .totalScore(totalScore).myScore(myScore)
                .solvedCount(solvedCount).totalCount(totalCount)
                .scenarios(scenarios).build();
    }

    // =====================================================================
    // 提交证据 Flag
    // =====================================================================

    /** 提交某任务的证据 Flag：前置依赖校验 → 常量时间比对 → 计分。 */
    @Transactional(rollbackFor = Exception.class)
    public ScenarioSubmitResult submit(Long userId, ScenarioSubmitCmd cmd) {
        Contest contest = contestGuard.requireAnswering(cmd.getContestId());
        requireScenario(contest);

        Long teamId = resolveTeamId(cmd.getContestId(), userId, false);

        // 1) 必须是本赛事已下发的题目
        ContestQuestion binding = contestQuestionMapper.selectOne(
                new LambdaQueryWrapper<ContestQuestion>()
                        .eq(ContestQuestion::getContestId, cmd.getContestId())
                        .eq(ContestQuestion::getQuestionId, cmd.getQuestionId())
                        .eq(ContestQuestion::getVisible, 1)
                        .last("LIMIT 1"));
        if (binding == null) throw BizException.of("该任务不属于当前赛事");

        CtfQuestion q = questionMapper.selectById(cmd.getQuestionId());
        if (q == null || !BANK_TYPE.equals(q.getBankType())) {
            throw BizException.of("任务不存在或不是场景赛题目");
        }

        Set<Long> solved = solvedQuestionIds(cmd.getContestId(), teamId);

        // 2) 幂等：已解出的任务不重复计分
        if (solved.contains(q.getId())) {
            return ScenarioSubmitResult.builder()
                    .correct(true).message("该任务已完成（不重复计分）").score(0)
                    .totalScore(currentScore(cmd.getContestId(), teamId, solved))
                    .firstBlood(false).scenarioCompleted(false).build();
        }

        // 3) 前置依赖：preQuestionId 未完成则拒绝
        Long pre = preQuestionIdOf(q);
        if (pre != null && !solved.contains(pre)) {
            CtfQuestion preQ = questionMapper.selectById(pre);
            throw BizException.of("需先完成前置任务：" +
                    (preQ == null ? String.valueOf(pre) : preQ.getTitle()));
        }

        // 4) 证据 Flag 常量时间比对
        boolean ok = q.getFlag() != null
                && FlagUtils.safeEquals(FlagUtils.normalize(q.getFlag()), FlagUtils.normalize(cmd.getFlag()));

        Submission sub = new Submission();
        sub.setContestId(cmd.getContestId());
        sub.setTeamId(teamId);
        sub.setUserId(userId);
        sub.setQuestionId(q.getId());
        sub.setSubmitType("SCENARIO_EVIDENCE");
        sub.setContent(mask(cmd.getFlag()));
        sub.setCorrect(ok ? 1 : 0);
        sub.setScore(0);
        sub.setCreatedAt(LocalDateTime.now());
        submissionMapper.insert(sub);

        if (!ok) {
            return ScenarioSubmitResult.builder()
                    .correct(false).message("证据 Flag 不正确").score(0)
                    .totalScore(currentScore(cmd.getContestId(), teamId, solved))
                    .firstBlood(false).scenarioCompleted(false).build();
        }

        int sc = binding.getScore() != null ? binding.getScore()
                : (q.getScore() == null ? 100 : q.getScore());
        sub.setScore(sc);
        submissionMapper.updateById(sub);

        solved.add(q.getId());

        // 5) 该场景是否因此次提交而全部完成
        boolean scenarioDone = isScenarioCompleted(cmd.getContestId(), teamId, q, solved);

        log.info("场景赛提交成功 contest={} team={} q={} +{}", cmd.getContestId(), teamId, q.getId(), sc);
        return ScenarioSubmitResult.builder()
                .correct(true).message("证据校验通过").score(sc)
                .totalScore(currentScore(cmd.getContestId(), teamId, solved))
                .firstBlood(false).scenarioCompleted(scenarioDone).build();
    }

    // =====================================================================
    // 内部工具
    // =====================================================================

    private ScenarioNodeVO toNode(CtfQuestion q, int score, boolean solved,
                                  int solvedTasks, int totalTasks) {
        return ScenarioNodeVO.builder()
                .questionId(q.getId())
                .title(q.getTitle())
                .category(q.getCategory())
                .content(q.getContent())
                .score(score)
                .difficulty(q.getDifficulty())
                .attachmentUrl(q.getAttachment())
                .imageName(q.getImageName())
                .docker(q.getImageName() != null && !q.getImageName().isBlank())
                .solved(solved)
                .solvedTasks(solvedTasks)
                .totalTasks(totalTasks)
                .preQuestionId(preQuestionIdOf(q))
                .children(List.of())
                .build();
    }

    /** 从 ext JSON 中读 preQuestionId / sceneGroup。 */
    private JsonNode extOf(CtfQuestion q) {
        if (q.getExt() == null || q.getExt().isBlank()) return null;
        try {
            return objectMapper.readTree(q.getExt());
        } catch (Exception e) {
            return null;
        }
    }

    private Long preQuestionIdOf(CtfQuestion q) {
        JsonNode n = extOf(q);
        if (n == null || !n.hasNonNull("preQuestionId")) return null;
        try {
            return n.get("preQuestionId").asLong();
        } catch (Exception e) {
            return null;
        }
    }

    private String sceneGroupOf(CtfQuestion q) {
        JsonNode n = extOf(q);
        if (n != null && n.hasNonNull("sceneGroup")) return n.get("sceneGroup").asText();
        return q.getCategory() == null || q.getCategory().isBlank() ? "默认场景" : q.getCategory();
    }

    /** 当前队伍已解出的题目 ID 集合（SCENARIO_EVIDENCE 且 correct=1）。 */
    private Set<Long> solvedQuestionIds(Long contestId, Long teamId) {
        if (teamId == null) return new HashSet<>();
        List<Submission> subs = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getContestId, contestId)
                        .eq(Submission::getTeamId, teamId)
                        .eq(Submission::getCorrect, 1)
                        .eq(Submission::getSubmitType, "SCENARIO_EVIDENCE"));
        return subs.stream().map(Submission::getQuestionId).collect(Collectors.toSet());
    }

    private int currentScore(Long contestId, Long teamId, Set<Long> solved) {
        if (solved.isEmpty()) return 0;
        List<ContestQuestion> bindings = contestQuestionMapper.selectList(
                new LambdaQueryWrapper<ContestQuestion>()
                        .eq(ContestQuestion::getContestId, contestId)
                        .eq(ContestQuestion::getVisible, 1));
        Map<Long, Integer> override = new HashMap<>();
        for (ContestQuestion b : bindings) {
            if (b.getScore() != null) override.put(b.getQuestionId(), b.getScore());
        }
        List<CtfQuestion> qs = questionMapper.selectBatchIds(solved);
        int sum = 0;
        for (CtfQuestion q : qs) {
            sum += override.getOrDefault(q.getId(), q.getScore() == null ? 100 : q.getScore());
        }
        return sum;
    }

    /** 判断该任务所属场景是否已全部完成。 */
    private boolean isScenarioCompleted(Long contestId, Long teamId, CtfQuestion q, Set<Long> solved) {
        String group = sceneGroupOf(q);
        List<ContestQuestion> bindings = contestQuestionMapper.selectList(
                new LambdaQueryWrapper<ContestQuestion>()
                        .eq(ContestQuestion::getContestId, contestId)
                        .eq(ContestQuestion::getVisible, 1));
        List<Long> qids = bindings.stream().map(ContestQuestion::getQuestionId).toList();
        if (qids.isEmpty()) return false;
        List<CtfQuestion> peers = questionMapper.selectList(
                new LambdaQueryWrapper<CtfQuestion>()
                        .in(CtfQuestion::getId, qids)
                        .eq(CtfQuestion::getBankType, BANK_TYPE));
        for (CtfQuestion p : peers) {
            if (!group.equals(sceneGroupOf(p))) continue;
            if (!solved.contains(p.getId())) return false;
        }
        return true;
    }

    /** 解析当前用户在本赛事中的队伍（管理员/裁判可免报名，返回其所属队伍或 null）。 */
    private Long resolveTeamId(Long contestId, Long userId, boolean privileged) {
        List<ContestTeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<ContestTeamMember>()
                        .eq(ContestTeamMember::getUserId, userId));
        if (!members.isEmpty()) {
            Set<Long> teamIds = members.stream()
                    .map(ContestTeamMember::getTeamId).collect(Collectors.toSet());
            List<ContestTeam> teams = teamMapper.selectList(
                    new LambdaQueryWrapper<ContestTeam>()
                            .eq(ContestTeam::getContestId, contestId)
                            .in(ContestTeam::getId, teamIds));
            if (!teams.isEmpty()) return teams.get(0).getId();
        }
        if (privileged) return null; // 特权角色：以「无队伍」身份查看全量，不拦截
        throw new BizException(ResultCode.FORBIDDEN, "尚未报名该赛事，无法查看题目");
    }

    private void requireScenario(Contest contest) {
        if (!"SCENARIO".equalsIgnoreCase(contest.getType())) {
            throw BizException.of("该赛事不是场景实操赛：" + contest.getType());
        }
    }

    private String mask(String flag) {
        if (flag == null) return null;
        if (flag.length() <= 6) return "***";
        return flag.substring(0, 3) + "***" + flag.substring(flag.length() - 2);
    }
}