package com.csp.platform.controller;

import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.platform.entity.Announcement;
import com.csp.platform.service.AnnouncementService;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 平台公告接口。登录用户可读，管理端可写。 */
@Tag(name = "平台公告")
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "公告分页列表")
    @GetMapping
    public Result<PageResult<Announcement>> page(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String scope,
                                                 @RequestParam(required = false) Long contestId,
                                                 @RequestParam(required = false) String keyword) {
        return Result.ok(announcementService.page(page, size, scope, contestId, keyword));
    }

    @Operation(summary = "最新公告（首页展示）")
    @GetMapping("/latest")
    public Result<List<Announcement>> latest(@RequestParam(defaultValue = "5") Integer n) {
        return Result.ok(announcementService.latest(n));
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    public Result<Announcement> get(@PathVariable Long id) {
        return Result.ok(announcementService.get(id));
    }

    @Operation(summary = "发布公告")
    @RequirePermission("announcement:manage")
    @PostMapping
    public Result<Announcement> create(@RequestBody Announcement cmd) {
        return Result.ok(announcementService.create(cmd));
    }

    @Operation(summary = "编辑公告")
    @RequirePermission("announcement:manage")
    @PutMapping("/{id}")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody Announcement cmd) {
        return Result.ok(announcementService.update(id, cmd));
    }

    @Operation(summary = "删除公告")
    @RequirePermission("announcement:manage")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        announcementService.remove(id);
        return Result.ok();
    }
}