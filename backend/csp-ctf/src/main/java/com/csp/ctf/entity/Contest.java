package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.csp.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 赛事（contest），只映射 CTF 子系统所需字段。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("contest")
public class Contest extends BaseEntity {

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
    /** 补报名开关 */
    private Integer allowLate;
    /** 是否冻结排行榜 */
    private Integer frozen;
    /** 动态 flag */
    private Integer dynamicFlag;
    /** 首杀奖励 */
    private Integer firstBlood;
    private String description;
    private String schemaName;
    private String organizer;
}
