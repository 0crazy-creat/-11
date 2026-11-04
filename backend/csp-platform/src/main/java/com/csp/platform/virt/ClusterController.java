package com.csp.platform.virt;

import com.csp.common.api.Result;
import com.csp.security.annotation.PublicApi;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 集群与 SLA 监控接口。 */
@Tag(name = "集群与 SLA")
@RestController
@RequestMapping("/api/cluster")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterSlaService cluster;

    @Operation(summary = "节点心跳上报")
    @PublicApi
    @PostMapping("/heartbeat")
    public Result<ClusterSlaService.Node> heartbeat(@RequestParam String nodeId,
                                                    @RequestParam(required = false) String host,
                                                    @RequestParam(required = false) String role) {
        return Result.ok(cluster.heartbeat(nodeId, host, role));
    }

    @Operation(summary = "集群概览（节点/容量/SLA）")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(cluster.clusterOverview());
    }

    @Operation(summary = "SLA 报告")
    @GetMapping("/sla")
    public Result<Map<String, Object>> sla() {
        return Result.ok(cluster.slaReport());
    }

    @Operation(summary = "模拟一次请求采样（验证用）")
    @RequirePermission("contest:control")
    @PostMapping("/sample")
    public Result<Map<String, Object>> sample(@RequestParam(defaultValue = "120") Long latencyMs,
                                              @RequestParam(defaultValue = "true") Boolean success) {
        cluster.record(latencyMs, success);
        return Result.ok(cluster.slaReport());
    }
}
