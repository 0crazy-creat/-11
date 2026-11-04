package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 排行榜快照（scoreboard）。 */
@Data
@TableName("scoreboard")
public class Scoreboard implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contestId;
    private Long teamId;
    private Long score;
    private Integer solved;
    private Long attackScore;
    private Long defenseScore;
    private Integer penalty;
    private LocalDateTime lastSolveAt;
    private Integer rankNo;
    private LocalDateTime updatedAt;
}
