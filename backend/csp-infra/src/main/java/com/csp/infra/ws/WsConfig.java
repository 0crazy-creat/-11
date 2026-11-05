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

/**
 * WebSocket 端点注册。
 *
 * <p>前端共使用三类房间，必须全部注册，否则握手直接 404（实时推送静默失效）：</p>
 * <ul>
 *   <li>{@code /ws/contest/{contestId}} —— 赛事排行榜 / 3D 态势 / 答题页</li>
 *   <li>{@code /ws/awd/{contestId}} —— AWD / AWDP 攻防大屏</li>
 *   <li>{@code /ws/team/{teamId}} —— 战队主页</li>
 * </ul>
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WsConfig implements WebSocketConfigurer {

    private final WsSessionManager sessionManager;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sessionManager, "/ws/contest/{contestId}")
                .addInterceptors(new RoomInterceptor("/ws/contest/", "contest"))
                .setAllowedOriginPatterns("*");
        registry.addHandler(sessionManager, "/ws/awd/{contestId}")
                .addInterceptors(new RoomInterceptor("/ws/awd/", "awd"))
                .setAllowedOriginPatterns("*");
        registry.addHandler(sessionManager, "/ws/team/{teamId}")
                .addInterceptors(new RoomInterceptor("/ws/team/", "team"))
                .setAllowedOriginPatterns("*");
    }

    /**
     * 解析路径末段的 id，拼成 {@code <prefix>:<id>} 房间键写入 session 属性，
     * 供 {@link WsSessionManager} 按房间分组广播。
     *
     * <p>缺少该拦截器时所有连接都会落入默认房间，导致按赛事/战队广播失效。</p>
     */
    static class RoomInterceptor implements HandshakeInterceptor {

        private final String pathPrefix;
        private final String roomPrefix;

        RoomInterceptor(String pathPrefix, String roomPrefix) {
            this.pathPrefix = pathPrefix;
            this.roomPrefix = roomPrefix;
        }

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            attributes.put("room", roomPrefix + ":0");
            if (request instanceof ServletServerHttpRequest sr) {
                String uri = sr.getServletRequest().getRequestURI();
                int idx = uri.indexOf(pathPrefix);
                if (idx >= 0) {
                    String tail = uri.substring(idx + pathPrefix.length());
                    int slash = tail.indexOf('/');
                    if (slash > 0) tail = tail.substring(0, slash);
                    try {
                        long id = Long.parseLong(tail.trim());
                        attributes.put("room", roomPrefix + ":" + id);
                    } catch (NumberFormatException ignored) {
                        // 保持默认 0 号房间
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