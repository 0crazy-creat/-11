package com.csp.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/** 全局战队视图（管理端跨赛事查看/解散）。 */
@Tag(name = "战队管理（全局）")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class GlobalTeamController {

    private final ContestTeamMapper teamMapper;

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
        return Result.ok(PageResult.of(p));
    }

    @Operation(summary = "战队详情")
    @RequirePermission("team:manage")
    @GetMapping("/{id}")
    public Result<ContestTeam> get(@PathVariable Long id) {
        return Result.ok(teamMapper.selectById(id));
    }

    @Operation(summary = "解散战队（逻辑删除）")
    @RequirePermission("team:manage")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        teamMapper.deleteById(id);
        return Result.ok();
    }
}
