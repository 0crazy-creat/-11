package com.csp.arena.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 基础实体：主键 + 审计 + 逻辑删除。 */
@Data
public abstract class BaseEntity implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    @TableLogic
    private Integer deleted;
}
