package com.csp.ctf.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 首杀榜条目：某题的最早正确提交。 */
@Data
@Builder
public class FirstBloodItem {
    private Long questionId;
    private String title;
    private String category;
    private String difficulty;
    private Integer score;

    private Long teamId;
    private Long globalTeamId;
    private String teamName;
    private String logo;

    /** 完成首杀的成员 */
    private Long userId;
    private String userNickname;

    private LocalDateTime solvedAt;
    /** 该题累计解题队伍数 */
    private Integer solveTeamCount;
}
