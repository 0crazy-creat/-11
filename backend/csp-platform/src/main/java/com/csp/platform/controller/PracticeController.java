package com.csp.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.Result;
import com.csp.common.exception.BizException;
import com.csp.ctf.entity.ChallengeContainer;
import com.csp.ctf.judge.QuestionExt;
import com.csp.ctf.service.ContainerLifecycleService;
import com.csp.infra.docker.ContainerStatus;
import com.csp.infra.docker.NetworkMode;
import com.csp.common.security.LoginUser;
import com.csp.platform.entity.PracticeSubmission;
import com.csp.platform.entity.Question;
import com.csp.platform.mapper.PracticeSubmissionMapper;
import com.csp.platform.mapper.QuestionMapper;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 题目练习接口（公开，无需权限）。
 */
@Tag(name = "题目练习")
@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final QuestionMapper questionMapper;
    private final PracticeSubmissionMapper practiceSubmissionMapper;
    private final ContainerLifecycleService lifecycleService;
    private final HttpServletRequest httpRequest;

    /** 练习模式无赛事上下文，用 0 作为虚拟赛事 ID，与其他赛事隔离。 */
    private static final Long PRACTICE_CONTEST_ID = 0L;

    @Value("${csp.docker.public-host:}")
    private String publicHost;

    @Operation(summary = "获取练习题列表")
    @GetMapping("/challenges")
    public Result<Page<Question>> listChallenges(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword) {
        
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询CTF类型的题目
        wrapper.eq(Question::getBankType, "CTF");
        
        // 分类筛选：题库中同时存在 WEB / Web 等大小写混用，按小写比对避免漏题
        if (category != null && !category.isEmpty()) {
            wrapper.apply("LOWER(category) = LOWER({0})", category);
        }
        
        // 难度筛选
        if (difficulty != null && !difficulty.isEmpty()) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Question::getTitle, keyword)
                    .or().like(Question::getContent, keyword));
        }
        
        // 按难度排序
        wrapper.orderByAsc(Question::getDifficulty);
        
        Page<Question> pageParam = new Page<>(page, pageSize);
        Page<Question> result = questionMapper.selectPage(pageParam, wrapper);
        
        // 标记当前用户是否已解出（未登录则全部 false）
        LoginUser loginUser = UserContext.get();
        Set<Long> solvedIds = new HashSet<>();
        if (loginUser != null && loginUser.getUserId() != null) {
            practiceSubmissionMapper.selectList(new LambdaQueryWrapper<PracticeSubmission>()
                            .eq(PracticeSubmission::getUserId, loginUser.getUserId())
                            .eq(PracticeSubmission::getCorrect, 1))
                    .forEach(s -> solvedIds.add(s.getQuestionId()));
        }

        // 隐藏敏感字段（flag / 答案 / 题解）
        // 注意：writeup 正文中常直接包含 flag，必须一并置空，否则列表接口会泄漏答案
        result.getRecords().forEach(q -> {
            q.setFlag(null);
            q.setAnswer(null);
            q.setWriteup(null);
            q.setSolved(solvedIds.contains(q.getId()));
        });

        return Result.ok(result);
    }

    @Operation(summary = "获取题目详情")
    @GetMapping("/challenges/{id}")
    public Result<Question> getChallenge(@PathVariable Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            return Result.ok(null);
        }
        // 隐藏敏感字段（flag / 答案 / 题解）
        // 注意：writeup 正文中常直接包含 flag，必须一并置空，否则详情接口会泄漏答案
        question.setFlag(null);
        question.setAnswer(null);
        question.setWriteup(null);
        return Result.ok(question);
    }

    @Operation(summary = "获取题目分类列表")
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        List<String> categories = List.of("Web", "Misc", "Crypto", "Reverse", "Pwn");
        return Result.ok(categories);
    }

    /**
     * 练习模式判题：无赛事上下文，只做 flag 比对，不写入赛事提交表（不影响排行榜）。
     * 前端 /practice 页面直接调用本接口校验答案。
     */
    @Operation(summary = "练习模式判题")
    @PostMapping("/challenges/{id}/submit")
    public Result<Map<String, Object>> submitPracticeFlag(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        String input = body == null ? null : body.get("flag");
        if (input == null || input.trim().isEmpty()) {
            return Result.ok(Map.of("correct", false, "message", "请输入 Flag"));
        }

        Question question = questionMapper.selectById(id);
        if (question == null || question.getFlag() == null) {
            return Result.ok(Map.of("correct", false, "message", "题目不存在或未配置 Flag"));
        }

        boolean correct = input.trim().equals(question.getFlag().trim());

        LoginUser loginUser = UserContext.get();
        Long userId = loginUser == null ? null : loginUser.getUserId();

        int gained = 0;
        boolean alreadySolved = false;
        long solvedCount = 0;
        int totalScore = 0;

        if (userId != null) {
            if (correct) {
                Long already = practiceSubmissionMapper.selectCount(
                        new LambdaQueryWrapper<PracticeSubmission>()
                                .eq(PracticeSubmission::getUserId, userId)
                                .eq(PracticeSubmission::getQuestionId, id)
                                .eq(PracticeSubmission::getCorrect, 1));
                alreadySolved = already != null && already > 0;
                gained = alreadySolved ? 0 : (question.getScore() == null ? 0 : question.getScore());
            }

            PracticeSubmission record = new PracticeSubmission();
            record.setUserId(userId);
            record.setQuestionId(id);
            record.setCorrect(correct ? 1 : 0);
            record.setScore(gained);
            record.setContent(input.length() > 500 ? input.substring(0, 500) : input);
            record.setIp(clientIp());
            practiceSubmissionMapper.insert(record);

            List<PracticeSubmission> mine = practiceSubmissionMapper.selectList(
                    new LambdaQueryWrapper<PracticeSubmission>()
                            .eq(PracticeSubmission::getUserId, userId)
                            .eq(PracticeSubmission::getCorrect, 1));
            solvedCount = mine.stream().map(PracticeSubmission::getQuestionId).distinct().count();
            totalScore = mine.stream().mapToInt(s -> s.getScore() == null ? 0 : s.getScore()).sum();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("correct", correct);
        result.put("message", correct
                ? (alreadySolved ? "已解出过，本次不计分" : "答案正确！")
                : "Flag 不正确，请重试");
        result.put("score", gained);
        result.put("alreadySolved", alreadySolved);
        result.put("solvedCount", solvedCount);
        result.put("totalScore", totalScore);
        return Result.ok(result);
    }

    @Operation(summary = "我的练习统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long userId = UserContext.require().getUserId();
        List<PracticeSubmission> all = practiceSubmissionMapper.selectList(
                new LambdaQueryWrapper<PracticeSubmission>()
                        .eq(PracticeSubmission::getUserId, userId));

        long submitCount = all.size();
        long correctCount = all.stream().filter(s -> Integer.valueOf(1).equals(s.getCorrect())).count();
        long solvedCount = all.stream().filter(s -> Integer.valueOf(1).equals(s.getCorrect()))
                .map(PracticeSubmission::getQuestionId).distinct().count();
        int totalScore = all.stream().filter(s -> Integer.valueOf(1).equals(s.getCorrect()))
                .mapToInt(s -> s.getScore() == null ? 0 : s.getScore()).sum();
        double accuracy = submitCount == 0 ? 0 : Math.round(correctCount * 1000.0 / submitCount) / 10.0;

        Map<String, Object> vo = new HashMap<>();
        vo.put("solvedCount", solvedCount);
        vo.put("submitCount", submitCount);
        vo.put("correctCount", correctCount);
        vo.put("totalScore", totalScore);
        vo.put("accuracy", accuracy);
        return Result.ok(vo);
    }

    @Operation(summary = "练习排行榜")
    @GetMapping("/leaderboard")
    public Result<List<Map<String, Object>>> leaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        int safe = Math.min(Math.max(limit, 1), 50);
        return Result.ok(practiceSubmissionMapper.selectLeaderboard(safe));
    }

    private String clientIp() {
        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = httpRequest.getRemoteAddr();
        }
        int idx = ip.indexOf(',');
        return idx > 0 ? ip.substring(0, idx).trim() : ip;
    }

    // ==================== 练习容器（无赛事上下文） ====================

    @Operation(summary = "查询练习容器状态")
    @GetMapping("/challenges/{id}/container")
    public Result<ContainerVO> practiceContainerStatus(@PathVariable Long id) {
        Long userId = UserContext.require().getUserId();
        ChallengeContainer c = lifecycleService.getContainer(PRACTICE_CONTEST_ID, userId, id);
        return Result.ok(c == null ? null : toContainerVO(c));
    }

    @Operation(summary = "启动练习容器")
    @PostMapping("/challenges/{id}/container/start")
    public Result<ContainerVO> startPracticeContainer(@PathVariable Long id) {
        Long userId = UserContext.require().getUserId();
        Question q = requireContainerQuestion(id);
        QuestionExt ext = QuestionExt.parse(q.getExt());
        String image = effectiveImage(q, ext);
        ChallengeContainer c = lifecycleService.createContainer(
                PRACTICE_CONTEST_ID, userId, id, userId,
                image, ext.getEffectivePort(), ext, NetworkMode.ISOLATED, q.getFlag());
        return Result.ok(toContainerVO(c));
    }

    @Operation(summary = "重置练习容器")
    @PostMapping("/challenges/{id}/container/reset")
    public Result<ContainerVO> resetPracticeContainer(@PathVariable Long id) {
        Long userId = UserContext.require().getUserId();
        Question q = requireContainerQuestion(id);
        QuestionExt ext = QuestionExt.parse(q.getExt());
        String image = effectiveImage(q, ext);
        ChallengeContainer c = lifecycleService.resetContainer(
                PRACTICE_CONTEST_ID, userId, id, userId,
                image, ext.getEffectivePort(), ext, NetworkMode.ISOLATED, q.getFlag());
        return Result.ok(toContainerVO(c));
    }

    @Operation(summary = "销毁练习容器")
    @PostMapping("/challenges/{id}/container/destroy")
    public Result<Map<String, Boolean>> destroyPracticeContainer(@PathVariable Long id) {
        Long userId = UserContext.require().getUserId();
        boolean ok = lifecycleService.destroyContainer(PRACTICE_CONTEST_ID, userId, id);
        return Result.ok(Map.of("destroyed", ok));
    }

    @Operation(summary = "续期练习容器")
    @PostMapping("/challenges/{id}/container/extend")
    public Result<ContainerVO> extendPracticeContainer(@PathVariable Long id) {
        Long userId = UserContext.require().getUserId();
        ChallengeContainer c = lifecycleService.extendContainer(PRACTICE_CONTEST_ID, userId, id);
        return Result.ok(toContainerVO(c));
    }

    // ==================== 内部方法 ====================

    private Question requireContainerQuestion(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) throw new BizException(404, "题目不存在");
        QuestionExt ext = QuestionExt.parse(q.getExt());
        if (!ext.dockerEnabled()) {
            throw new BizException(400, "该题为附件题，无需启动容器");
        }
        if (effectiveImage(q, ext) == null) {
            throw new BizException(409, "题目未配置靶机镜像");
        }
        return q;
    }

    private String effectiveImage(Question q, QuestionExt ext) {
        String img = ext.getEffectiveImage();
        if (img == null || img.isBlank()) img = q.getImageName();
        return (img == null || img.isBlank()) ? null : img;
    }

    private String resolveHost() {
        if (publicHost != null && !publicHost.isBlank()) return publicHost;
        String host = httpRequest.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) host = httpRequest.getHeader("Host");
        if (host == null || host.isBlank()) return "127.0.0.1";
        int idx = host.indexOf(':');
        return idx > 0 ? host.substring(0, idx) : host;
    }

    private ContainerVO toContainerVO(ChallengeContainer c) {
        Integer accessPort = c.getPublicPort() != null && c.getPublicPort() > 0
                ? c.getPublicPort() : c.getPort();
        String url = accessPort != null && accessPort > 0
                && !ContainerStatus.DESTROYED.name().equals(c.getStatus())
                && !ContainerStatus.ERROR.name().equals(c.getStatus())
                ? "http://" + resolveHost() + ":" + accessPort : null;
        return ContainerVO.builder()
                .questionId(c.getQuestionId())
                .containerId(c.getContainerId())
                .port(accessPort)
                .accessUrl(url)
                .status(c.getStatus())
                .simulated(Boolean.TRUE.equals(c.getSimulated()))
                .startedAt(c.getStartedAt())
                .expectStopAt(c.getExpectStopAt())
                .checkedAt(LocalDateTime.now())
                .build();
    }

    @Data
    @Builder
    public static class ContainerVO {
        private Long questionId;
        private String containerId;
        private Integer port;
        private String accessUrl;
        private String status;
        private boolean simulated;
        private LocalDateTime startedAt;
        private LocalDateTime expectStopAt;
        private LocalDateTime checkedAt;
    }
}
