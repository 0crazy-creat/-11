package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 全局战队（team）——与赛事无关，跨赛事复用。
 *
 * <p>赛事参赛记录见 {@link ContestTeam}，其 team_id 指向本表。</p>
 */
@Data
@TableName("team")
public class Team implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private String logo;
    private String description;
    private String slogan;
    private Long captainId;
    private Integer members;
    private Integer maxMembers;
    private String inviteCode;
    private LocalDateTime inviteExpireAt;

    /** 当前登录用户在该战队的角色（CAPTAIN/MEMBER，非成员为空）。 */
    @TableField(exist = false)
    private String myRole;

    /** 该战队当前生效的参赛赛事数。 */
    @TableField(exist = false)
    private Integer enrolledContests;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private Integer deleted;
}
