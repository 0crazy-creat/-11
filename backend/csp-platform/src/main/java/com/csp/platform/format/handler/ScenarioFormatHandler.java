package com.csp.platform.format.handler;

import com.csp.platform.entity.ContestInfo;
import com.csp.platform.format.ContestFormatHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/** 场景实操赛：逐级闯关，按关卡累计得分。 */
@Slf4j
@Component
public class ScenarioFormatHandler implements ContestFormatHandler {

    @Override public String type() { return "SCENARIO"; }

    @Override public boolean isTargetsDynamic() { return false; }

    @Override public void onStart(ContestInfo contest) {
        log.info("[SCENARIO] 开赛 contest={} 场景关卡解锁至第 1 关", contest.getId());
    }

    @Override public void onScore(Long contestId, Long teamId, int delta, String reason) {
        log.debug("[SCENARIO] 关卡通过 contest={} team={} delta={}", contestId, teamId, delta);
    }

    @Override public void onFinish(ContestInfo contest) {
        log.info("[SCENARIO] 结束 contest={} 封盘", contest.getId());
    }

    @Override public Set<String> capabilities() {
        return Set.of("STAGE_BASED", "CUMULATIVE_SCORE");
    }
}
