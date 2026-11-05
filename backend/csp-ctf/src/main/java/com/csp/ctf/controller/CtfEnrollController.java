package com.csp.ctf.controller;

import com.csp.common.api.Result;
import com.csp.common.security.LoginUser;
import com.csp.ctf.dto.EnrollCmd;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.ContestTeamMember;
import com.csp.ctf.service.ContestEnrollService;
import com.csp.security.annotation.RequirePermission;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CTF 参赛报名。
 *
 * <p>战队管理已拆到全局战队模块（{@code /api/teams}），此处只做「个人 / 战队」二选一的报名，
 * 战队报名仅队长可提交。</p>
 */
@Tag(name = "CTF 报名")
@RestController
@RequestMapping("/api/ctf/contests/{contestId}/teams")
@RequiredArgsConstructor
public class CtfEnrollController {

    private final ContestEnrollService enrollService;

    @Operation(summary = "报名可选项（赛事模式 + 我担任队长的战队 + 当前参赛状态）")
    @GetMapping("/options")
    public Result<Map<String, Object>> options(@PathVariable Long contestId) {
        return Result.ok(enrollService.options(currentUserId(), contestId));
    }

    @Operation(summary = "报名参赛（mode=INDIVIDUAL 个人 / TEAM 战队，战队仅队长可报名）")
    @PostMapping("/enroll")
    public Result<ContestTeam> enroll(@PathVariable Long contestId, @RequestBody EnrollCmd cmd) {
        LoginUser u = requireUser();
        if ("TEAM".equalsIgnoreCase(cmd.getMode())) {
            return Result.ok(enrollService.enrollTeam(u.getUserId(), contestId, cmd.getTeamId()));
        }
        return Result.ok(enrollService.enrollIndividual(u.getUserId(), contestId, displayName(u)));
    }

    @Operation(summary = "个人赛报名")
    @PostMapping("/enroll-individual")
    public Result<ContestTeam> enrollIndividual(@PathVariable Long contestId) {
        LoginUser u = requireUser();
        return Result.ok(enrollService.enrollIndividual(u.getUserId(), contestId, displayName(u)));
    }

    @Operation(summary = "取消报名（队长取消即整队撤赛）")
    @DeleteMapping("/quit")
    public Result<Void> quit(@PathVariable Long contestId) {
        enrollService.quit(currentUserId(), contestId);
        return Result.ok();
    }

    @Operation(summary = "我的参赛记录")
    @GetMapping("/mine")
    public Result<ContestTeam> mine(@PathVariable Long contestId) {
        return Result.ok(enrollService.myTeam(currentUserId(), contestId));
    }

    @Operation(summary = "参赛队伍成员列表")
    @GetMapping("/{teamId}/members")
    public Result<List<ContestTeamMember>> members(@PathVariable Long contestId,
                                                   @PathVariable Long teamId) {
        enrollService.requireEnroll(contestId, teamId);
        return Result.ok(enrollService.teamMembers(teamId));
    }

    @Operation(summary = "审核参赛队伍（管理员/裁判）")
    @RequirePermission("contest:control")
    @PostMapping("/{teamId}/review")
    public Result<Void> review(@PathVariable Long contestId,
                               @PathVariable Long teamId,
                               @RequestParam String status) {
        enrollService.review(contestId, teamId, status);
        return Result.ok();
    }

    private LoginUser requireUser() {
        LoginUser u = UserContext.get();
        if (u == null || u.getUserId() == null) throw new com.csp.common.exception.BizException(
                com.csp.common.api.ResultCode.FORBIDDEN, "请先登录");
        return u;
    }

    private String displayName(LoginUser u) {
        if (u.getNickname() != null && !u.getNickname().isBlank()) return u.getNickname();
        return u.getUsername() != null ? u.getUsername() : ("用户" + u.getUserId());
    }

    private Long currentUserId() {
        return requireUser().getUserId();
    }
}
