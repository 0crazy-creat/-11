package com.csp.platform.controller;

import com.csp.common.api.Result;
import com.csp.platform.dto.TeamMemberCmd;
import com.csp.platform.dto.TeamSaveCmd;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.ContestTeamMember;
import com.csp.platform.service.ContestTeamService;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 战队管理接口。 */
@Tag(name = "战队管理")
@RestController
@RequestMapping("/api/contests/{contestId}/teams")
@RequiredArgsConstructor
public class ContestTeamController {

    private final ContestTeamService teamService;

    @Operation(summary = "赛事战队列表")
    @RequirePermission("contest:view")
    @GetMapping
    public Result<List<ContestTeam>> list(@PathVariable Long contestId,
                                          @RequestParam(required = false) String status) {
        return Result.ok(teamService.listByContest(contestId, status));
    }

    @Operation(summary = "战队详情")
    @RequirePermission("contest:view")
    @GetMapping("/{teamId}")
    public Result<ContestTeam> get(@PathVariable Long contestId, @PathVariable Long teamId) {
        return Result.ok(teamService.get(teamId));
    }

    @Operation(summary = "创建战队（报名）")
    @RequirePermission("contest:view")
    @PostMapping
    public Result<ContestTeam> create(@PathVariable Long contestId, @Valid @RequestBody TeamSaveCmd cmd) {
        return Result.ok(teamService.create(contestId, cmd));
    }

    @Operation(summary = "战队审核（通过/驳回/退赛）")
    @RequirePermission("team:manage")
    @PostMapping("/{teamId}/review")
    public Result<ContestTeam> review(@PathVariable Long contestId, @PathVariable Long teamId,
                                      @RequestParam String status) {
        return Result.ok(teamService.reviewTeam(teamId, status));
    }

    @Operation(summary = "战队成员列表")
    @RequirePermission("contest:view")
    @GetMapping("/{teamId}/members")
    public Result<List<ContestTeamMember>> members(@PathVariable Long contestId, @PathVariable Long teamId) {
        return Result.ok(teamService.listMembers(teamId));
    }

    @Operation(summary = "加入战队成员")
    @RequirePermission("team:manage")
    @PostMapping("/{teamId}/members")
    public Result<Void> addMember(@PathVariable Long contestId, @PathVariable Long teamId,
                                  @Valid @RequestBody TeamMemberCmd cmd) {
        teamService.addMember(teamId, cmd);
        return Result.ok();
    }

    @Operation(summary = "移除战队成员")
    @RequirePermission("team:manage")
    @DeleteMapping("/{teamId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long contestId, @PathVariable Long teamId,
                                     @PathVariable Long userId) {
        teamService.removeMember(teamId, userId);
        return Result.ok();
    }
}
