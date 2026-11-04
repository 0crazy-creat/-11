package com.csp.arena.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 靶机实例。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arena_instance")
public class ArenaInstance extends BaseEntity {
    private Long templateId;
    private String templateCode;
    private Long ownerId;
    private String ownerName;
    private Long contestId;
    private Long teamId;
    private String containerId;
    private String containerName;
    private String image;
    private String hostIp;
    private Integer hostPort;
    private String accessUrl;
    private String flag;
    private String status;
    private Integer health;
    private Double cpuPercent;
    private Double memUsedMb;
    private LocalDateTime expiresAt;
    private LocalDateTime lastCheckAt;
}
