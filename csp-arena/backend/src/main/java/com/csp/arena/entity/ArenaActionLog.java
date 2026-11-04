package com.csp.arena.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 靶场操作审计。 */
@Data
@TableName("arena_action_log")
public class ArenaActionLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long instanceId;
    private String action;
    private Long actorId;
    private String actorName;
    private String detail;
    private Integer success;
    private Long costMs;
    private LocalDateTime createdAt;
}
