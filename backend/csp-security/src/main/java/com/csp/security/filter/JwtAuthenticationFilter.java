package com.csp.security.filter;

import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import com.csp.security.jwt.JwtProperties;
import com.csp.security.jwt.JwtService;
import com.csp.common.util.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** 解析 JWT 并写入线程上下文；无 token 时静默放行，由权限拦截器决定是否拒绝。 */
@Component
@Order(10)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtProperties props;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        try {
            String header = req.getHeader(props.getHeader());
            if (header != null && header.startsWith(props.getPrefix())) {
                String token = header.substring(props.getPrefix().length()).trim();
                LoginUser user = jwtService.parse(token);
                if (user != null) {
                    user.setClientIp(IpUtils.getClientIp(req));
                    user.setDeviceId(req.getHeader("X-Device-Id"));
                    UserContext.set(user);
                }
            }
            chain.doFilter(req, resp);
        } finally {
            UserContext.clear();
        }
    }
}
