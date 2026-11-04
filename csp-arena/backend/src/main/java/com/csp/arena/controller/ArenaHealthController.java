package com.csp.arena.controller;

import com.csp.arena.config.Result;
import com.csp.arena.service.DockerScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** 靶场服务健康与能力探测。 */
@RestController
@RequestMapping("/api/arena")
@RequiredArgsConstructor
public class ArenaHealthController {

    private final DockerScheduler scheduler;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("service", "csp-arena");
        m.put("status", "UP");
        m.put("dockerAvailable", scheduler.dockerAvailable());
        m.put("publicHost", scheduler.publicHost());
        m.put("time", System.currentTimeMillis());
        return Result.ok(m);
    }
}
