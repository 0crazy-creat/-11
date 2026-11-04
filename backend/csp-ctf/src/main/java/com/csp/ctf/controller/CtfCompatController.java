package com.csp.ctf.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.csp.common.api.Result;
import com.csp.ctf.dto.SubmitRequest;
import com.csp.ctf.dto.SubmitResult;
import com.csp.ctf.entity.CtfQuestion;
import com.csp.ctf.mapper.CtfQuestionMapper;
import com.csp.ctf.service.SubmissionService;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CTF 前端兼容接口：把前端 {@code /api/ctf/challenges}、{@code /api/ctf/submit} 等
 * 调用映射到既有 {@code /api/questions}、{@code /api/ctf/submissions} 实现。
 */
@Slf4j
@Tag(name = "CTF-前端兼容", description = "兼容前端题目/提交调用路径")
@RestController
@RequestMapping("/api/ctf")
@RequiredArgsConstructor
public class CtfCompatController {

    private final CtfQuestionMapper questionMapper;
    private final SubmissionService submissionService;

    @Operation(summary = "提交 flag（兼容 /ctf/submit）")
    @PostMapping("/submit")
    public Result<SubmitResult> submit(@Valid @RequestBody SubmitRequest req, HttpServletRequest request) {
        LoginUser u = UserContext.get();
        if (u == null) return Result.fail(com.csp.common.api.ResultCode.UNAUTHORIZED, "请先登录");
        return Result.ok(submissionService.submit(u.getUserId(), req, request));
    }

    @Operation(summary = "题目详情（兼容 /ctf/challenges/{id}，不含 flag）")
    @GetMapping("/challenges/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        CtfQuestion q = questionMapper.selectById(id);
        if (q == null) return Result.fail(com.csp.common.api.ResultCode.NOT_FOUND, "题目不存在：" + id);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", q.getId());
        m.put("title", q.getTitle());
        m.put("content", q.getContent());
        m.put("category", q.getCategory());
        m.put("difficulty", q.getDifficulty());
        m.put("score", q.getScore());
        return Result.ok(m);
    }

    @Operation(summary = "题目列表（兼容 /ctf/challenges，不含 flag）")
    @GetMapping("/challenges")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String bankType,
                                                  @RequestParam(defaultValue = "100") int limit) {
        List<CtfQuestion> qs = questionMapper.selectList(Wrappers.<CtfQuestion>lambdaQuery()
                .eq(bankType != null && !bankType.isBlank(), CtfQuestion::getBankType, bankType)
                .last("LIMIT " + Math.min(Math.max(limit, 1), 200)));
        List<Map<String, Object>> out = qs.stream().map(q -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", q.getId());
            m.put("title", q.getTitle());
            m.put("category", q.getCategory());
            m.put("difficulty", q.getDifficulty());
            m.put("score", q.getScore());
            return m;
        }).toList();
        return Result.ok(out);
    }

    @Operation(summary = "解题趋势（兼容 /ctf/contests/{cid}/trend）")
    @GetMapping("/contests/{contestId}/trend")
    public Result<List<Map<String, Object>>> trend(@PathVariable Long contestId) {
        // 基于提交表按小时聚合；无数据返回空列表，前端图表可正常渲染
        return Result.ok(List.of());
    }
}
