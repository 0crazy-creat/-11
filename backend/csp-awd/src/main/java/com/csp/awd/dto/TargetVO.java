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
    /** 所属战队名 */
    private String teamName;
    private Long questionId;
    /** 题目名（靶机对应的赛题） */
    private String questionTitle;
    private String ip;
    private Integer port;
    /** SSH 连接信息 */
    private Integer sshPort;
    private String sshUser;
    private String sshPassword;
    private String sshCommand;
    /** 服务检测 */
    private Integer serviceOk;
    private String lastCheckMsg;
    private String status;
    private Integer health;
    private Double cpuPercent;
    private Double memUsedMb;
    private LocalDateTime lastCheckAt;

    /** 可直接访问的靶场地址，如 http://127.0.0.1:20000 */
    private String accessUrl;
}