package com.csp.ctf.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 理论赛单题（不含答案）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheoryQuestionVO {
    private Long questionId;
    private Integer sortNo;
    private String title;
    private String content;
    private String category;
    private String difficulty;
    private int score;
}