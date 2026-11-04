package com.csp.ctf.scenario.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/** 场景实操赛：场景树节点（场景 → 目标 → 任务 → 关键证据）。 */
@Data
@Builder
public class ScenarioNodeVO {
    private Long questionId;
    private String title;
    private String category;
    private String content;
    private Integer score;
    private String difficulty;
    private String attachmentUrl;
    private String imageName;
    private boolean docker;
    /** 是否已完成 */
    private boolean solved;
    /** 已完成的任务数 / 总任务数（仅场景节点有效） */
    private int solvedTasks;
    private int totalTasks;
    /** 前置任务（必须先完成） */
    private Long preQuestionId;
    private List<ScenarioNodeVO> children;
}
