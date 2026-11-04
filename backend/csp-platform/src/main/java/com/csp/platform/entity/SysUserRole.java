package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** 用户-角色关联（sys_user_role）。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user_role")
public class SysUserRole implements Serializable {
    private Long userId;
    private Long roleId;
}