package com.csp.infra.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 业务侧统一推送入口。
 *
 * <p>消息体字段名保持 {@code payload}（{@link WsMessage}），前端在 {@code utils/ws.js}
 * 统一归一化出 {@code data} 别名，避免各处分别兼容。</p>
 *
 * <p>AWD 事件同时投递到 {@code contest:*} 与 {@code awd:*} 两个房间：
 * 赛事大屏/3D 态势订阅 contest 房间，AWD 大屏订阅 awd 房间，两者都要能收到。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WsPublisher {

    private final WsSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    /** 投递赛事事件：contest 房间 + awd 房间双投。 */
    public void publish(String type, Long contestId, Object payload) {
        String json = serialize(type, contestId, payload);
        if (json == null) return;
        sessionManager.broadcast(WsSessionManager.contestRoom(contestId), json);
        sessionManager.broadcast(WsSessionManager.awdRoom(contestId), json);
    }

    /** 兼容旧调用名，等价于 publish。 */
    public void publishAwd(String type, Long contestId, Object payload) {
        publish(type, contestId, payload);
    }

    /** 投递到战队房间（战队主页事件）。 */
    public void publishTeam(String type, Long teamId, Object payload) {
        broadcast(WsSessionManager.teamRoom(teamId), type, null, payload);
    }

    private void broadcast(String roomKey, String type, Long contestId, Object payload) {
        String json = serialize(type, contestId, payload);
        if (json == null) return;
        sessionManager.broadcast(roomKey, json);
    }

    private String serialize(String type, Long contestId, Object payload) {
        try {
            return objectMapper.writeValueAsString(WsMessage.of(type, contestId, payload));
        } catch (Exception e) {
            log.warn("WS 序列化失败 type={} contest={}", type, contestId, e);
            return null;
        }
    }

    public void scoreUpdate(Long contestId, Object payload) { publish("SCORE_UPDATE", contestId, payload); }
    public void firstBlood(Long contestId, Object payload) { publish("FIRST_BLOOD", contestId, payload); }
    public void announce(Long contestId, Object payload) { publish("ANNOUNCE", contestId, payload); }
    public void roundChange(Long contestId, Object payload) { publish("ROUND_CHANGE", contestId, payload); }
    public void targetAlert(Long contestId, Object payload) { publish("TARGET_ALERT", contestId, payload); }

    /** AWD 攻击事件：赛事房间 + AWD 房间双投。 */
    public void attackEvent(Long contestId, Object payload) { publishAwd("ATTACK_EVENT", contestId, payload); }
}
