package com.csp.platform.controller;

import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.platform.dto.*;
import com.csp.platform.entity.ContestInfo;
import com.csp.platform.service.ContestService;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.csp.platform.aspect.Audited;

/** 赛事管理接口。 */
@Tag(name = "赛事管理")
@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    @Operation(summary = "分页查询赛事")
    @GetMapping
    public Result<PageResult<ContestInfo>> page(@Valid ContestQuery query) {
        return Result.ok(contestService.page(query));
    }

    @Operation(summary = "赛事详情")
    @GetMapping("/{id}")
    public Result<ContestInfo> get(@PathVariable Long id) {
        return Result.ok(contestService.get(id));
    }

    @Operation(summary = "按编码查询赛事")
    @RequirePermission("contest:view")
    @GetMapping("/code/{code}")
    public Result<ContestInfo> getByCode(@PathVariable String code) {
        return Result.ok(contestService.getByCode(code));
    }

    @Operation(summary = "创建赛事")
    @Audited(action = "CONTEST_CREATE", resource = "contest")
    @RequirePermission("contest:create")
    @PostMapping
    public Result<ContestInfo> create(@Valid @RequestBody ContestSaveCmd cmd) {
        return Result.ok(contestService.create(cmd));
    }

    @Operation(summary = "编辑赛事")
    @Audited(action = "CONTEST_UPDATE", resource = "contest")
    @RequirePermission("contest:update")
    @PutMapping("/{id}")
    public Result<ContestInfo> update(@PathVariable Long id, @Valid @RequestBody ContestSaveCmd cmd) {
        return Result.ok(contestService.update(id, cmd));
    }

    @Operation(summary = "删除赛事")
    @Audited(action = "CONTEST_DELETE", resource = "contest")
    @RequirePermission("contest:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contestService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "赛事状态流转")
    @Audited(action = "CONTEST_STATUS", resource = "contest")
    @RequirePermission("contest:control")
    @PostMapping("/{id}/status")
    public Result<ContestInfo> changeStatus(@PathVariable Long id, @Valid @RequestBody ContestStatusCmd cmd) {
        return Result.ok(contestService.changeStatus(id, cmd));
    }

    @Operation(summary = "赛事组卷（绑定题目）")
    @Audited(action = "CONTEST_BIND_QUESTIONS", resource = "contest")
    @RequirePermission("contest:update")
    @PostMapping("/{id}/questions")
    public Result<Void> bindQuestions(@PathVariable Long id, @Valid @RequestBody ContestQuestionBindCmd cmd) {
        contestService.bindQuestions(id, cmd);
        return Result.ok();
    }

    @Operation(summary = "赛事题目列表")
    @RequirePermission("contest:view")
    @GetMapping("/{id}/questions")
    public Result<List<ContestQuestionVO>> listQuestions(@PathVariable Long id,
                                                         @RequestParam(defaultValue = "false") boolean includeHidden) {
        return Result.ok(contestService.listQuestions(id, includeHidden));
    }

    @Operation(summary = "赛事移除题目")
    @RequirePermission("contest:update")
    @DeleteMapping("/{id}/questions/{questionId}")
    public Result<Void> unbindQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        contestService.unbindQuestion(id, questionId);
        return Result.ok();
    }
}
