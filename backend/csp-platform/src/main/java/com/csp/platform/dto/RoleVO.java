package com.csp.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 角色视图。 */
@Data
@Builder
public class RoleVO {
    private Long id;
    private String code;
    private String name;
    private String description;
    /** 1 为内置角色，不可删除 */
    private Integer builtin;
    private LocalDateTime createdAt;
    /** 角色拥有的权限点编码 */
    private List<String> permissions;
}