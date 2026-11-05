package com.csp.ctf.scenario.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
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

    /**
     * 赛事状态（DRAFT/PENDING/SIGNUP/RUNNING/FINISHED/ARCHIVED）。
     * 供前端判断是否允许提交，避免未开赛时渲染可点的提交按钮后必然 409。
     */
    private String status;
    /** 比赛开始时间，null 表示不限。 */
    private LocalDateTime startAt;
    /** 比赛结束时间，null 表示不限。 */
    private LocalDateTime endAt;
    /** 当前是否处于可答题窗口（RUNNING 且 now ∈ [startAt, endAt]）。 */
    private boolean answering;
    /** 不可答题原因，answering=true 时为空串。 */
    private String closedReason;
}