package com.csp.arena.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.arena.config.Result;
import com.csp.arena.dto.CreateInstanceCmd;
import com.csp.arena.dto.InstanceVO;
import com.csp.arena.dto.StatsVO;
import com.csp.arena.service.InstanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 靶机实例管理：创建、重置、销毁、巡检、监控。 */
@RestController
@RequestMapping("/api/instances")
@RequiredArgsConstructor
public class InstanceController {

    private final InstanceService instanceService;

    @GetMapping
    public Result<Page<InstanceVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long contestId,
            @RequestParam(required = false) Long teamId) {
        return Result.ok(instanceService.page(page, size, templateId, status, ownerId, contestId, teamId));
    }

    @GetMapping("/{id}")
    public Result<InstanceVO> detail(@PathVariable Long id,
                                     @RequestParam(defaultValue = "false") boolean showFlag) {
        return Result.ok(instanceService.detail(id, showFlag));
    }

    /** 我的靶机（练习中心）。 */
    @GetMapping("/mine")
    public Result<List<InstanceVO>> mine(@RequestParam(defaultValue = "1") Long ownerId) {
        return Result.ok(instanceService.listByOwner(ownerId));
    }

    @PostMapping
    public Result<InstanceVO> create(@Valid @RequestBody CreateInstanceCmd cmd,
                                     @RequestParam(defaultValue = "1") Long ownerId,
                                     @RequestParam(defaultValue = "admin") String ownerName) {
        return Result.ok(instanceService.create(cmd, ownerId, ownerName));
    }

    @PostMapping("/{id}/reset")
    public Result<InstanceVO> reset(@PathVariable Long id,
                                    @RequestParam(defaultValue = "1") Long actorId) {
        return Result.ok(instanceService.reset(id, actorId, "admin"));
    }

    @PostMapping("/{id}/destroy")
    public Result<Map<String, Boolean>> destroy(@PathVariable Long id,
                                                @RequestParam(defaultValue = "1") Long actorId) {
        return Result.ok(Map.of("destroyed", instanceService.destroy(id, actorId, "admin")));
    }

    @PostMapping("/{id}/health-check")
    public Result<InstanceVO> healthCheck(@PathVariable Long id,
                                          @RequestParam(defaultValue = "1") Long actorId) {
        return Result.ok(instanceService.healthCheckOne(id, actorId, "admin"));
    }

    @PostMapping("/health-check-all")
    public Result<Map<String, Integer>> healthCheckAll() {
        return Result.ok(Map.of("healthy", instanceService.healthCheckAll()));
    }

    @PostMapping("/destroy-all")
    public Result<Map<String, Integer>> destroyAll(@RequestParam(required = false) Long ownerId,
                                                   @RequestParam(required = false) Long contestId) {
        return Result.ok(Map.of("destroyed", instanceService.destroyAll(ownerId, contestId)));
    }

    @PostMapping("/reclaim-expired")
    public Result<Map<String, Integer>> reclaim() {
        return Result.ok(Map.of("reclaimed", instanceService.reclaimExpired()));
    }

    /** 靶场总览统计（大屏/管理页）。 */
    @GetMapping("/stats")
    public Result<StatsVO> stats() {
        return Result.ok(instanceService.stats());
    }

    /** 靶机监控列表（屏幕管理页使用，字段扁平化）。 */
    @GetMapping("/monitor")
    public Result<List<InstanceVO>> monitor(@RequestParam(required = false) Long contestId,
                                            @RequestParam(required = false) Long ownerId) {
        return Result.ok(instanceService.page(1, 500, null, null, ownerId, contestId, null).getRecords());
    }

    @GetMapping("/logs")
    public Result<List<Map<String, Object>>> logs(@RequestParam(required = false) Long instanceId,
                                                  @RequestParam(defaultValue = "50") int limit) {
        return Result.ok(instanceService.actionLogs(instanceId, limit));
    }
}
