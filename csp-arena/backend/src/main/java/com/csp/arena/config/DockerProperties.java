package com.csp.arena.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 靶场容器调度配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "arena.docker")
public class DockerProperties {
    private boolean enabled = true;
    private String host = "unix:///var/run/docker.sock";
    /** 端口池 */
    private int portStart = 21000;
    private int portEnd = 22000;
    /** 宿主机对外访问 IP（写进 accessUrl） */
    private String publicHost = "127.0.0.1";
    private String networkPrefix = "arena-net-";
    private String defaultCpu = "1.0";
    private String defaultMemory = "512m";
}
