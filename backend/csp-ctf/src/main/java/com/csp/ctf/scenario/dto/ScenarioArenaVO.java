package com.csp.ctf.scenario.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/** 场景实操赛选手端视图：场景树 + 我的进度 + 总分。 */
@Data
@Builder
public class ScenarioArenaVO {
    private Long contestId;
    private String contestName;
    private int totalScore;
    private int myScore;
    private int solvedCount;
    private int totalCount;
    private List<ScenarioNodeVO> scenarios;
}
