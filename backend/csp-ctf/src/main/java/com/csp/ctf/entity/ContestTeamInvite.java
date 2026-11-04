package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 战队邀请记录（contest_team_invite）。
 */
@Data
@TableName("contest_team_invite")
public class ContestTeamInvite implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 赛事ID */
    private Long contestId;

    /** 战队ID */
    private Long teamId;

    /** 邀请人用户ID */
    private Long inviterId;

    /** 被邀请人用户ID（接受后填充） */
    private Long inviteeId;

    /** 邀请码 */
    private String inviteCode;

    /** 状态: PENDING/ACCEPTED/EXPIRED/CANCELLED */
    private String status;

    /** 过期时间 */
    private LocalDateTime expireAt;

    /** 接受时间 */
    private LocalDateTime acceptedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
