package com.csp.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** 新增/编辑角色。 */
@Data
public class RoleSaveCmd {

    /** 角色码仅允许大写字母、数字与下划线 */
    @NotBlank(message = "角色码不能为空")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,63}$", message = "角色码需为大写字母开头的 2-64 位大写字母/数字/下划线")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称过长")
    private String name;

    @Size(max = 255, message = "描述过长")
    private String description;

    /** 权限点编码列表 */
    private List<String> permissions;
}