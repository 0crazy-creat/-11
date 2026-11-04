package com.csp.platform.dto;

import com.csp.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 赛事分页查询。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContestQuery extends PageQuery {
    private String status;
    private String type;
    private String keyword;
}
