package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 队长转让记录（contest_team_captain_transfer）。
 */
@Data
@TableName("contest_team_captain_transfer")
public class ContestTeamCaptainTransfer implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 赛事ID */
    private Long contestId;

    /** 战队ID */
    private Long teamId;

    /** 原队长用户ID */
    private Long fromCaptain;

    /** 新队长用户ID */
    private Long toCaptain;

    /** 转让原因 */
    private String reason;

    /** 转让时间 */
    private LocalDateTime transferredAt;
}
