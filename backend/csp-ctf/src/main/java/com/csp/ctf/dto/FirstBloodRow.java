package com.csp.ctf.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 首杀明细行：某题最早的正确提交。 */
@Data
public class FirstBloodRow {
    private Long questionId;
    private Long teamId;
    private Long userId;
    private LocalDateTime solvedAt;
}
