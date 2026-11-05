package com.csp.ctf.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 截至某时刻的榜单聚合行（阶段榜/历史回放）。 */
@Data
public class StageBoardRow {
    private Long teamId;
    private Long score;
    private Integer solved;
    private LocalDateTime lastSolveAt;
}
