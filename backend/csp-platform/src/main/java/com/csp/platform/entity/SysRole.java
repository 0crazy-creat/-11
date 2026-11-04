package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 角色（sys_role）。 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 角色码 ADMIN/JUDGE/TEACHER/PLAYER/VIEWER */
    private String code;
    private String name;
    private String description;
    /** 内置角色不可删 */
    private Integer builtin;
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}