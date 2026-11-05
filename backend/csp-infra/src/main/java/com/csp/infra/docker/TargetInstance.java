package com.csp.infra.docker;

import lombok.Builder;
import lombok.Data;

/** 已拉起的靶机实例。 */
@Data
@Builder
public class TargetInstance {
    private String containerId;
    private String containerName;
    private String host;
    private int port;
    /** SSH 宿主端口，0 表示未映射 */
    private int sshPort;
    private String ip;
    private String status;
    private boolean simulated;
}
