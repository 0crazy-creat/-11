package com.csp.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.Result;
import com.csp.platform.entity.Question;
import com.csp.platform.mapper.QuestionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 题目练习接口（公开，无需权限）。
 */
@Tag(name = "题目练习")
@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final QuestionMapper questionMapper;

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
        
        // 分类筛选
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Question::getCategory, category);
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
        
        // 隐藏敏感字段（flag / 答案 / 题解）
        // 注意：writeup 正文中常直接包含 flag，必须一并置空，否则列表接口会泄漏答案
        result.getRecords().forEach(q -> {
            q.setFlag(null);
            q.setAnswer(null);
            q.setWriteup(null);
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
        return Result.ok(Map.of(
                "correct", correct,
                "message", correct ? "答案正确！" : "Flag 不正确，请重试",
                "score", correct ? (question.getScore() == null ? 0 : question.getScore()) : 0
        ));
    }
}
