package com.csp.platform.format.handler;

import com.csp.platform.entity.ContestInfo;
import com.csp.platform.format.ContestFormatHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/** CFS 场景化渗透：分层网络 + 递进解锁 + 递进得分，允许跨题库组卷。 */
@Slf4j
@Component
public class CfsFormatHandler implements ContestFormatHandler {

    @Override public String type() { return "CFS"; }

    @Override public boolean isTargetsDynamic() { return true; }

    /** CFS 需要多题库（CTF/SCENARIO/...) 混合组卷，放开单类型约束。 */
    @Override public boolean allowCrossBankTypes() { return true; }

    @Override public void onStart(ContestInfo contest) {
        log.info("[CFS] 开赛 contest={} 分层拓扑下发，各队实例化 LAYER1 入口", contest.getId());
    }

    @Override public void onScore(Long contestId, Long teamId, int delta, String reason) {
        log.debug("[CFS] 分层得分 contest={} team={} delta={} reason={}", contestId, teamId, delta, reason);
    }

    @Override public void onFinish(ContestInfo contest) {
        log.info("[CFS] 结束 contest={} 回收分层网络与靶机", contest.getId());
    }

    @Override public Set<String> capabilities() {
        return Set.of("LAYERED_NETWORK", "PROGRESSIVE_SCORE", "CROSS_BANK", "DYNAMIC_TARGET");
    }
}
