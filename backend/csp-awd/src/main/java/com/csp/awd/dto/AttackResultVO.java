package com.csp.awd.dto;

import lombok.Builder;
import lombok.Data;

/** 攻击提交判定结果。 */
@Data
@Builder
public class AttackResultVO {
    private boolean success;
    private String message;
    /** 本次获得攻击分 */
    private long gainedScore;
    /** 是否为首杀（额外奖励） */
    private boolean firstBlood;
    /** 被攻击方扣分（SLA 扣分，负值） */
    private long victimPenalty;
    /** 攻击方当前总分 */
    private long attackerTotalScore;
}