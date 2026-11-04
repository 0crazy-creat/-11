package com.csp.awd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 靶机实例。 */
@Data
@TableName("awd_target")
public class AwdTarget {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contestId;
    private Long teamId;
    private Long questionId;
    private String containerId;
    private String ip;
    private Integer port;
    private String status;
    private Integer health;
    private Double cpuPercent;
    private Double memUsedMb;
    private LocalDateTime lastCheckAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
