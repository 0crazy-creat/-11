package com.csp.ctf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.entity.ContestQuestion;
import com.csp.ctf.entity.CtfQuestion;
import com.csp.ctf.mapper.ContestQuestionMapper;
import com.csp.ctf.mapper.CtfQuestionMapper;
import com.csp.ctf.vo.TheoryPaperVO;
import com.csp.ctf.vo.TheoryQuestionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 理论赛：按赛事抽题组卷、下发去答案的题面、交卷后统一判分。
 * <p>理论赛与 CTF 共用 question 表（bankType=THEORY），答案字段不下发前端。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TheoryService {

    private final ContestGuard guard;
    private final ContestQuestionMapper contestQuestionMapper;
    private final CtfQuestionMapper questionMapper;
    private final SubmissionService submissionService;

    /** 组卷：返回该理论赛赛事的全部理论题（隐去答案/flag）。 */
    public TheoryPaperVO paper(Long contestId, Long userId) {
        var contest = guard.requireAnswering(contestId);
        if (!"THEORY".equalsIgnoreCase(contest.getType())) {
            throw new BizException(ResultCode.BAD_REQUEST, "该赛事不是理论赛：" + contest.getType());
        }
        List<ContestQuestion> links = contestQuestionMapper.selectList(new LambdaQueryWrapper<ContestQuestion>()
                .eq(ContestQuestion::getContestId, contestId)
                .eq(ContestQuestion::getVisible, 1)
                .orderByAsc(ContestQuestion::getSortNo));
        List<TheoryQuestionVO> items = new ArrayList<>(links.size());
        int totalScore = 0;
        for (ContestQuestion link : links) {
            CtfQuestion q = questionMapper.selectById(link.getQuestionId());
            if (q == null || !"THEORY".equalsIgnoreCase(q.getBankType())) continue;
            int score = link.getScore() != null && link.getScore() > 0
                    ? link.getScore()
                    : (q.getScore() == null ? 0 : q.getScore());
            totalScore += score;
            items.add(TheoryQuestionVO.builder()
                    .questionId(q.getId())
                    .sortNo(link.getSortNo())
                    .title(q.getTitle())
                    .content(q.getContent())
                    .category(q.getCategory())
                    .difficulty(q.getDifficulty())
                    .score(score)
                    .build());
        }
        return TheoryPaperVO.builder()
                .contestId(contestId)
                .contestName(contest.getName())
                .endAt(contest.getEndAt())
                .totalScore(totalScore)
                .questionCount(items.size())
                .questions(items)
                .build();
    }

    /** 交卷：逐题判分（每题一次，忽略重复提交），返回汇总。 */
    public SubmitSummary grade(Long contestId, Long userId, List<TheoryAnswer> answers, jakarta.servlet.http.HttpServletRequest request) {
        if (answers == null || answers.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "答卷为空");
        }
        int right = 0;
        int totalGot = 0;
        List<String> details = new ArrayList<>();
        for (TheoryAnswer a : answers) {
            if (a.getQuestionId() == null || a.getAnswer() == null || a.getAnswer().isBlank()) continue;
            var req = new com.csp.ctf.dto.SubmitRequest();
            req.setContestId(contestId);
            req.setQuestionId(a.getQuestionId());
            req.setContent(a.getAnswer());
            req.setSubmitType("ANSWER");
            req.setDeviceId(a.getDeviceId());
            try {
                var r = submissionService.submit(userId, req, request);
                if (r.isCorrect()) {
                    right++;
                    totalGot += r.getScore();
                }
                details.add("Q" + a.getQuestionId() + ":" + (r.isCorrect() ? "正确" : "错误"));
            } catch (com.csp.common.exception.BizException be) {
                // 业务异常（重复提交、题目不存在等）不阻断整卷判分
                details.add("Q" + a.getQuestionId() + ":跳过(" + be.getMessage() + ")");
            } catch (Exception ex) {
                // 非法答案值等运行期异常一律判错，避免整卷 500
                log.warn("理论判分单题异常 qid={} answer={} : {}", a.getQuestionId(), a.getAnswer(), ex.getMessage());
                details.add("Q" + a.getQuestionId() + ":错误");
            }
        }
        return new SubmitSummary(right, answers.size(), totalGot, details);
    }

    /** 单题答案提交体。 */
    @lombok.Data
    public static class TheoryAnswer {
        private Long questionId;
        private String answer;
        private String deviceId;
    }

    /** 交卷汇总。 */
    public record SubmitSummary(int correctCount, int totalCount, int gainedScore, List<String> details) {
    }
}