package com.csp.ctf.controller;

import com.csp.common.api.Result;
import com.csp.common.exception.BizException;
import com.csp.ctf.dto.TeamCreateRequest;
import com.csp.ctf.dto.TeamJoinRequest;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.ContestTeamMember;
import com.csp.ctf.service.ContestEnrollService;
import com.csp.security.annotation.RequirePermission;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CTF 报名：建队 / 加入 / 退出 / 审核。 */
@Tag(name = "CTF 报名")
@RestController
@RequestMapping("/api/ctf/contests/{contestId}/teams")
@RequiredArgsConstructor
public class CtfEnrollController {

    private final ContestEnrollService enrollService;

    @Operation(summary = "创建战队（自动成为队长并报名）")
    @PostMapping
    public Result<ContestTeam> create(@PathVariable Long contestId,
                                      @Valid @RequestBody TeamCreateRequest req) {
        req.setContestId(contestId);
        return Result.ok(enrollService.createTeam(currentUserId(), req));
    }

    @Operation(summary = "加入战队")
    @PostMapping("/join")
    public Result<ContestTeam> join(@PathVariable Long contestId,
                                    @Valid @RequestBody TeamJoinRequest req) {
        return Result.ok(enrollService.joinTeam(currentUserId(), contestId, req.getTeamId()));
    }

    @Operation(summary = "退出/取消报名")
    @DeleteMapping("/quit")
    public Result<Void> quit(@PathVariable Long contestId) {
        enrollService.quit(currentUserId(), contestId);
        return Result.ok();
    }

    @Operation(summary = "我的战队")
    @GetMapping("/mine")
    public Result<ContestTeam> mine(@PathVariable Long contestId) {
        return Result.ok(enrollService.myTeam(currentUserId(), contestId));
    }

    @Operation(summary = "战队成员列表")
    @GetMapping("/{teamId}/members")
    public Result<List<ContestTeamMember>> members(@PathVariable Long contestId,
                                                   @PathVariable Long teamId) {
        return Result.ok(enrollService.teamMembers(teamId));
    }

    @Operation(summary = "审核战队（管理员/裁判）")
    @RequirePermission("contest:control")
    @PostMapping("/{teamId}/review")
    public Result<Void> review(@PathVariable Long contestId,
                               @PathVariable Long teamId,
                               @RequestParam String status) {
        enrollService.review(contestId, teamId, status);
        return Result.ok();
    }

    @Operation(summary = "生成/获取邀请码")
    @PostMapping("/{teamId}/invite-code")
    public Result<String> generateInviteCode(@PathVariable Long contestId,
                                             @PathVariable Long teamId) {
        // 验证当前用户是队长
        ContestTeam team = enrollService.myTeam(currentUserId(), contestId);
        if (team == null || !team.getId().equals(teamId)) {
            throw BizException.forbidden("无权操作该战队");
        }
        if (!team.getCaptainId().equals(currentUserId())) {
            throw BizException.forbidden("仅队长可生成邀请码");
        }
        
        String code = enrollService.generateInviteCode(teamId);
        return Result.ok(code);
    }

    @Operation(summary = "通过邀请码加入战队")
    @PostMapping("/join-by-code")
    public Result<ContestTeam> joinByCode(@PathVariable Long contestId,
                                          @RequestParam String inviteCode) {
        return Result.ok(enrollService.joinByInviteCode(currentUserId(), contestId, inviteCode));
    }

    @Operation(summary = "刷新邀请码（重新生成）")
    @PutMapping("/{teamId}/invite-code")
    public Result<String> refreshInviteCode(@PathVariable Long contestId,
                                            @PathVariable Long teamId) {
        return Result.ok(enrollService.refreshInviteCode(teamId, currentUserId()));
    }

    private Long currentUserId() {
        LoginUser u = UserContext.get();
        return u == null ? null : u.getUserId();
    }
}