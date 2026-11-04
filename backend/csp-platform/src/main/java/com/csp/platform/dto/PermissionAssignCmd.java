package com.csp.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 角色权限点全量覆盖。 */
@Data
public class PermissionAssignCmd {

    @NotNull(message = "permissions 不能为 null")
    private List<String> permissions;
}