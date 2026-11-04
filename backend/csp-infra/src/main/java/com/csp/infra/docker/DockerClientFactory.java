package com.csp.infra.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/** Docker 客户端构建。 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "csp.docker.enabled", havingValue = "true", matchIfMissing = true)
public class DockerClientFactory {

    private final DockerProperties props;

    @Bean(destroyMethod = "close")
    public DockerClient dockerClient() {
        // 仅在显式配置 host 时才覆盖；传 null 会被 docker-java 当作空值，导致 "uri was not specified"
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

        DockerClient client = DockerClientImpl.getInstance(config, httpClient);
        try {
            client.pingCmd().exec();
            log.info("Docker 连接成功: {}", config.getDockerHost());
        } catch (Exception e) {
            log.warn("Docker 不可用，靶场调度将降级为模拟模式: {}", e.getMessage());
        }
        return client;
    }
}
