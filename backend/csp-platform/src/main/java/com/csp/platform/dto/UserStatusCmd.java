package com.csp.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 变更用户状态：1正常 0禁用 2锁定。 */
@Data
public class UserStatusCmd {

    @NotNull(message = "status 不能为空")
    @Min(value = 0, message = "status 取值 0/1/2")
    @Max(value = 2, message = "status 取值 0/1/2")
    private Integer status;
}