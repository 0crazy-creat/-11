package com.csp.platform.format.handler;

import com.csp.platform.entity.ContestInfo;
import com.csp.platform.format.ContestFormatHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/** AWD 攻防赛：动态靶机，开赛拉起首轮。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwdFormatHandler implements ContestFormatHandler {

    @Override public String type() { return "AWD"; }

    @Override public boolean isTargetsDynamic() { return true; }

    @Override public void onStart(ContestInfo contest) {
        log.info("[AWD] 开赛 contest={} 第一轮由 AwdRoundService.start 拉起", contest.getId());
    }

    @Override public void onScore(Long contestId, Long teamId, int delta, String reason) {
        log.debug("[AWD] 计分 contest={} team={} delta={} reason={}", contestId, teamId, delta, reason);
    }

    @Override public void onFinish(ContestInfo contest) {
        log.info("[AWD] 结束 contest={} 封盘并回收靶机", contest.getId());
    }

    @Override public Set<String> capabilities() {
        return Set.of("ATTACK_DEFENSE", "DYNAMIC_TARGET", "ROUND_BASED", "SLA_CHECK");
    }
}
