package com.csp.platform.controller;

import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.platform.entity.Writeup;
import com.csp.platform.service.WriteupService;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 题解（Writeup）：选手提交、公开浏览、管理端审核。 */
@Tag(name = "题解 Writeup")
@RestController
@RequestMapping("/api/writeups")
@RequiredArgsConstructor
public class WriteupController {

    private final WriteupService writeupService;

    @Operation(summary = "提交题解（待审核）")
    @PostMapping
    public Result<Writeup> submit(@RequestBody Writeup cmd) {
        return Result.ok(writeupService.submit(cmd));
    }

    @Operation(summary = "公开题解列表（仅已审核通过）")
    @GetMapping
    public Result<PageResult<Writeup>> page(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) Long questionId,
                                            @RequestParam(required = false) Long contestId,
                                            @RequestParam(required = false) String keyword) {
        return Result.ok(writeupService.publicPage(page, size, questionId, contestId, keyword));
    }

    @Operation(summary = "我的题解（含待审核/被拒）")
    @GetMapping("/mine")
    public Result<List<Writeup>> mine() {
        return Result.ok(writeupService.mine());
    }

    @Operation(summary = "题解详情（公开的或自己的）")
    @GetMapping("/{id}")
    public Result<Writeup> detail(@PathVariable Long id) {
        return Result.ok(writeupService.detail(id));
    }

    @Operation(summary = "点赞题解")
    @PostMapping("/{id}/like")
    public Result<Map<String, Integer>> like(@PathVariable Long id) {
        return Result.ok(Map.of("likeCount", writeupService.like(id)));
    }

    @Operation(summary = "审核列表（管理端）")
    @RequirePermission("writeup:review")
    @GetMapping("/review")
    public Result<PageResult<Writeup>> reviewPage(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String status) {
        return Result.ok(writeupService.reviewPage(page, size, status));
    }

    @Operation(summary = "审核题解（通过/拒绝）")
    @RequirePermission("writeup:review")
    @PostMapping("/{id}/review")
    public Result<Writeup> review(@PathVariable Long id,
                                  @RequestParam String status,
                                  @RequestParam(required = false) String remark) {
        return Result.ok(writeupService.review(id, status, remark));
    }

    @Operation(summary = "删除我的题解")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        writeupService.delete(id);
        return Result.ok();
    }
}
