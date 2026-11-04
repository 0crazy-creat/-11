package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 靶机容器实例（对齐 GZCTF Container 模型）。
 *
 * <p>持久化容器全生命周期状态，服务重启后可恢复追踪。
 * 与 challenge_container 表一一对应。</p>
 */
@Data
@TableName("challenge_container")
public class ChallengeContainer {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long contestId;
    private Long teamId;
    private Long questionId;
    private Long userId;

    private String image;
    private String containerId;
    private String containerName;

    /** PENDING / RUNNING / STOPPED / DESTROYED / ERROR */
    private String status;

    private LocalDateTime startedAt;
    private LocalDateTime expectStopAt;

    private String ip;
    private Integer port;
    private String publicIp;
    private Integer publicPort;

    private Boolean isProxy;
    /** OPEN / ISOLATED / CUSTOM */
    private String networkMode;

    private Double cpuLimit;
    private Integer memoryLimit;
    private Integer storageLimit;

    private Boolean simulated;
    private LocalDateTime lastOperationAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
