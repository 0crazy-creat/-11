package com.csp.ctf.controller;

import com.csp.common.api.Result;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.dto.BoardDetailVO;
import com.csp.ctf.dto.FirstBloodItem;
import com.csp.ctf.dto.RankItem;
import com.csp.ctf.dto.StageVO;
import com.csp.ctf.dto.TimelineVO;
import com.csp.ctf.entity.Scoreboard;
import com.csp.ctf.service.ContestEnrollService;
import com.csp.ctf.service.ScoreboardService;
import com.csp.security.annotation.RequirePermission;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** 排行榜：实时 TopN、阶段榜、首杀榜、我的名次、刷新排名。 */
@Tag(name = "CTF 排行榜")
@RestController
@RequestMapping("/api/ctf/contests/{contestId}/scoreboard")
@RequiredArgsConstructor
public class ScoreboardController {

    private final ScoreboardService scoreboardService;
    private final ContestEnrollService enrollService;

    @Operation(summary = "排行榜 TopN（实时 Redis 优先，回落数据库）")
    @GetMapping
    public Result<List<RankItem>> top(@PathVariable Long contestId,
                                      @RequestParam(defaultValue = "50") int topN) {
        return Result.ok(scoreboardService.topN(contestId, clampTop(topN)));
    }

    @Operation(summary = "榜单明细：总分榜/阶段榜 + 题目表头 + 解题矩阵（一血标识）")
    @GetMapping("/detail")
    public Result<BoardDetailVO> detail(@PathVariable Long contestId,
                                        @RequestParam(defaultValue = "100") int topN,
                                        @RequestParam(required = false) String asOf) {
        return Result.ok(scoreboardService.detail(contestId, clampTop(topN), parseAsOf(asOf)));
    }

    @Operation(summary = "阶段列表（按比赛时间窗切片，作为阶段榜/趋势图的时间轴）")
    @GetMapping("/stages")
    public Result<List<StageVO>> stages(@PathVariable Long contestId,
                                       @RequestParam(defaultValue = "0") int slices) {
        int count = slices > 0 ? slices : scoreboardService.defaultSlices(contestId);
        return Result.ok(scoreboardService.stages(contestId, count));
    }

    @Operation(summary = "阶段榜：截至指定阶段末的累计榜")
    @GetMapping("/stage")
    public Result<List<RankItem>> stage(@PathVariable Long contestId,
                                        @RequestParam(defaultValue = "1") int index,
                                        @RequestParam(defaultValue = "0") int slices,
                                        @RequestParam(defaultValue = "100") int topN) {
        int count = slices > 0 ? slices : scoreboardService.defaultSlices(contestId);
        return Result.ok(scoreboardService.stageBoard(contestId, Math.max(1, index), count, clampTop(topN)));
    }

    @Operation(summary = "首杀榜：每题首个正确提交的战队与解题者")
    @GetMapping("/first-blood")
    public Result<List<FirstBloodItem>> firstBlood(@PathVariable Long contestId) {
        return Result.ok(scoreboardService.firstBloodBoard(contestId));
    }

    @Operation(summary = "分数趋势：Top 战队在各阶段的累计得分")
    @GetMapping("/timeline")
    public Result<TimelineVO> timeline(@PathVariable Long contestId,
                                                      @RequestParam(defaultValue = "10") int traces,
                                                      @RequestParam(defaultValue = "0") int slices) {
        int count = slices > 0 ? slices : scoreboardService.defaultSlices(contestId);
        return Result.ok(scoreboardService.timeline(contestId, traces, count));
    }

    @Operation(summary = "我的战队成绩")
    @GetMapping("/mine")
    public Result<Scoreboard> mine(@PathVariable Long contestId) {
        LoginUser u = UserContext.get();
        if (u == null) return Result.ok(null);
        var team = enrollService.myTeam(u.getUserId(), contestId);
        if (team == null) return Result.ok(null);
        return Result.ok(scoreboardService.get(contestId, team.getId()));
    }

    @Operation(summary = "刷新排名号（管理员/裁判）")
    @RequirePermission("contest:control")
    @PostMapping("/refresh")
    public Result<Void> refresh(@PathVariable Long contestId) {
        scoreboardService.refreshRanks(contestId);
        return Result.ok();
    }

    private int clampTop(int n) {
        return Math.max(1, Math.min(n, 500));
    }

    /** 兼容 ISO（2026-11-05T12:00:00）与空格（2026-11-05 12:00:00）两种时间写法。 */
    private LocalDateTime parseAsOf(String asOf) {
        if (asOf == null || asOf.isBlank()) return null;
        String text = asOf.trim().replace(' ', 'T');
        if (text.length() == 16) text = text + ":00";
        try {
            return LocalDateTime.parse(text);
        } catch (Exception e) {
            throw new BizException(ResultCode.BAD_REQUEST, "时间格式不正确：" + asOf);
        }
    }
}
