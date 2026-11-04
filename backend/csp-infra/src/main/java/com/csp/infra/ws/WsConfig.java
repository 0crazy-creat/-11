package com.csp.infra.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/** WebSocket 端点注册。 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WsConfig implements WebSocketConfigurer {

    private final WsSessionManager sessionManager;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sessionManager, "/ws/contest/{contestId}")
                .addInterceptors(new ContestIdInterceptor())
                .setAllowedOriginPatterns("*");
    }

    /**
     * 把路径变量 contestId 写入 session 属性，供 WsSessionManager 按房间分组。
     * 缺少该拦截器时所有连接都会落入 contestId=0 的默认房间，导致按赛事广播失效。
     */
    static class ContestIdInterceptor implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            if (request instanceof ServletServerHttpRequest sr) {
                String uri = sr.getServletRequest().getRequestURI();
                int idx = uri.indexOf("/ws/contest/");
                if (idx >= 0) {
                    String tail = uri.substring(idx + "/ws/contest/".length());
                    int slash = tail.indexOf('/');
                    if (slash > 0) tail = tail.substring(0, slash);
                    try {
                        attributes.put("contestId", Long.parseLong(tail.trim()));
                    } catch (NumberFormatException ignored) {
                        attributes.put("contestId", 0L);
                    }
                }
            }
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
        }
    }
}
