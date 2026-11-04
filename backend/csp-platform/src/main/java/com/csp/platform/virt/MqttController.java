package com.csp.platform.virt;

import com.csp.common.api.Result;
import com.csp.security.annotation.PublicApi;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** MQTT 消息桥接接口。 */
@Tag(name = "MQTT 消息总线")
@RestController
@RequestMapping("/api/mqtt")
@RequiredArgsConstructor
public class MqttController {

    private final MqttBridgeService mqtt;

    @Operation(summary = "发布消息")
    @RequirePermission("contest:control")
    @PostMapping("/publish")
    public Result<Map<String, Object>> publish(@RequestParam String topic,
                                               @RequestBody(required = false) String payload,
                                               @RequestParam(defaultValue = "0") Integer qos) {
        return Result.ok(mqtt.publish(topic, payload, qos));
    }

    @Operation(summary = "拉取 topic 消息（订阅）")
    @GetMapping("/messages")
    public Result<List<Map<String, Object>>> messages(@RequestParam String topic,
                                                      @RequestParam(defaultValue = "20") Integer limit) {
        return Result.ok(mqtt.messages(topic, limit));
    }

    @Operation(summary = "MQTT 状态")
    @PublicApi
    @GetMapping("/status")
    public Result<Map<String, Object>> status() {
        return Result.ok(mqtt.status());
    }
}
