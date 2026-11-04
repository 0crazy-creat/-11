package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 战队成员（contest_team_member）。 */
@Data
@TableName("contest_team_member")
public class ContestTeamMember implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contestId;
    private Long teamId;
    private Long userId;
    /** CAPTAIN/MEMBER */
    private String role;
    private LocalDateTime joinedAt;
}