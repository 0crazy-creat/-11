package com.csp.arena.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 靶机实例视图（对前端隐藏容器 ID 等内部细节）。 */
@Data
@Builder
public class InstanceVO {
    private Long id;
    private Long templateId;
    private String templateCode;
    private String templateName;
    private String category;
    private String difficulty;
    private Long ownerId;
    private String ownerName;
    private Long contestId;
    private Long teamId;
    private String accessUrl;
    private String hostIp;
    private Integer hostPort;
    private String status;
    private Integer health;
    private Double cpuPercent;
    private Double memUsedMb;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastCheckAt;
    /** 仅创建者/管理员可见 */
    private String flag;
}
