package com.csp.awd.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 靶机视图（对参赛队隐去宿主机信息，仅暴露可访问地址）。 */
@Data
@Builder
public class TargetVO {
    private Long id;
    private Long contestId;
    private Long teamId;
    private Long questionId;
    private String ip;
    private Integer port;
    private String status;
    private Integer health;
    private Double cpuPercent;
    private Double memUsedMb;
    private LocalDateTime lastCheckAt;

    /** 可直接访问的靶场地址，如 http://127.0.0.1:20000 */
    private String accessUrl;
}