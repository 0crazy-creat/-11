package com.csp.awd.dto;

import lombok.Builder;
import lombok.Data;

/** 单队 AWD 积分明细。 */
@Data
@Builder
public class ScoreVO {
    private Long contestId;
    private Long teamId;
    /** 总分（攻击 + 防御 - SLA 扣分） */
    private long score;
    /** 攻击得分 */
    private long attackScore;
    /** 防御（修补/SLA）得分 */
    private long defenseScore;
    /** SLA 可用性扣分（负数） */
    private long slaPenalty;
    /** 被攻破次数 */
    private long compromisedCount;
    /** 成功攻击次数 */
    private long attackSuccessCount;
    /** 排名（1 起） */
    private Long rank;
}