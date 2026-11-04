package com.csp.infra.abac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ABAC 决策审计日志（abac_audit_log）。
 */
@Data
@TableName("abac_audit_log")
public class AbacAuditLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 匹配的策略ID */
    private Long policyId;

    /** 资源类型 */
    private String resourceType;

    /** 资源ID */
    private String resourceId;

    /** 操作 */
    private String action;

    /** 决策结果: ALLOW/DENY */
    private String decision;

    /** 决策原因 */
    private String reason;

    /** 请求IP */
    private String ipAddress;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
