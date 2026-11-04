package com.csp.ctf.controller;

import com.csp.common.api.Result;
import com.csp.ctf.dto.RankItem;
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

import java.util.List;

/** 排行榜：实时 TopN、我的名次、刷新排名。 */
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
        return Result.ok(scoreboardService.topN(contestId, Math.max(1, Math.min(topN, 500))));
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
}