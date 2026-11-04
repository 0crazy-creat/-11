package com.csp.platform.format.handler;

import com.csp.platform.entity.ContestInfo;
import com.csp.platform.format.ContestFormatHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/** CTF 解题赛：静态题目靶机，无开赛/结束特化动作。 */
@Slf4j
@Component
public class CtfFormatHandler implements ContestFormatHandler {

    @Override public String type() { return "CTF"; }

    @Override public boolean isTargetsDynamic() { return false; }

    @Override public void onStart(ContestInfo contest) {
        log.info("[CTF] 开赛 contest={} 题目组卷完成，选手可开始解题", contest.getId());
    }

    @Override public void onScore(Long contestId, Long teamId, int delta, String reason) {
        log.debug("[CTF] 计分 contest={} team={} delta={} reason={}", contestId, teamId, delta, reason);
    }

    @Override public Set<String> capabilities() {
        return Set.of("STATIC_CHALLENGE", "FLAG_SUBMIT", "SCORE_DECAY");
    }
}
