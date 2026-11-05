package com.csp.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.platform.entity.SysUser;
import com.csp.platform.mapper.SysUserMapper;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 全局战队视图（管理端跨赛事查看/解散）。 */
@Tag(name = "战队管理（全局）")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class GlobalTeamController {

    private final ContestTeamMapper teamMapper;
    private final SysUserMapper userMapper;

    @Operation(summary = "跨赛事分页查询战队")
    @RequirePermission("team:manage")
    @GetMapping
    public Result<PageResult<ContestTeam>> page(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String status) {
        LambdaQueryWrapper<ContestTeam> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) qw.like(ContestTeam::getName, keyword);
        if (StringUtils.hasText(status)) qw.eq(ContestTeam::getStatus, status);
        qw.orderByDesc(ContestTeam::getCreatedAt);
        Page<ContestTeam> p = teamMapper.selectPage(new Page<>(page, size), qw);
        fillCaptainNames(p.getRecords());
        return Result.ok(PageResult.of(p));
    }

    @Operation(summary = "战队详情")
    @RequirePermission("team:manage")
    @GetMapping("/{id}")
    public Result<ContestTeam> get(@PathVariable Long id) {
        ContestTeam team = teamMapper.selectById(id);
        if (team != null) fillCaptainNames(List.of(team));
        return Result.ok(team);
    }

    /** 回填队长昵称（captainName 为非表字段，需二次查询 sys_user）。 */
    private void fillCaptainNames(Collection<ContestTeam> teams) {
        if (teams == null || teams.isEmpty()) return;
        List<Long> ids = teams.stream()
                .map(ContestTeam::getCaptainId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) return;
        Map<Long, SysUser> users = userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        for (ContestTeam t : teams) {
            SysUser u = users.get(t.getCaptainId());
            if (u != null) {
                t.setCaptainName(StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername());
            }
        }
    }

    @Operation(summary = "解散战队（逻辑删除）")
    @RequirePermission("team:manage")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        teamMapper.deleteById(id);
        return Result.ok();
    }
}
