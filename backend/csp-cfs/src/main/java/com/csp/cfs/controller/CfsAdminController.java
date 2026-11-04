package com.csp.cfs.controller;

import com.csp.cfs.dto.CfsTopologySaveCmd;
import com.csp.cfs.entity.CfsLayer;
import com.csp.cfs.entity.CfsNode;
import com.csp.cfs.entity.CfsTopology;
import com.csp.cfs.service.CfsService;
import com.csp.cfs.service.CfsTopologyService;
import com.csp.common.api.Result;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CFS 管理端：分层拓扑编排、发布、开赛。 */
@Tag(name = "CFS 管理")
@RestController
@RequestMapping("/api/cfs/admin")
@RequiredArgsConstructor
public class CfsAdminController {

    private final CfsTopologyService topologyService;
    private final CfsService cfsService;

    @Operation(summary = "新建分层拓扑（层 + 节点）")
    @RequirePermission("contest:update")
    @PostMapping("/topologies")
    public Result<CfsTopology> create(@Valid @RequestBody CfsTopologySaveCmd cmd) {
        return Result.ok(topologyService.create(cmd));
    }

    @Operation(summary = "拓扑详情")
    @RequirePermission("contest:view")
    @GetMapping("/topologies/{id}")
    public Result<CfsTopology> get(@PathVariable Long id) {
        return Result.ok(topologyService.get(id));
    }

    @Operation(summary = "拓扑层定义")
    @RequirePermission("contest:view")
    @GetMapping("/topologies/{id}/layers")
    public Result<List<CfsLayer>> layers(@PathVariable Long id) {
        topologyService.get(id);
        return Result.ok(topologyService.layers(id));
    }

    @Operation(summary = "拓扑节点定义")
    @RequirePermission("contest:view")
    @GetMapping("/topologies/{id}/nodes")
    public Result<List<CfsNode>> nodes(@PathVariable Long id) {
        topologyService.get(id);
        return Result.ok(topologyService.nodes(id));
    }

    @Operation(summary = "发布拓扑（校验分层约束）")
    @RequirePermission("contest:update")
    @PostMapping("/topologies/{id}/publish")
    public Result<CfsTopology> publish(@PathVariable Long id) {
        return Result.ok(topologyService.publish(id));
    }

    @Operation(summary = "开赛：为所有审核通过战队拉起分层拓扑")
    @RequirePermission("contest:control")
    @PostMapping("/contests/{contestId}/start")
    public Result<Integer> start(@PathVariable Long contestId) {
        return Result.ok(cfsService.startContest(contestId));
    }
}