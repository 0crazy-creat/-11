package com.csp.ctf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 排行榜条目。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankItem {
    private int rank;
    /** 参赛记录 ID（contest_team.id） */
    private Long teamId;
    /** 全局战队 ID（team.id）；个人参赛为 null，前端据此跳转战队主页 */
    private Long globalTeamId;
    private String teamName;
    private String logo;
    private String orgName;
    private long score;
    private int solved;
    private LocalDateTime lastSolveAt;

    /** 是否获得过一血（该赛事内任一道题的首个正确提交）。 */
    private Boolean firstBlood;
    /** 一血题目数，用于榜单角标展示。 */
    private Integer firstBloodCount;
}
