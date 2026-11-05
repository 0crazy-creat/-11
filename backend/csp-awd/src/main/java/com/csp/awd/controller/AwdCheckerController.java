package com.csp.awd.controller;

import com.csp.awd.entity.AwdChecker;
import com.csp.awd.service.AwdCheckerService;
import com.csp.common.api.Result;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** AWD checker 管理：配置 + 手动触发检测。 */
@Tag(name = "AWD checker")
@RestController
@RequestMapping("/api/awd/checkers")
@RequiredArgsConstructor
public class AwdCheckerController {

    private final AwdCheckerService checkerService;

    @Operation(summary = "checker 列表")
    @RequirePermission("contest:view")
    @GetMapping
    public Result<List<AwdChecker>> list(@RequestParam Long contestId) {
        return Result.ok(checkerService.list(contestId));
    }

    @Operation(summary = "新建/更新 checker")
    @RequirePermission("contest:update")
    @PostMapping
    public Result<AwdChecker> save(@RequestBody AwdChecker cmd) {
        return Result.ok(checkerService.save(cmd));
    }

    @Operation(summary = "删除 checker")
    @RequirePermission("contest:update")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkerService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "立即执行一轮检测")
    @RequirePermission("contest:control")
    @PostMapping("/run")
    public Result<Map<String, Object>> run(@RequestParam Long contestId) {
        return Result.ok(checkerService.runContest(contestId));
    }
}
