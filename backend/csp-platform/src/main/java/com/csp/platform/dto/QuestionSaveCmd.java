package com.csp.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/** 新建/更新题目入参。 */
@Data
public class QuestionSaveCmd {
    @NotBlank(message = "bankType 不能为空")
    @Pattern(regexp = "THEORY|CTF|SCENARIO|AWD", message = "bankType 必须为 THEORY/CTF/SCENARIO/AWD")
    private String bankType;

    private String category;

    @NotBlank(message = "题目标题不能为空")
    @Size(max = 255, message = "标题过长")
    private String title;

    private String content;
    private String answer;
    private String flag;

    @Min(value = 0, message = "分数不能为负")
    @Max(value = 100000, message = "分数过大")
    private Integer score = 100;

    @Pattern(regexp = "^(EASY|MEDIUM|HARD|INSANE)?$", message = "difficulty 非法")
    private String difficulty;

    /** JSON 数组字符串，如 ["web","sql"] */
    private String tags;
    private String attachment;
    private String writeup;
    private String imageName;
    private String cpuLimit;
    private String memLimit;
    private Integer status = 1;
    private String ext;
}
