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

/** WebSocket 连接管理：按比赛分组广播，支撑秒级排行榜/大屏推送。 */
@Slf4j
@Component
public class WsSessionManager extends TextWebSocketHandler {

    /** contestId -> sessions */
    private final Map<Long, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    /** sessionId -> contestId */
    private final Map<String, Long> sessionRoom = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Object contestId = session.getAttributes().get("contestId");
        Long cid = contestId == null ? 0L : Long.valueOf(contestId.toString());
        rooms.computeIfAbsent(cid, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionRoom.put(session.getId(), cid);
        log.info("WS 连接建立 session={} contest={} 房间在线={}", session.getId(), cid, rooms.get(cid).size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        Long cid = sessionRoom.remove(session.getId());
        if (cid != null) {
            Set<WebSocketSession> set = rooms.get(cid);
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

    /** 向指定比赛房间广播。 */
    public void broadcast(Long contestId, String json) {
        Set<WebSocketSession> set = rooms.get(contestId);
        if (set == null || set.isEmpty()) return;
        TextMessage msg = new TextMessage(json);
        for (WebSocketSession s : set) {
            try {
                if (s.isOpen()) s.sendMessage(msg);
            } catch (IOException e) {
                log.warn("WS 推送失败 session={}", s.getId(), e);
            }
        }
    }

    public int onlineCount(Long contestId) {
        Set<WebSocketSession> set = rooms.get(contestId);
        return set == null ? 0 : set.size();
    }

    public Set<Long> activeRooms() { return rooms.keySet(); }
}
