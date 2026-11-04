package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** 角色-权限关联（sys_role_permission）。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role_permission")
public class SysRolePermission implements Serializable {
    private Long roleId;
    private Long permissionId;
}