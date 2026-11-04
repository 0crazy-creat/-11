package com.csp.platform.enums;

import java.util.Map;
import java.util.Set;

/** 赛事状态机：DRAFT → PENDING → SIGNUP → RUNNING → FINISHED → ARCHIVED。 */
public enum ContestStatus {
    DRAFT, PENDING, SIGNUP, RUNNING, FINISHED, ARCHIVED;

    private static final Map<ContestStatus, Set<ContestStatus>> TRANSITIONS = Map.of(
            DRAFT, Set.of(PENDING),
            PENDING, Set.of(SIGNUP, DRAFT),
            SIGNUP, Set.of(RUNNING, PENDING),
            RUNNING, Set.of(FINISHED),
            FINISHED, Set.of(ARCHIVED),
            ARCHIVED, Set.of()
    );

    public boolean canTransitTo(ContestStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public static ContestStatus of(String v) {
        return valueOf(v.trim().toUpperCase());
    }
}
