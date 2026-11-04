package com.csp.platform.virt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.*;

/**
 * 集群与 SLA 监控：
 * - 节点注册/心跳（多实例部署时各节点上报）
 * - 服务可用性统计（成功/失败请求、可用率、P95 延迟）
 * - 容量水位（CPU/内存/实例数）与 SLA 达标判定
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClusterSlaService {

    private final LvpVirtualizationService lvp;

    /** 节点信息。 */
    public static class Node {
        public String id;
        public String host;
        public String role;
        public long lastHeartbeat;
        public boolean healthy = true;
        public Map<String, Object> metrics = new LinkedHashMap<>();
    }

    /** 请求统计（滑动累计，用于 SLA 计算）。 */
    private final Deque<long[]> samples = new ArrayDeque<>();   // [timestamp, latencyMs, success(1/0)]
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final long startedAt = System.currentTimeMillis();

    private static final int MAX_SAMPLES = 5000;
    /** SLA 目标：可用率 ≥ 99.9%，P95 延迟 ≤ 500ms */
    public static final double SLA_TARGET_AVAILABILITY = 99.9;
    public static final long SLA_TARGET_P95_MS = 500;

    /** 记录一次请求（可由过滤器调用）。 */
    public synchronized void record(long latencyMs, boolean success) {
        samples.addLast(new long[]{System.currentTimeMillis(), latencyMs, success ? 1 : 0});
        while (samples.size() > MAX_SAMPLES) samples.removeFirst();
    }

    /** 注册/心跳节点。 */
    public Node heartbeat(String nodeId, String host, String role) {
        Node n = nodes.computeIfAbsent(nodeId, k -> {
            Node x = new Node();
            x.id = k;
            return x;
        });
        n.host = host;
        n.role = role == null ? "app" : role;
        n.lastHeartbeat = System.currentTimeMillis();
        n.healthy = true;
        n.metrics.put("jvmHeapUsedMb", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
        n.metrics.put("jvmUptimeSec", ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
        return n;
    }

    /** SLA 报告。 */
    public Map<String, Object> slaReport() {
        long total, ok;
        long p95;
        synchronized (this) {
            total = samples.size();
            ok = samples.stream().filter(s -> s[2] == 1).count();
            long[] lat = samples.stream().mapToLong(s -> s[1]).sorted().toArray();
            p95 = lat.length == 0 ? 0 : lat[(int) Math.min(lat.length - 1, Math.floor(lat.length * 0.95))];
        }
        double availability = total == 0 ? 100.0 : Math.round(ok * 100000.0 / total) / 1000.0;
        boolean availOk = availability >= SLA_TARGET_AVAILABILITY;
        boolean latencyOk = p95 <= SLA_TARGET_P95_MS;

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("uptimeSeconds", (System.currentTimeMillis() - startedAt) / 1000);
        r.put("samples", total);
        r.put("success", ok);
        r.put("availability", availability);
        r.put("p95LatencyMs", p95);
        r.put("targetAvailability", SLA_TARGET_AVAILABILITY);
        r.put("targetP95Ms", SLA_TARGET_P95_MS);
        r.put("availabilityOk", availOk);
        r.put("latencyOk", latencyOk);
        r.put("slaMet", availOk && latencyOk);
        return r;
    }

    /** 集群概览。 */
    public Map<String, Object> clusterOverview() {
        long now = System.currentTimeMillis();
        List<Map<String, Object>> list = new ArrayList<>();
        int healthy = 0;
        for (Node n : nodes.values()) {
            // 30s 无心跳判定不健康
            boolean alive = n.lastHeartbeat > 0 && (now - n.lastHeartbeat) < Duration.ofSeconds(30).toMillis();
            n.healthy = alive;
            if (alive) healthy++;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", n.id);
            m.put("host", n.host);
            m.put("role", n.role);
            m.put("healthy", alive);
            m.put("lastHeartbeatAgoMs", n.lastHeartbeat == 0 ? -1 : now - n.lastHeartbeat);
            m.put("metrics", n.metrics);
            list.add(m);
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("nodeCount", nodes.size());
        r.put("healthyNodes", healthy);
        r.put("nodes", list);
        r.put("lvp", lvp.summary());
        r.put("sla", slaReport());
        return r;
    }
}
