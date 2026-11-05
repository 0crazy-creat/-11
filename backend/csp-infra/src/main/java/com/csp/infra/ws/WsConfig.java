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
        // 前端按业务命名连接（contest/awd/team），房间号统一取路径末段。
        registry.addHandler(sessionManager,
                        "/ws/contest/{contestId}", "/ws/awd/{contestId}", "/ws/team/{teamId}")
                .addInterceptors(new RoomIdInterceptor())
                .setAllowedOriginPatterns("*");
    }

    /**
     * 把路径末段写入 session 属性 "contestId"，作为广播房间号。
     * 缺少该拦截器时所有连接都会落入 contestId=0 的默认房间，导致按赛事广播失效。
     */
    static class RoomIdInterceptor implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            attributes.put("contestId", 0L);
            if (request instanceof ServletServerHttpRequest sr) {
                String uri = sr.getServletRequest().getRequestURI();
                int q = uri.indexOf('?');
                if (q >= 0) uri = uri.substring(0, q);
                while (uri.endsWith("/")) uri = uri.substring(0, uri.length() - 1);
                int slash = uri.lastIndexOf('/');
                String tail = slash >= 0 ? uri.substring(slash + 1) : uri;
                try {
                    attributes.put("contestId", Long.parseLong(tail.trim()));
                } catch (NumberFormatException ignored) {
                    // 非数字路径保持默认房间
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
