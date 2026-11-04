package com.csp.situation.dto;

import lombok.Builder;
import lombok.Data;

/** 赛事态势总览：大屏顶部指标卡。 */
@Data
@Builder
public class OverviewVO {
    private Long contestId;
    private String contestName;
    /** 报名队伍数 */
    private int teamCount;
    /** 在线靶机数 */
    private int targetCount;
    /** 健康靶机数 */
    private int healthyCount;
    /** 累计解题数 */
    private int solvedTotal;
    /** 累计提交数 */
    private int submitTotal;
    /** 当前领先战队名 */
    private String leadingTeam;
    /** 当前最高分 */
    private long topScore;
}
