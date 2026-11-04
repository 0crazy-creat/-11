package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 作弊审计日志：记录反作弊检测事件。 */
@Data
@TableName("cheat_audit_log")
public class CheatAuditLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long contestId;
    private Long userId;
    private Long teamId;
    
    /** 检测类型（RATE_LIMIT,BRUTE_FORCE,MACHINE_SPEED,MULTI_IP,MULTI_DEVICE,SAME_ANSWER） */
    private String auditType;
    
    /** 严重程度（LOW,MEDIUM,HIGH,CRITICAL） */
    private String severity;
    
    /** 风险分（0-100） */
    private Integer riskScore;
    
    /** 详细信息（JSON格式） */
    private String detail;
    
    private String ipAddress;
    private String deviceId;
    
    /** 采取措施（WARNING,BLOCKED,SUSPENDED） */
    private String actionTaken;
    
    private LocalDateTime createdAt;
}
