package com.csp.awd.round.dto;

import lombok.Data;

/** 选手提交申诉请求。 */
@Data
public class DisputeSubmitRequest {
    /** 关联题目/靶机，null 表示针对整轮 */
    private Long questionId;
    /** 申诉类型：SCORE / SLA / FLAG / OTHER，默认 SCORE */
    private String disputeType;
    /** 申诉理由（必填） */
    private String reason;
    /** 证据链接/说明 */
    private String evidence;
}