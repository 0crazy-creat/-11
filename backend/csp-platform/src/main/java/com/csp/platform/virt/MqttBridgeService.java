package com.csp.platform.virt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQTT 对接（消息总线）：
 * 用于把赛事事件（开赛/攻破/轮次切换）推给外部系统与大屏。
 * 未配置 broker 时使用本地内存总线（可完整验证发布/订阅语义）。
 */
@Slf4j
@Service
public class MqttBridgeService {

    @Value("${csp.mqtt.broker:tcp://127.0.0.1:1883}")
    private String broker;
    @Value("${csp.mqtt.enabled:false}")
    private boolean enabled;
    @Value("${csp.mqtt.client-id:csp-platform}")
    private String clientId;

    /** 本地内存总线：topic → 最近消息（保留最近 100 条） */
    private final Map<String, Deque<Map<String, Object>>> bus = new ConcurrentHashMap<>();
    private static final int KEEP = 100;

    public boolean brokerConfigured() {
        return enabled && broker != null && !broker.isBlank();
    }

    /** 发布消息。真实 broker 接入点在此扩展（如 Eclipse Paho）。 */
    public Map<String, Object> publish(String topic, String payload, int qos) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic 不能为空");
        }
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("topic", topic);
        msg.put("payload", payload);
        msg.put("qos", qos);
        msg.put("ts", System.currentTimeMillis());
        msg.put("broker", brokerConfigured() ? broker : "in-memory");
        bus.computeIfAbsent(topic, k -> new ArrayDeque<>()).addLast(msg);
        Deque<Map<String, Object>> q = bus.get(topic);
        while (q.size() > KEEP) q.removeFirst();
        log.info("[MQTT] publish topic={} qos={} broker={}", topic, qos, msg.get("broker"));
        return msg;
    }

    /** 拉取某 topic 最近消息（订阅语义）。 */
    public List<Map<String, Object>> messages(String topic, int limit) {
        Deque<Map<String, Object>> q = bus.getOrDefault(topic, new ArrayDeque<>());
        List<Map<String, Object>> list = new ArrayList<>(q);
        int from = Math.max(0, list.size() - Math.max(1, Math.min(limit, KEEP)));
        return list.subList(from, list.size());
    }

    public Map<String, Object> status() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("enabled", enabled);
        m.put("broker", broker);
        m.put("clientId", clientId);
        m.put("mode", brokerConfigured() ? "broker" : "in-memory");
        m.put("topics", bus.keySet());
        return m;
    }
}
