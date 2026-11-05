package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csp.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 赛事主表（平台侧完整映射，CTF/AWD 子系统只读其中部分字段）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("contest")
public class ContestInfo extends BaseEntity {

    private String code;
    private String name;
    /** CTF/AWD/AWDP/THEORY/SCENARIO/CFS */
    private String type;
    /** INDIVIDUAL/TEAM */
    private String mode;
    /** DRAFT/PENDING/SIGNUP/RUNNING/FINISHED/ARCHIVED */
    private String status;
    private LocalDateTime signupStart;
    private LocalDateTime signupEnd;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer maxPlayers;
    private Integer allowLate;
    /** INTERNAL/EXTERNAL */
    private String networkMode;
    private Integer blackout;
    private Integer dynamicFlag;
    private Integer firstBlood;
    private String description;
    private String config;
    private String schemaName;
    private String organizer;

    /** 已报名战队数（列表页展示用，非持久化） */
    @TableField(exist = false)
    private Integer teamCount;

    /** 当前登录用户是否已报名（列表页展示用，非持久化） */
    @TableField(exist = false)
    private Boolean joined;
}
