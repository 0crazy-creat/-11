package com.csp.platform.format.handler;

import com.csp.platform.entity.ContestInfo;
import com.csp.platform.format.ContestFormatHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/** AWDP 增强攻防：AWD + 防御修补闭环。 */
@Slf4j
@Component
public class AwdpFormatHandler implements ContestFormatHandler {

    @Override public String type() { return "AWDP"; }

    @Override public boolean isTargetsDynamic() { return true; }

    @Override public void onStart(ContestInfo contest) {
        log.info("[AWDP] 开赛 contest={} 攻防+修补双通道开启", contest.getId());
    }

    @Override public void onScore(Long contestId, Long teamId, int delta, String reason) {
        log.debug("[AWDP] 计分 contest={} team={} delta={} reason={}", contestId, teamId, delta, reason);
    }

    @Override public void onFinish(ContestInfo contest) {
        log.info("[AWDP] 结束 contest={} 封盘并回收靶机", contest.getId());
    }

    @Override public Set<String> capabilities() {
        return Set.of("ATTACK_DEFENSE", "DYNAMIC_TARGET", "ROUND_BASED", "SLA_CHECK", "DEFENSE_PATCH");
    }
}
