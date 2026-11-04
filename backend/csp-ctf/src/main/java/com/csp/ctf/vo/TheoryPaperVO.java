package com.csp.ctf.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 理论赛试卷（不含任何答案）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheoryPaperVO {
    private Long contestId;
    private String contestName;
    private LocalDateTime endAt;
    private int totalScore;
    private int questionCount;
    private List<TheoryQuestionVO> questions;
}