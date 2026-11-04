package com.csp.infra.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** WebSocket 统一消息体。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsMessage {
    /** 消息类型：SCORE_UPDATE / ANNOUNCE / FIRST_BLOOD / TARGET_ALERT / ATTACK_EVENT / ROUND_CHANGE */
    private String type;
    private Long contestId;
    private Object payload;
    private LocalDateTime ts = LocalDateTime.now();

    public static WsMessage of(String type, Long contestId, Object payload) {
        return new WsMessage(type, contestId, payload, LocalDateTime.now());
    }
}
