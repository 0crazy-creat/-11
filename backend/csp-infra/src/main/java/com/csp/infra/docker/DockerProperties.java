package com.csp.infra.docker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 靶场容器调度配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "csp.docker")
public class DockerProperties {
    /** Docker 守护进程地址，留空使用本地默认 */
    private String host = "";
    private int connectTimeout = 10;
    private int responseTimeout = 60;
    /** 端口池起始 */
    private int portStart = 20000;
    private int portEnd = 30000;
    /** 单容器默认资源限制 */
    private String defaultCpu = "1.0";
    private String defaultMemory = "512m";
    /** 靶场网络名前缀（VLAN 隔离用） */
    private String networkPrefix = "csp-net-";
    /** 拉取镜像超时（秒） */
    private int pullTimeoutSeconds = 300;
    /** 是否启用容器调度（无 Docker 环境时降级为模拟模式，便于开发调试） */
    private boolean enabled = true;
}
