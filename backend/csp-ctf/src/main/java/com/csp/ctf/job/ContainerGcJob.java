package com.csp.ctf.job;

import com.csp.ctf.entity.ChallengeContainer;
import com.csp.ctf.service.ContainerLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 容器 GC 定时任务（对齐 GZCTF RuntimeCronJobs.ContainerChecker）。
 *
 * <p>每 3 分钟扫描一次到期容器并销毁，同时清理数据库中状态不一致的脏数据。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerGcJob {

    private final ContainerLifecycleService lifecycleService;

    @Scheduled(fixedRate = 180_000, initialDelay = 30_000)
    public void gcExpiredContainers() {
        List<ChallengeContainer> dying = lifecycleService.getDyingContainers();
        if (dying.isEmpty()) return;

        log.info("容器 GC: 发现 {} 个到期容器", dying.size());
        int destroyed = 0;
        int errors = 0;

        for (ChallengeContainer container : dying) {
            try {
                boolean ok = lifecycleService.forceDestroy(container.getId());
                if (ok) {
                    destroyed++;
                } else {
                    errors++;
                }
            } catch (Exception e) {
                log.warn("容器 GC 销毁失败 id={}: {}", container.getId(), e.getMessage());
                errors++;
            }
        }

        log.info("容器 GC 完成: 销毁={}, 失败={}", destroyed, errors);
    }
}
