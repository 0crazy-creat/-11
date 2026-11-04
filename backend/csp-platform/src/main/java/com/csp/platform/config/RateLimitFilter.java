package com.csp.platform.config;

import com.csp.infra.redis.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * 全局接口限流：按 IP + 路径维度，Redis 计数，超限返回 429。
 * 仅作用于 /api/**，放行登录与静态资源；Redis 故障时降级放行，不影响可用性。
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisService redisService;

    /** 默认每窗口允许次数 */
    private static final int LIMIT = 120;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (!uri.startsWith("/api/")) {
            chain.doFilter(req, resp);
            return;
        }
        // 登录接口单独更严格
        boolean login = uri.startsWith("/api/auth/login");
        int limit = login ? 10 : LIMIT;
        String ip = clientIp(req);
        String key = "csp:rl:" + (login ? "login" : "api") + ":" + ip;
        try {
            boolean allowed = redisService.allowRequest(key, limit, WINDOW);
            if (!allowed) {
                resp.setStatus(429);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"success\":false}");
                return;
            }
        } catch (Exception e) {
            // Redis 不可用时降级放行
        }
        chain.doFilter(req, resp);
    }

    private static String clientIp(HttpServletRequest req) {
        String h = req.getHeader("X-Forwarded-For");
        if (h != null && !h.isBlank()) return h.split(",")[0].trim();
        String r = req.getHeader("X-Real-IP");
        if (r != null && !r.isBlank()) return r;
        return req.getRemoteAddr();
    }
}
