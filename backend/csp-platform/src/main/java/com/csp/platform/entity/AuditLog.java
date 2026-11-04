package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 审计日志（只追加，不可修改/删除）。 */
@Data
@TableName("audit_log")
public class AuditLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String traceId;
    private Long userId;
    private String username;
    /** 操作类型，如 QUESTION_CREATE / CONTEST_STATUS */
    private String action;
    private String resource;
    /** 明细 JSON 字符串（已脱敏） */
    private String detail;
    private String ip;
    private String ua;
    /** SUCCESS / FAIL */
    private String result;
    private Long costMs;
    private LocalDateTime createdAt;
}
