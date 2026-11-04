package com.csp.infra.docker;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 容器操作结果（对齐 GZCTF Container 数据模型）。
 *
 * <p>包含容器创建后的全部元信息：ID、状态、端口映射、TTL 等。
 * 由 {@link ContainerManager} 返回，上层服务负责持久化到数据库。</p>
 */
@Data
@Builder
public class ContainerResult {
    private String containerId;
    private String containerName;
    private String image;
    private ContainerStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime expectStopAt;
    private String ip;
    private int port;
    private int publicPort;
    private boolean simulated;
}
