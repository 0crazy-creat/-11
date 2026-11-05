package com.csp.ctf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.common.exception.BizException;
import com.csp.common.security.LoginUser;
import com.csp.ctf.dto.TeamProfileCmd;
import com.csp.ctf.entity.Team;
import com.csp.ctf.mapper.TeamMapper;
import com.csp.ctf.service.GlobalTeamService;
import com.csp.ctf.vo.TeamMemberVO;
import com.csp.security.annotation.RequirePermission;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 全局战队模块：与赛事解耦，用户在此建队、入队、管理成员。
 *
 * <p>参赛报名（个人/战队）在 {@code CtfEnrollController}。</p>
 */
@Tag(name = "战队（全局）")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class GlobalTeamController {

    private final GlobalTeamService teamService;
    private final TeamMapper teamMapper;

    @Operation(summary = "与我相关的全部战队")
    @GetMapping
    public Result<List<Team>> mine() {
        return Result.ok(teamService.myTeams(currentUserId()));
    }

    @Operation(summary = "创建战队（创建者自动成为队长）")
    @PostMapping
    public Result<Team> create(@Valid @RequestBody TeamProfileCmd cmd) {
        return Result.ok(teamService.create(currentUserId(), cmd));
    }

    @Operation(summary = "浏览全部战队")
    @GetMapping("/browse")
    public Result<List<Team>> browse(@RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "50") Integer limit) {
        return Result.ok(teamService.browse(currentUserId(), keyword, limit));
    }

    @Operation(summary = "通过邀请码加入战队")
    @PostMapping("/join")
    public Result<Team> join(@RequestParam String inviteCode) {
        return Result.ok(teamService.joinByCode(currentUserId(), inviteCode));
    }

    @Operation(summary = "战队详情")
    @GetMapping("/{id}")
    public Result<Team> detail(@PathVariable Long id) {
        return Result.ok(teamService.detail(currentUserId(), id));
    }

    @Operation(summary = "战队成员列表")
    @GetMapping("/{id}/members")
    public Result<List<TeamMemberVO>> members(@PathVariable Long id) {
        return Result.ok(teamService.members(id));
    }

    @Operation(summary = "更新战队资料（队长）")
    @PutMapping("/{id}")
    public Result<Team> update(@PathVariable Long id, @Valid @RequestBody TeamProfileCmd cmd) {
        return Result.ok(teamService.update(currentUserId(), id, cmd));
    }

    @Operation(summary = "退出战队")
    @DeleteMapping("/{id}/members/me")
    public Result<Void> quit(@PathVariable Long id) {
        teamService.quit(currentUserId(), id);
        return Result.ok();
    }

    @Operation(summary = "移除成员（队长）")
    @DeleteMapping("/{id}/members")
    public Result<Void> kick(@PathVariable Long id, @RequestParam Long userId) {
        teamService.kick(currentUserId(), id, userId);
        return Result.ok();
    }

    @Operation(summary = "转让队长（队长）")
    @PostMapping("/{id}/transfer")
    public Result<Void> transfer(@PathVariable Long id, @RequestParam Long userId) {
        teamService.transfer(currentUserId(), id, userId);
        return Result.ok();
    }

    @Operation(summary = "生成/刷新邀请码（队长）")
    @PostMapping("/{id}/invite-code")
    public Result<String> inviteCode(@PathVariable Long id) {
        return Result.ok(teamService.refreshInviteCode(currentUserId(), id));
    }

    @Operation(summary = "解散战队（队长）")
    @DeleteMapping("/{id}")
    public Result<Void> disband(@PathVariable Long id) {
        teamService.disband(currentUserId(), id);
        return Result.ok();
    }

    // ---------- 管理端 ----------

    @Operation(summary = "跨战队分页查询（管理端）")
    @RequirePermission("team:manage")
    @GetMapping("/manage")
    public Result<PageResult<Team>> page(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Team> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) qw.like(Team::getName, keyword);
        qw.orderByDesc(Team::getCreatedAt);
        Page<Team> p = teamMapper.selectPage(new Page<>(page, size), qw);
        p.getRecords().forEach(t -> t.setEnrolledContests(teamMapper.countEnrollments(t.getId())));
        return Result.ok(PageResult.of(p));
    }

    @Operation(summary = "强制解散战队（管理端）")
    @RequirePermission("team:manage")
    @DeleteMapping("/{id}/force")
    public Result<Void> forceDisband(@PathVariable Long id) {
        teamService.forceDisband(id);
        return Result.ok();
    }

    private Long currentUserId() {
        LoginUser u = UserContext.get();
        if (u == null || u.getUserId() == null) throw BizException.forbidden("请先登录");
        return u.getUserId();
    }
}
