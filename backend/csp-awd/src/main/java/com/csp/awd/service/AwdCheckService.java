package com.csp.awd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.awd.dto.CheckResultVO;
import com.csp.awd.entity.AwdTarget;
import com.csp.awd.mapper.AwdTargetMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AWD check 脚本执行器（对齐 Cardinal Round Orchestrator 的 checker 角色）。
 *
 * <h3>检查内容</h3>
 * 对每队每个靶机按顺序做两段校验：
 * <ol>
 *   <li><b>HTTP 探活</b>：GET {@code /} 期望返回 2xx/3xx，超时即判该靶机不可达。</li>
 *   <li><b>业务功能校验</b>：GET 配置的健康端点（默认 {@code /health}），期望 2xx。</li>
 * </ol>
 *
 * <h3>容错（关键）</h3>
 * 单台靶机超时/失败/异常<b>只影响该队该题</b>，绝不向上抛出——
 * 返回结果对象而非异常，保证调度器可以继续检查其他队。
 *
 * <h3>可注入性</h3>
 * HTTP 探测委托给 {@link Probe} 接口，单测可注入桩实现，无需真实容器。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwdCheckService {

    private final AwdTargetMapper targetMapper;
    private final ObjectMapper objectMapper;

    /** 单次 check 的超时（毫秒）。靶机无响应时快速失败，避免拖住整轮。 */
    @Value("${csp.awd.check.timeout-ms:3000}")
    private long timeoutMs = 3000;

    /** 业务功能校验路径。 */
    @Value("${csp.awd.check.health-path:/health}")
    private String healthPath = "/health";

    /**
     * HTTP 探测函数式接口：返回 {httpCode, latencyMs, error}。
     * 生产用 {@link #httpProbe()}；单测注入桩。
     */
    @FunctionalInterface
    public interface Probe {
        ProbeOutcome probe(String url, long timeoutMs);
    }

    /** 探测结果。 */
    public record ProbeOutcome(int httpCode, long latencyMs, String error) {
        public boolean ok() { return error == null && httpCode >= 200 && httpCode < 400; }
    }

    private volatile Probe probeOverride;

    /** 测试注入点：替换 HTTP 探测实现。传 null 恢复默认。 */
    public void setProbe(Probe probe) { this.probeOverride = probe; }

    // ------------------------------------------------------------------ 单靶机

    /**
     * 检查一台靶机。**永不抛异常**。
     */
    public CheckResultVO checkTarget(Long contestId, Integer roundNo, AwdTarget t) {
        String base = baseUrl(t);
        long start = System.currentTimeMillis();
        if (base == null) {
            return fail(contestId, roundNo, t, "UNREACHABLE", 0,
                    System.currentTimeMillis() - start, "靶机无可用访问地址");
        }
        Probe probe = probeOverride != null ? probeOverride : httpProbe();

        // 1) HTTP 探活
        ProbeOutcome alive = safeProbe(probe, base + "/");
        if (!alive.ok()) {
            String reason = alive.error() != null ? alive.error() : "HTTP " + alive.httpCode();
            String status = "TIMEOUT".equalsIgnoreCase(alive.error()) ? "TIMEOUT" : "UNREACHABLE";
            return fail(contestId, roundNo, t, status, alive.httpCode(),
                    System.currentTimeMillis() - start, "探活失败: " + reason);
        }

        // 2) 业务功能校验：主路径不通过时，按逗号分隔的兜底路径依次重试。
        //    典型场景：AWD 靶场镜像暴露 /healthz，而平台默认 /health —— 不兜底会误判修复失败。
        String usedPath = healthPath;
        ProbeOutcome health = safeProbe(probe, base + usedPath);
        if (!health.ok()) {
            for (String alt : fallbackHealthPaths()) {
                ProbeOutcome retry = safeProbe(probe, base + alt);
                if (retry.ok()) {
                    usedPath = alt;
                    health = retry;
                    break;
                }
            }
        }
        if (!health.ok()) {
            String reason = health.error() != null ? health.error() : "HTTP " + health.httpCode();
            return fail(contestId, roundNo, t, "FAIL", health.httpCode(),
                    System.currentTimeMillis() - start, "功能校验失败 " + healthPath + ": " + reason);
        }

        return CheckResultVO.builder()
                .contestId(contestId)
                .roundNo(roundNo)
                .teamId(t.getTeamId())
                .questionId(t.getQuestionId())
                .status("PASS")
                .httpCode(health.httpCode())
                .latencyMs(System.currentTimeMillis() - start)
                .reason(null)
                .slaDelta(0)
                .checkedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 检查整个赛事的所有靶机，按战队聚合。
     *
     * <p>容错：单台靶机异常被吞掉并记为 FAIL，不影响其他队。</p>
     *
     * @return teamId -> 该队全部靶机的 check 结果（顺序与靶机表一致）
     */
    public Map<Long, List<CheckResultVO>> checkContest(Long contestId, Integer roundNo) {
        List<AwdTarget> targets = targetMapper.selectList(new LambdaQueryWrapper<AwdTarget>()
                .eq(AwdTarget::getContestId, contestId)
                .orderByAsc(AwdTarget::getTeamId)
                .orderByAsc(AwdTarget::getQuestionId));

        Map<Long, List<CheckResultVO>> byTeam = new LinkedHashMap<>();
        for (AwdTarget t : targets) {
            CheckResultVO r;
            try {
                r = checkTarget(contestId, roundNo, t);
            } catch (Throwable e) {
                // 兜底：任何意外都只影响该队该题
                log.warn("check 异常已隔离 contest={} round={} team={} q={}: {}",
                        contestId, roundNo, t.getTeamId(), t.getQuestionId(), e.toString());
                r = fail(contestId, roundNo, t, "FAIL", 0, 0, "check 执行异常: " + e.getClass().getSimpleName());
            }
            byTeam.computeIfAbsent(t.getTeamId(), k -> new ArrayList<>()).add(r);
        }
        log.info("check 完成 contest={} round={} 战队数={} 失败靶机数={}",
                contestId, roundNo, byTeam.size(),
                byTeam.values().stream().mapToInt(AwdCheckService::failCount).sum());
        return byTeam;
    }

    // ------------------------------------------------------------------ 聚合判定

    /** 该队本轮是否全部靶机 PASS。空列表视为 PASS（无靶机可扣分）。 */
    public static boolean teamAllPass(List<CheckResultVO> results) {
        if (results == null || results.isEmpty()) return true;
        for (CheckResultVO r : results) {
            if (r == null || !r.passed()) return false;
        }
        return true;
    }

    /** 统计某队失败靶机数。 */
    public static int failCount(List<CheckResultVO> results) {
        if (results == null) return 0;
        int n = 0;
        for (CheckResultVO r : results) {
            if (r != null && !r.passed()) n++;
        }
        return n;
    }

    // ------------------------------------------------------------------ 内部

    private CheckResultVO fail(Long contestId, Integer roundNo, AwdTarget t,
                               String status, int httpCode, long latencyMs, String reason) {
        return CheckResultVO.builder()
                .contestId(contestId)
                .roundNo(roundNo)
                .teamId(t.getTeamId())
                .questionId(t.getQuestionId())
                .status(status)
                .httpCode(httpCode)
                .latencyMs(latencyMs)
                .reason(reason)
                .slaDelta(0)
                .checkedAt(LocalDateTime.now())
                .build();
    }

    private ProbeOutcome safeProbe(Probe probe, String url) {
        long st = System.currentTimeMillis();
        try {
            return probe.probe(url, timeoutMs);
        } catch (Exception e) {
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return new ProbeOutcome(0, System.currentTimeMillis() - st, msg);
        }
    }

    /**
     * 拼装靶机访问地址。
     *
     * <p>{@code awd_target.ip} 存的是容器内网 IP（如 192.168.160.2），只有与靶机同网络的
     * 容器才能访问；主服务是 {@code --network host}，必须走宿主机映射端口（127.0.0.1）。
     * 因此只有 ip 本身是回环/宿主机地址时才用它，其余一律回落 127.0.0.1。</p>
     */
    private String baseUrl(AwdTarget t) {
        if (t.getPort() == null || t.getPort() <= 0) return null;
        String host = resolveReachableHost(t.getIp());
        return "http://" + host + ":" + t.getPort();
    }

    /** 靶机访问主机：仅回环/宿主 IP 可直接用，容器内网 IP 一律换成本机映射地址。 */
    /** 主健康路径失败后的兜底路径列表（逗号分隔配置，去空去重，排除主路径本身）。 */
    @Value("${csp.awd.check.health-path-fallbacks:/healthz,/health,/healthcheck}")
    private String healthPathFallbacks = "/healthz,/health,/healthcheck";

    private java.util.List<String> fallbackHealthPaths() {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (healthPathFallbacks == null || healthPathFallbacks.isBlank()) return out;
        for (String raw : healthPathFallbacks.split(",")) {
            String p = raw.trim();
            if (p.isEmpty()) continue;
            if (!p.startsWith("/")) p = "/" + p;
            if (p.equals(healthPath)) continue;
            if (!out.contains(p)) out.add(p);
        }
        return out;
    }

    static String resolveReachableHost(String ip) {
        if (ip == null || ip.isBlank()) return "127.0.0.1";
        String h = ip.trim();
        if ("0.0.0.0".equals(h) || "localhost".equalsIgnoreCase(h)) return "127.0.0.1";
        if (h.startsWith("127.")) return h;
        if (h.startsWith("192.168.") || h.startsWith("172.1") || h.startsWith("10.")) {
            return "127.0.0.1"; // 容器内网地址，主服务不可直连
        }
        return h;
    }

    /** 默认 HTTP 探测器（JDK HttpClient，短超时 + 不跟随重定向）。 */
    public Probe httpProbe() {
        return (url, timeout) -> {
            long st = System.currentTimeMillis();
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(timeout))
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .build();
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofMillis(timeout))
                        .GET()
                        .build();
                HttpResponse<Void> resp = client.send(req, HttpResponse.BodyHandlers.discarding());
                return new ProbeOutcome(resp.statusCode(), System.currentTimeMillis() - st, null);
            } catch (java.net.http.HttpTimeoutException e) {
                return new ProbeOutcome(0, System.currentTimeMillis() - st, "TIMEOUT");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ProbeOutcome(0, System.currentTimeMillis() - st, "INTERRUPTED");
            } catch (Exception e) {
                String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                return new ProbeOutcome(0, System.currentTimeMillis() - st, msg);
            }
        };
    }

    /** 供上层序列化存档用（check 明细落 Redis 由 AwdScoreService 负责，避免循环依赖）。 */
    public String toJson(Object o) {
        if (objectMapper == null) return "{}";
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }
}