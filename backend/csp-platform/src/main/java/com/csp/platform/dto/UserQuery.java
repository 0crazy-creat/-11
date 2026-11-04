package com.csp.platform.dto;

import com.csp.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用户分页查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {
    /** 用户名/昵称/真实姓名/手机号 模糊匹配 */
    private String keyword;
    private String orgCode;
    /** 1正常 0禁用 2锁定 */
    private Integer status;
    private String roleCode;
}