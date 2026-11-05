package com.csp.infra.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 连接管理：按「房间」分组广播。
 *
 * <p>房间键统一为字符串，前缀区分业务域，避免不同类型房间串号：</p>
 * <ul>
 *   <li>{@code contest:{contestId}} —— 赛事大屏 / 排行榜 / 3D 态势</li>
 *   <li>{@code awd:{contestId}} —— AWD 攻防大屏</li>
 *   <li>{@code team:{teamId}} —— 战队主页</li>
 * </ul>
 *
 * <p>历史版本只按 contestId(Long) 分组，且只注册了 /ws/contest 一个端点，
 * 导致 /ws/awd、/ws/team 两类连接握手 404；现统一为字符串房间键。</p>
 */
@Slf4j
@Component
public class WsSessionManager extends TextWebSocketHandler {

    /** roomKey -> sessions */
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    /** sessionId -> roomKey */
    private final Map<String, String> sessionRoom = new ConcurrentHashMap<>();

    public static String contestRoom(Long contestId) { return "contest:" + contestId; }

    public static String awdRoom(Long contestId) { return "awd:" + contestId; }

    public static String teamRoom(Long teamId) { return "team:" + teamId; }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Object room = session.getAttributes().get("room");
        String key = room == null ? contestRoom(0L) : room.toString();
        rooms.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionRoom.put(session.getId(), key);
        log.info("WS 连接建立 session={} room={} 房间在线={}", session.getId(), key, rooms.get(key).size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String key = sessionRoom.remove(session.getId());
        if (key != null) {
            Set<WebSocketSession> set = rooms.get(key);
            if (set != null) set.remove(session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // 心跳
        if ("ping".equals(message.getPayload())) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    /** 向指定房间广播（房间键由 {@link #contestRoom}/{@link #awdRoom}/{@link #teamRoom} 生成）。 */
    public void broadcast(String roomKey, String json) {
        if (roomKey == null) return;
        Set<WebSocketSession> set = rooms.get(roomKey);
        if (set == null || set.isEmpty()) return;
        TextMessage msg = new TextMessage(json);
        for (WebSocketSession s : set) {
            try {
                if (s.isOpen()) s.sendMessage(msg);
            } catch (IOException e) {
                log.warn("WS 推送失败 room={} session={}", roomKey, s.getId(), e);
            }
        }
    }

    /** 兼容旧调用：等价于广播到 contest 房间。 */
    public void broadcast(Long contestId, String json) {
        broadcast(contestRoom(contestId == null ? 0L : contestId), json);
    }

    public int onlineCount(String roomKey) {
        Set<WebSocketSession> set = rooms.get(roomKey);
        return set == null ? 0 : set.size();
    }

    public int onlineCount(Long contestId) {
        return onlineCount(contestRoom(contestId == null ? 0L : contestId));
    }

    public Set<String> activeRooms() { return rooms.keySet(); }
}