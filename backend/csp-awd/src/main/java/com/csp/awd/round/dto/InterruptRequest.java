package com.csp.awd.round.dto;

import lombok.Data;

/** 轮次中断请求：type=SOFT/HARD，reason 必填。 */
@Data
public class InterruptRequest {
    /** SOFT（暂停攻击窗口，保留提交）/ HARD（立即中止并结算） */
    private String type;
    /** 中断原因（必填，用于赛后复盘与申诉依据） */
    private String reason;
}