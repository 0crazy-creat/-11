package com.csp.awd.controller;

import com.csp.awd.dto.CreateRoundRequest;
import com.csp.awd.dto.RoundVO;
import com.csp.awd.entity.AwdRound;
import com.csp.awd.engine.RoundStateMachine;
import com.csp.awd.enums.RoundStatus;
import com.csp.awd.service.AwdRoundService;
import com.csp.common.api.Result;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** AWD 轮次编排接口：裁判控制台使用。 */
@Tag(name = "AWD-轮次编排", description = "轮次创建、状态机流转、自动推进")
@RestController
@RequestMapping("/api/awd/rounds")
@RequiredArgsConstructor
public class AwdRoundController {

    private final AwdRoundService roundService;

    @Operation(summary = "创建/编排轮次")
    @RequirePermission("awd:control")
    @PostMapping
    public Result<RoundVO> create(@Valid @RequestBody CreateRoundRequest req) {
        return Result.ok(roundService.createRound(req));
    }

    @Operation(summary = "轮次列表")
    @RequirePermission(value = {"awd:control", "contest:view"}, logical = RequirePermission.Logical.OR)
    @GetMapping
    public Result<List<RoundVO>> list(@RequestParam Long contestId) {
        return Result.ok(roundService.listRounds(contestId));
    }

    @Operation(summary = "轮次详情")
    @RequirePermission(value = {"awd:control", "contest:view"}, logical = RequirePermission.Logical.OR)
    @GetMapping("/{contestId}/{roundNo}")
    public Result<RoundVO> detail(@PathVariable Long contestId, @PathVariable Integer roundNo) {
        return Result.ok(roundService.toVO(roundService.requireRound(contestId, roundNo)));
    }

    @Operation(summary = "当前进行中轮次")
    @RequirePermission(value = {"awd:control", "contest:view"}, logical = RequirePermission.Logical.OR)
    @GetMapping("/{contestId}/current")
    public Result<RoundVO> current(@PathVariable Long contestId) {
        AwdRound r = roundService.currentRunning(contestId);
        return Result.ok(r == null ? null : roundService.toVO(r));
    }

    // begin 语义已由 com.csp.awd.round.controller.AwdRoundControlController 统一承载（含状态机+CAS+审计），
    // 此处不再重复映射 /begin，避免 Spring Ambiguous mapping。

    // running 语义已由 com.csp.awd.round.controller.AwdRoundControlController 统一承载
    // （GET /api/awd/rounds/{contestId}/{roundNo}/running），此处不再重复映射。

    @Operation(summary = "开局（PENDING → RUNNING）")
    @RequirePermission("awd:control")
    @PostMapping("/{contestId}/{roundNo}/start")
    public Result<RoundVO> start(@PathVariable Long contestId, @PathVariable Integer roundNo) {
        return Result.ok(roundService.start(contestId, roundNo));
    }

    @Operation(summary = "进入检查阶段（RUNNING → CHECKING）")
    @RequirePermission("awd:control")
    @PostMapping("/{contestId}/{roundNo}/check")
    public Result<RoundVO> check(@PathVariable Long contestId, @PathVariable Integer roundNo) {
        return Result.ok(roundService.check(contestId, roundNo));
    }

    @Operation(summary = "结算（CHECKING → FINISHED）")
    @RequirePermission("awd:control")
    @PostMapping("/{contestId}/{roundNo}/finish")
    public Result<RoundVO> finish(@PathVariable Long contestId, @PathVariable Integer roundNo) {
        return Result.ok(roundService.finish(contestId, roundNo));
    }

    @Operation(summary = "中止轮次")
    @RequirePermission("awd:control")
    @PostMapping("/{contestId}/{roundNo}/abort")
    public Result<RoundVO> abort(@PathVariable Long contestId, @PathVariable Integer roundNo) {
        return Result.ok(roundService.abort(contestId, roundNo));
    }

    @Operation(summary = "查询状态机：当前状态可迁移到哪些状态")
    @RequirePermission("awd:control")
    @GetMapping("/{contestId}/{roundNo}/next-states")
    public Result<Map<String, Object>> nextStates(@PathVariable Long contestId, @PathVariable Integer roundNo) {
        RoundStatus cur = RoundStateMachine.parse(roundService.requireRound(contestId, roundNo).getStatus());
        Set<RoundStatus> next = RoundStateMachine.nextStates(cur);
        return Result.ok(Map.of(
                "current", cur.name(),
                "terminal", cur.isTerminal(),
                "next", next.stream().map(Enum::name).toList()));
    }

    @Operation(summary = "手动触发一次自动推进扫描（运维用）")
    @RequirePermission("awd:control")
    @PostMapping("/tick")
    public Result<Map<String, Integer>> tick() {
        return Result.ok(Map.of("advanced", roundService.tick()));
    }
}