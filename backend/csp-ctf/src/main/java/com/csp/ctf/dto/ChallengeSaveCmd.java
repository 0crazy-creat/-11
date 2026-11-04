package com.csp.ctf.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 题目新建/编辑请求（题目管理页）。 */
@Data
public class ChallengeSaveCmd {
    /** 赛事 ID，挂载用 */
    private Long contestId;
    @NotBlank(message = "题目标题不能为空")
    private String title;
    private String category;
    private String difficulty;
    private Integer score;
    private String flag;
    private String description;
    /** 前端字段名 attachmentUrl，映射到 question.attachment */
    private String attachmentUrl;
    private String tags;
    private String answer;
    private String bankType;
    private Integer status;
}
