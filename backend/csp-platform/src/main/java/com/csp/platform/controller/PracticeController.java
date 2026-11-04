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
        
        // 隐藏敏感字段
        result.getRecords().forEach(q -> {
            q.setFlag(null);
            q.setAnswer(null);
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
        // 隐藏敏感字段
        question.setFlag(null);
        question.setAnswer(null);
        return Result.ok(question);
    }

    @Operation(summary = "获取题目分类列表")
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        List<String> categories = List.of("Web", "Misc", "Crypto", "Reverse", "Pwn");
        return Result.ok(categories);
    }
}
