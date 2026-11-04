package com.csp.arena.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/** Docker 客户端配置。 */
@Slf4j
@Configuration
public class DockerConfig {

    @Value("${arena.docker.host:unix:///var/run/docker.sock}")
    private String dockerHost;

    @Bean(destroyMethod = "close")
    public DockerClient dockerClient() {
        DockerClientConfig cfg = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .withDockerTlsVerify(false)
                .build();
        DockerHttpClient http = new ApacheDockerHttpClient.Builder()
                .dockerHost(cfg.getDockerHost())
                .sslConfig(cfg.getSSLConfig())
                .maxConnections(50)
                .connectionTimeout(Duration.ofSeconds(10))
                .responseTimeout(Duration.ofSeconds(60))
                .build();
        DockerClient client = DockerClientImpl.getInstance(cfg, http);
        try {
            client.pingCmd().exec();
            log.info("Docker 连接成功: {}", dockerHost);
        } catch (Exception e) {
            log.warn("Docker 不可用，靶场将进入模拟模式: {}", e.getMessage());
        }
        return client;
    }
}
