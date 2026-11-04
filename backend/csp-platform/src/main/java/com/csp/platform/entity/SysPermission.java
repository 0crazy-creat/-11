package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 权限点（sys_permission）。 */
@Data
@TableName("sys_permission")
public class SysPermission implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 权限点，如 contest:create */
    private String code;
    private String name;
    private String module;
    /** API/MENU/BUTTON */
    private String type;
    private Long parentId;
}