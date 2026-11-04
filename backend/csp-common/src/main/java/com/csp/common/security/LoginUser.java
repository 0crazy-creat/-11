package com.csp.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/** 当前登录用户上下文（从 JWT 解析而来）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String nickname;
    /** 平台角色码：ADMIN / JUDGE / TEACHER / PLAYER / VIEWER */
    private Set<String> roles;
    /** 细粒度权限点 */
    private Set<String> permissions;
    /** 当前比赛命名空间（按比赛粒度数据隔离） */
    private Long contestId;
    /** ABAC 属性：所属院校 */
    private String orgCode;
    /** ABAC 属性：院校名称 */
    private String orgName;
    /** ABAC 属性：班级名称 */
    private String className;
    private String clientIp;
    private String deviceId;

    public boolean hasRole(String role) { return roles != null && roles.contains(role); }

    public boolean hasPermission(String perm) {
        return permissions != null && (permissions.contains("*") || permissions.contains(perm));
    }
}
