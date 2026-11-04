package com.csp.awd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 攻击得分提交：提交从对手靶机拿到的 flag。 */
@Data
public class AttackSubmitRequest {
    @NotNull(message = "竞赛ID不能为空")
    private Long contestId;

    /** 被攻击方战队ID（flag 所属战队） */
    @NotNull(message = "目标战队ID不能为空")
    private Long victimTeamId;

    /** 攻击方战队ID（缺省取当前登录用户所属战队） */
    private Long attackerTeamId;

    /** 题目/靶机题目ID */
    private Long questionId;

    @NotBlank(message = "flag 不能为空")
    private String flag;
}