package com.csp.ctf.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 解题矩阵单元格：某战队对某题的最早一次正确提交。
 * <p>bloodRank 为该题的全局解出顺序，1=一血、2=二血、3=三血。</p>
 */
@Data
public class SolveCell {
    /** 参赛记录 ID（contest_team.id） */
    private Long teamId;
    private Long questionId;
    /** 提交当时的得分，回放不受后续动态衰减影响 */
    private Long score;
    private LocalDateTime solvedAt;
    private Integer bloodRank;
}
