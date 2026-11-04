package com.csp.platform.format.handler;

import com.csp.platform.entity.ContestInfo;
import com.csp.platform.format.ContestFormatHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/** 理论赛：静态答题，交卷判分。 */
@Slf4j
@Component
public class TheoryFormatHandler implements ContestFormatHandler {

    @Override public String type() { return "THEORY"; }

    @Override public boolean isTargetsDynamic() { return false; }

    @Override public void onStart(ContestInfo contest) {
        log.info("[THEORY] 开赛 contest={} 答卷下发", contest.getId());
    }

    @Override public void onScore(Long contestId, Long teamId, int delta, String reason) {
        log.debug("[THEORY] 判分 contest={} team={} delta={}", contestId, teamId, delta);
    }

    @Override public void onFinish(ContestInfo contest) {
        log.info("[THEORY] 结束 contest={} 强制收卷", contest.getId());
    }

    @Override public Set<String> capabilities() {
        return Set.of("QUIZ", "AUTO_JUDGE", "NO_TARGET");
    }
}
