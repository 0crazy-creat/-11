package com.csp.awd.controller;

import com.csp.awd.dto.RoundVO;
import com.csp.awd.dto.ScoreVO;
import com.csp.awd.dto.TargetVO;
import com.csp.awd.entity.AwdRound;
import com.csp.awd.service.AwdRoundService;
import com.csp.awd.service.AwdScoreService;
import com.csp.awd.service.AwdTargetService;
import com.csp.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AWD 前端兼容接口：把前端 {@code /api/awd/contests/{contestId}/*} 形式的调用
 * 映射到既有 {@code /api/awd/score}、{@code /api/awd/rounds}、{@code /api/awd/targets} 实现，
 * 避免前端页面因路径不一致而 404/500。
 */
@Tag(name = "AWD-前端兼容", description = "按赛事聚合的只读视图，供态势/大屏页面调用")
@RestController
@RequestMapping("/api/awd/contests/{contestId}")
@RequiredArgsConstructor
public class AwdCompatController {

    private final AwdScoreService scoreService;
    private final AwdRoundService roundService;
    private final AwdTargetService targetService;

    @Operation(summary = "赛事总览（队伍/轮次/榜单/靶机）")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(@PathVariable Long contestId,
                                                @RequestParam(defaultValue = "10") int topN) {
        List<ScoreVO> board = scoreService.leaderboard(contestId, topN);
        List<RoundVO> rounds = roundService.listRounds(contestId);
        AwdRound cur = roundService.currentRunning(contestId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("contestId", contestId);
        m.put("teamCount", board == null ? 0 : board.size());
        m.put("top", board);
        m.put("rounds", rounds);
        m.put("currentRound", cur == null ? null : roundService.toVO(cur));
        m.put("targetCount", targetService.listByContest(contestId, null).size());
        return Result.ok(m);
    }

    @Operation(summary = "参赛队伍积分列表")
    @GetMapping("/teams")
    public Result<List<ScoreVO>> teams(@PathVariable Long contestId,
                                       @RequestParam(defaultValue = "50") int topN) {
        return Result.ok(scoreService.leaderboard(contestId, topN));
    }

    @Operation(summary = "赛事排行榜（别名）")
    @GetMapping("/scoreboard")
    public Result<List<ScoreVO>> scoreboard(@PathVariable Long contestId,
                                            @RequestParam(defaultValue = "50") int topN) {
        return Result.ok(scoreService.leaderboard(contestId, topN));
    }

    @Operation(summary = "轮次列表")
    @GetMapping("/rounds")
    public Result<List<RoundVO>> rounds(@PathVariable Long contestId) {
        return Result.ok(roundService.listRounds(contestId));
    }

    @Operation(summary = "靶机列表")
    @GetMapping("/targets")
    public Result<List<TargetVO>> targets(@PathVariable Long contestId,
                                          @RequestParam(required = false) Long teamId) {
        return Result.ok(targetService.listByContest(contestId, teamId));
    }

    @Operation(summary = "攻击流水（按赛事聚合，供前端日志面板）")
    @GetMapping("/attacks")
    public Result<List<Map<String, Object>>> attacks(@PathVariable Long contestId) {
        // 攻击流水由 Redis 记录，此处返回空列表占位，避免前端页面 500
        return Result.ok(List.of());
    }
}
