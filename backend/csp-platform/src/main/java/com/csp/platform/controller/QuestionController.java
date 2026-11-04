package com.csp.platform.controller;

import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.platform.dto.QuestionQuery;
import com.csp.platform.dto.QuestionSaveCmd;
import com.csp.platform.entity.Question;
import com.csp.platform.service.QuestionService;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.csp.platform.aspect.Audited;

/** 统一题库接口。 */
@Tag(name = "统一题库")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "分页查询题目")
    @RequirePermission("question:view")
    @GetMapping
    public Result<PageResult<Question>> page(@Valid QuestionQuery query) {
        return Result.ok(questionService.page(query));
    }

    @Operation(summary = "题目详情")
    @RequirePermission("question:view")
    @GetMapping("/{id}")
    public Result<Question> get(@PathVariable Long id) {
        return Result.ok(questionService.get(id));
    }

    @Operation(summary = "新建题目")
    @Audited(action = "QUESTION_CREATE", resource = "question")
    @RequirePermission("question:create")
    @PostMapping
    public Result<Question> create(@Valid @RequestBody QuestionSaveCmd cmd) {
        return Result.ok(questionService.create(cmd));
    }

    @Operation(summary = "编辑题目")
    @Audited(action = "QUESTION_UPDATE", resource = "question")
    @RequirePermission("question:update")
    @PutMapping("/{id}")
    public Result<Question> update(@PathVariable Long id, @Valid @RequestBody QuestionSaveCmd cmd) {
        return Result.ok(questionService.update(id, cmd));
    }

    @Operation(summary = "删除题目（逻辑删除）")
    @Audited(action = "QUESTION_DELETE", resource = "question")
    @RequirePermission("question:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "批量导入题目")
    @RequirePermission("question:create")
    @PostMapping("/import")
    public Result<Integer> importBatch(@Valid @RequestBody List<@Valid QuestionSaveCmd> cmds) {
        return Result.ok(questionService.importBatch(cmds));
    }
}
