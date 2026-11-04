package com.csp.platform.dto;

import com.csp.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 通知查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotifyQuery extends PageQuery {
    private Long contestId;
    private Integer readFlag;
    /** 是否包含广播消息（默认 true） */
    private Boolean includeBroadcast = Boolean.TRUE;
}
