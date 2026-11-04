package com.csp.awd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AWD 轮次自动编排定时任务。
 *
 * <p>每 10 秒扫描一次进行中的轮次，按时间自动推进：
 * RUNNING 到点 → CHECKING；CHECKING 超时 → FINISHED（触发结算）。</p>
 *
 * <p>多实例部署下，推进由 {@link AwdRoundService#transit} 内部的 Redis 锁 + CAS 保证幂等，
 * 因此无需额外的调度选主。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwdRoundScheduler {

    private final AwdRoundService roundService;

    @Scheduled(fixedDelayString = "${csp.awd.scheduler.round-tick-ms:10000}",
            initialDelayString = "${csp.awd.scheduler.round-initial-delay-ms:15000}")
    public void tick() {
        try {
            int n = roundService.tick();
            if (n > 0) {
                log.info("AWD 轮次自动推进 {} 个", n);
            }
        } catch (Exception e) {
            log.error("AWD 轮次自动推进异常", e);
        }
    }
}