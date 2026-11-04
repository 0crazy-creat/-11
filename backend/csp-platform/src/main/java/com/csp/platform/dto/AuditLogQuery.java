package com.csp.platform.dto;

import com.csp.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 审计日志查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLogQuery extends PageQuery {
    private Long userId;
    private String username;
    private String action;
    private String result;
    private String traceId;
    private LocalDateTime from;
    private LocalDateTime to;
}
