package com.csp.ctf.controller;

import com.csp.common.api.Result;
import com.csp.ctf.entity.CheatAuditLog;
import com.csp.ctf.service.AntiCheatAnalysisService;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** CTF 反作弊分析：风险查询、审计日志。 */
@Tag(name = "CTF 反作弊分析")
@RestController
@RequestMapping("/api/ctf/contests/{contestId}/anti-cheat")
@RequiredArgsConstructor
public class CtfAntiCheatController {

    private final AntiCheatAnalysisService analysisService;

    @Operation(summary = "查询用户风险摘要（管理员/裁判）")
    @RequirePermission("contest:control")
    @GetMapping("/users/{userId}/risk")
    public Result<Map<String, Object>> getUserRiskSummary(@PathVariable Long contestId,
                                                           @PathVariable Long userId) {
        return Result.ok(analysisService.getUserRiskSummary(contestId, userId));
    }

    @Operation(summary = "查询赛事高风险用户列表（管理员/裁判）")
    @RequirePermission("contest:control")
    @GetMapping("/high-risk")
    public Result<List<CheatAuditLog>> getHighRiskUsers(@PathVariable Long contestId,
                                                         @RequestParam(defaultValue = "60") int minRiskScore) {
        return Result.ok(analysisService.getHighRiskUsers(contestId, minRiskScore));
    }
}
