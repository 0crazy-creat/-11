package com.csp.infra.docker;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 容器创建配置（对齐 GZCTF ContainerConfig）。
 *
 * <p>替代旧 {@link TargetSpec}，新增 TTL、网络模式、流量捕获等字段。</p>
 */
@Data
@Builder
public class ContainerConfig {
    private Long contestId;
    private Long teamId;
    private Long questionId;
    private Long userId;
    private String image;
    private int exposedPort;
    private Map<String, String> env;
    private List<String> cmd;
    private String flag;
    private NetworkMode networkMode;
    private double cpuLimit;
    private int memoryLimit;
    private int storageLimit;
    private boolean enableTrafficCapture;
    /** 容器存活时长（分钟），默认 120 */
    @Builder.Default
    private int ttlMinutes = 120;

    public String containerName() {
        String raw = "csp-c" + contestId + "-t" + teamId + "-q" + questionId;
        if (flag != null && !flag.isBlank()) {
            String hash = Integer.toHexString(flag.hashCode());
            raw = raw + "-" + hash.substring(0, Math.min(8, hash.length()));
        }
        return raw;
    }
}
