package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态 Flag 上下文（对齐 GZCTF FlagContext 模型）。
 *
 * <p>每队每題唯一，持久化分配的 flag 值，支持审计和防作弊。
 * 与 flag_context 表一一对应。</p>
 */
@Data
@TableName("flag_context")
public class FlagContext {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long contestId;
    private Long teamId;
    private Long questionId;

    /** 分配给该队该题的 flag 值 */
    private String flag;

    /** STATIC / DYNAMIC */
    private String flagType;

    /** 是否已正确提交: null=未提交, 0=错误, 1=正确 */
    private Boolean isCorrect;

    private LocalDateTime submittedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
