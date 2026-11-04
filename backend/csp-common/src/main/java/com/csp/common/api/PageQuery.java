package com.csp.common.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 分页查询基础参数。 */
@Data
public class PageQuery {
    @Min(value = 1, message = "页码最小为1")
    private long page = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 200, message = "每页条数最大为200")
    private long size = 10;

    private String sortBy;
    private String sortOrder = "desc";

    public long getOffset() { return (page - 1) * size; }
}
