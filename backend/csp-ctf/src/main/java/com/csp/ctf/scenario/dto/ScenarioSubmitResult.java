package com.csp.ctf.scenario.dto;

import lombok.Builder;
import lombok.Data;

/** 场景实操赛提交结果。 */
@Data
@Builder
public class ScenarioSubmitResult {
    private boolean correct;
    private String message;
    private int score;
    private int totalScore;
    private boolean firstBlood;
    /** 该场景是否因此次提交而全部完成 */
    private boolean scenarioCompleted;
}
