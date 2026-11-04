package com.csp.infra.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Docker 客户端提供者（对齐 GZCTF DockerProvider）。
 *
 * <p>职责：
 * <ul>
 *   <li>初始化 DockerClient 连接</li>
 *   <li>启动时预创建 open / isolated / custom 三套网络</li>
 *   <li>isolated 网络禁用 IP 伪装（阻断出站外网）</li>
 * </ul>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "csp.docker.enabled", havingValue = "true", matchIfMissing = true)
public class ContainerProvider {

    private final DockerProperties props;

    @Getter
    private DockerClient dockerClient;

    @Getter
    private final Map<NetworkMode, String> networkNames = new HashMap<>();

    @Getter
    private boolean available = false;

    public ContainerProvider(DockerProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        String prefix = props.getNetworkPrefix() != null ? props.getNetworkPrefix() : "csp-net-";
        networkNames.put(NetworkMode.OPEN, prefix + "open");
        networkNames.put(NetworkMode.ISOLATED, prefix + "isolated");
        networkNames.put(NetworkMode.CUSTOM, prefix + "custom");

        try {
            DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
            if (props.getHost() != null && !props.getHost().isBlank()) {
                builder.withDockerHost(props.getHost().trim());
            }
            DockerClientConfig config = builder.build();

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(props.getConnectTimeout()))
                    .responseTimeout(Duration.ofSeconds(props.getResponseTimeout()))
                    .build();

            dockerClient = DockerClientImpl.getInstance(config, httpClient);
            dockerClient.pingCmd().exec();
            available = true;
            log.info("Docker 连接成功: {}", config.getDockerHost());

            ensureNetworks();
        } catch (Exception e) {
            log.warn("Docker 不可用，靶场调度将降级为模拟模式: {}", e.getMessage());
            available = false;
        }
    }

    private void ensureNetworks() {
        if (!available || dockerClient == null) return;
        try {
            List<Network> existing = dockerClient.listNetworksCmd().exec();

            String openName = networkNames.get(NetworkMode.OPEN);
            if (existing.stream().noneMatch(n -> openName.equals(n.getName()))) {
                dockerClient.createNetworkCmd()
                        .withName(openName)
                        .withDriver("bridge")
                        .withAttachable(true)
                        .exec();
                log.info("创建 Docker 网络: {} (open)", openName);
            }

            String isolatedName = networkNames.get(NetworkMode.ISOLATED);
            if (existing.stream().noneMatch(n -> isolatedName.equals(n.getName()))) {
                Map<String, String> opts = new HashMap<>();
                opts.put("com.docker.network.bridge.enable_ip_masquerade", "false");
                dockerClient.createNetworkCmd()
                        .withName(isolatedName)
                        .withDriver("bridge")
                        .withAttachable(true)
                        .withOptions(opts)
                        .exec();
                log.info("创建 Docker 网络: {} (isolated, 禁止出站)", isolatedName);
            }

            String customName = networkNames.get(NetworkMode.CUSTOM);
            if (existing.stream().noneMatch(n -> customName.equals(n.getName()))) {
                dockerClient.createNetworkCmd()
                        .withName(customName)
                        .withDriver("bridge")
                        .withAttachable(true)
                        .exec();
                log.info("创建 Docker 网络: {} (custom)", customName);
            }
        } catch (Exception e) {
            log.warn("创建 Docker 网络失败（不影响启动）: {}", e.getMessage());
        }
    }

    public String getNetworkName(NetworkMode mode) {
        return networkNames.getOrDefault(mode, networkNames.get(NetworkMode.ISOLATED));
    }
}
