package com.csp.awd.controller;

import com.csp.awd.entity.AwdTrafficAnalysis;
import com.csp.awd.entity.AwdTrafficCapture;
import com.csp.awd.service.TrafficCaptureService;
import com.csp.common.api.Result;
import com.csp.common.api.ResultCode;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** AWD 流量取证：捕获管理、分析结果查询。 */
@Tag(name = "AWD 流量取证")
@RestController
@RequestMapping("/api/awd/contests/{contestId}/traffic")
@RequiredArgsConstructor
public class AwdTrafficController {

    private final TrafficCaptureService captureService;

    @Operation(summary = "启动流量捕获（裁判/管理员）")
    @RequirePermission("contest:control")
    @PostMapping("/capture/start")
    public Result<Long> startCapture(@PathVariable Long contestId,
                                      @RequestParam Integer roundNo,
                                      @RequestParam Long teamId,
                                      @RequestParam Long targetId) {
        Long captureId = captureService.startCapture(contestId, roundNo, teamId, targetId);
        return Result.ok(captureId);
    }

    @Operation(summary = "停止流量捕获（裁判/管理员）")
    @RequirePermission("contest:control")
    @PostMapping("/capture/{captureId}/stop")
    public Result<Void> stopCapture(@PathVariable Long contestId,
                                     @PathVariable Long captureId) {
        captureService.stopCapture(captureId);
        return Result.ok();
    }

    @Operation(summary = "查询捕获记录列表")
    @GetMapping("/captures")
    public Result<List<AwdTrafficCapture>> listCaptures(@PathVariable Long contestId,
                                                         @RequestParam Integer roundNo,
                                                         @RequestParam Long teamId) {
        return Result.ok(captureService.listCaptures(contestId, roundNo, teamId));
    }

    @Operation(summary = "查询捕获记录详情")
    @GetMapping("/capture/{captureId}")
    public Result<AwdTrafficCapture> getCapture(@PathVariable Long contestId,
                                                 @PathVariable Long captureId) {
        AwdTrafficCapture capture = captureService.getCapture(captureId);
        if (capture == null) {
            return Result.fail(ResultCode.NOT_FOUND, "捕获记录不存在");
        }
        return Result.ok(capture);
    }

    @Operation(summary = "查询分析结果")
    @GetMapping("/capture/{captureId}/analysis")
    public Result<List<AwdTrafficAnalysis>> getAnalysis(@PathVariable Long contestId,
                                                         @PathVariable Long captureId) {
        return Result.ok(captureService.getAnalysisResults(captureId));
    }

    @Operation(summary = "手动触发分析（裁判/管理员）")
    @RequirePermission("contest:control")
    @PostMapping("/capture/{captureId}/analyze")
    public Result<Void> triggerAnalysis(@PathVariable Long contestId,
                                         @PathVariable Long captureId) {
        captureService.triggerAnalysis(captureId);
        return Result.ok();
    }

    @Operation(summary = "清理过期流量文件（管理员）")
    @RequirePermission("system:admin")
    @DeleteMapping("/cleanup")
    public Result<Integer> cleanupExpired(@PathVariable Long contestId) {
        int count = captureService.cleanupExpired();
        return Result.ok(count);
    }
}
