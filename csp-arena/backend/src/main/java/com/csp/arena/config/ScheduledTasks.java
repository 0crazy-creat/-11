package com.csp.arena.config;

import com.csp.arena.service.InstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 靶场定时任务：健康巡检 + 过期回收。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final InstanceService instanceService;

    /** 每 60 秒巡检一次靶机健康。 */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void healthCheck() {
        try {
            int healthy = instanceService.healthCheckAll();
            log.debug("靶机巡检完成，健康数={}", healthy);
        } catch (Exception e) {
            log.warn("靶机巡检异常: {}", e.getMessage());
        }
    }

    /** 每 5 分钟回收过期实例。 */
    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void reclaim() {
        try {
            int n = instanceService.reclaimExpired();
            if (n > 0) log.info("回收过期靶机 {} 台", n);
        } catch (Exception e) {
            log.warn("过期回收异常: {}", e.getMessage());
        }
    }
}
