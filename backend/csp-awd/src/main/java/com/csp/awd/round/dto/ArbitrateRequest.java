package com.csp.awd.round.dto;

import lombok.Data;

/** JUDGE 仲裁请求。 */
@Data
public class ArbitrateRequest {
    /** 申诉ID */
    private Long disputeId;
    /** true=通过（回滚扣分），false=驳回 */
    private Boolean approve;
    /** 仲裁意见 */
    private String note;
}