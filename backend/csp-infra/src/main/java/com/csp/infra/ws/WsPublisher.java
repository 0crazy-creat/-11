package com.csp.infra.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 业务侧统一推送入口。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WsPublisher {

    private final WsSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public void publish(String type, Long contestId, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(WsMessage.of(type, contestId, payload));
            sessionManager.broadcast(contestId, json);
        } catch (Exception e) {
            log.warn("WS 发布失败 type={} contest={}", type, contestId, e);
        }
    }

    public void scoreUpdate(Long contestId, Object payload) { publish("SCORE_UPDATE", contestId, payload); }
    public void firstBlood(Long contestId, Object payload) { publish("FIRST_BLOOD", contestId, payload); }
    public void announce(Long contestId, Object payload) { publish("ANNOUNCE", contestId, payload); }
    public void attackEvent(Long contestId, Object payload) { publish("ATTACK_EVENT", contestId, payload); }
    public void roundChange(Long contestId, Object payload) { publish("ROUND_CHANGE", contestId, payload); }
    public void targetAlert(Long contestId, Object payload) { publish("TARGET_ALERT", contestId, payload); }
}
