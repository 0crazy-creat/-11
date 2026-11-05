package com.csp.infra.docker;

import com.csp.common.exception.BizException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 容器生命周期管理器（对齐 GZCTF DockerManager）。
 *
 * <p>职责：
 * <ul>
 *   <li>镜像管理：拉取策略 + 去重</li>
 *   <li>容器创建：重试 3 次（处理名称冲突自动清理）</li>
 *   <li>容器启动：重试 3 次（500ms 间隔）</li>
 *   <li>状态检测：InspectContainer 检查 Dead/OOMKilled/Restarting/Running</li>
 *   <li>容器销毁：Force Remove + 异常容错</li>
 *   <li>端口分配：让 Docker 选择随机临时端口</li>
 * </ul>
 *
 * <p>Docker 不可用、镜像缺失或容器创建失败时抛出明确的业务异常，不再静默降级为模拟容器。</p>
 */
@Slf4j
@Service
public class ContainerManager {

    private static final int MAX_CREATE_ATTEMPTS = 3;
    private static final int MAX_START_ATTEMPTS = 3;
    private static final Duration START_RETRY_DELAY = Duration.ofMillis(500);

    private final ContainerProvider provider;
    private final DockerProperties props;
    private final ImagePullDedup imagePullDedup = new ImagePullDedup();

    /**
     * 是否允许从镜像仓库拉取。默认关闭：靶场镜像通常由 docker load 导入，
     * 本地不存在时应立即报错，而不是卡在远程拉取上。
     */
    @Value("${csp.docker.pull-enabled:false}")
    private boolean pullEnabled;

    @Autowired
    public ContainerManager(@Autowired(required = false) ContainerProvider provider,
                            DockerProperties props) {
        this.provider = provider;
        this.props = props;
    }

    public boolean isAvailable() {
        return provider != null && provider.isAvailable();
    }

    /**
     * 创建并启动容器，返回结果（含实际端口映射）。
     * Docker 不可用时返回模拟实例。
     */
    public ContainerResult create(ContainerConfig config) {
        if (!isAvailable()) {
            throw new BizException(503, "Docker 服务不可用，无法创建靶机容器，请检查 Docker 守护进程");
        }

        DockerClient client = provider.getDockerClient();
        String imageName = config.getImage();
        String containerName = config.containerName();

        if (!ensureImage(client, imageName)) {
            log.error("靶机镜像不可用: {}", imageName);
            throw new BizException(409, "靶机镜像不可用：" + imageName + "，请先在服务器上构建或导入该镜像");
        }

        for (int attempt = 1; attempt <= MAX_CREATE_ATTEMPTS; attempt++) {
            try {
                return doCreate(client, config, containerName);
            } catch (Exception e) {
                log.warn("容器创建失败 (attempt={}/{}): {}", attempt, MAX_CREATE_ATTEMPTS, e.getMessage());
                if (attempt < MAX_CREATE_ATTEMPTS) {
                    tryRemoveByName(client, containerName);
                }
            }
        }

        log.error("容器创建彻底失败: {}", containerName);
        throw new BizException(500, "靶机容器创建失败：" + containerName + "，请检查镜像、端口与资源限制配置");
    }

    private ContainerResult doCreate(DockerClient client, ContainerConfig config, String containerName) {
        int exposedPort = config.getExposedPort() > 0 ? config.getExposedPort() : 80;
        String networkName = provider.getNetworkName(
                config.getNetworkMode() != null ? config.getNetworkMode() : NetworkMode.ISOLATED);

        List<String> env = buildEnv(config);

        Ports portBindings = new Ports();
        portBindings.bind(ExposedPort.tcp(exposedPort), Ports.Binding.bindPort(0));

        CreateContainerCmd cmd = client.createContainerCmd(config.getImage())
                .withName(containerName)
                .withEnv(env)
                .withExposedPorts(ExposedPort.tcp(exposedPort))
                .withHostConfig(HostConfig.newHostConfig()
                        .withPortBindings(portBindings)
                        .withNetworkMode(networkName)
                        .withCpuQuota((long) (config.getCpuLimit() * 100000))
                        .withMemory((long) config.getMemoryLimit() * 1024 * 1024)
                        .withRestartPolicy(RestartPolicy.noRestart()));

        if (config.getCmd() != null && !config.getCmd().isEmpty()) {
            cmd.withCmd(config.getCmd());
        }

        CreateContainerResponse resp = cmd.exec();
        String containerId = resp.getId();

        for (int attempt = 1; attempt <= MAX_START_ATTEMPTS; attempt++) {
            try {
                client.startContainerCmd(containerId).exec();
                break;
            } catch (Exception e) {
                if (attempt == MAX_START_ATTEMPTS) {
                    tryRemove(client, containerId);
                    throw e;
                }
                try { Thread.sleep(START_RETRY_DELAY.toMillis()); } catch (InterruptedException ignored) {}
            }
        }

        InspectContainerResponse info = client.inspectContainerCmd(containerId).exec();
        InspectContainerResponse.ContainerState state = info.getState();

        ContainerStatus status;
        if (state == null || state.getDead() || state.getOOMKilled() || state.getRestarting()) {
            status = ContainerStatus.ERROR;
            tryRemove(client, containerId);
            throw new RuntimeException("容器启动后状态异常: " + (state != null ? state.getStatus() : "null"));
        } else if (state.getRunning()) {
            status = ContainerStatus.RUNNING;
        } else {
            status = ContainerStatus.PENDING;
        }

        String ip = "";
        int publicPort = 0;
        NetworkSettings ns = info.getNetworkSettings();
        if (ns != null && ns.getNetworks() != null) {
            for (Map.Entry<String, ContainerNetwork> e : ns.getNetworks().entrySet()) {
                if (e.getValue().getIpAddress() != null) {
                    ip = e.getValue().getIpAddress();
                    break;
                }
            }
        }

        if (ns != null && ns.getPorts() != null) {
            Ports ports = ns.getPorts();
            Ports.Binding[] bindings = ports.getBindings().get(ExposedPort.tcp(exposedPort));
            if (bindings != null && bindings.length > 0) {
                try {
                    publicPort = Integer.parseInt(bindings[0].getHostPortSpec());
                } catch (NumberFormatException ignored) {}
            }
        }

        LocalDateTime startedAt = state.getStartedAt() != null
                ? LocalDateTime.parse(state.getStartedAt().replace("Z", ""))
                : LocalDateTime.now();
        LocalDateTime expectStopAt = startedAt.plusMinutes(config.getTtlMinutes());

        return ContainerResult.builder()
                .containerId(containerId)
                .containerName(containerName)
                .image(config.getImage())
                .status(status)
                .startedAt(startedAt)
                .expectStopAt(expectStopAt)
                .ip(ip)
                .port(exposedPort)
                .publicPort(publicPort)
                .simulated(false)
                .build();
    }

    /** 销毁容器（Force Remove），异常容错。 */
    public boolean destroy(String containerId) {
        if (!isAvailable() || containerId == null || containerId.startsWith("sim-")) {
            return true;
        }
        try {
            provider.getDockerClient().removeContainerCmd(containerId).withForce(true).exec();
            log.debug("容器已销毁: {}", containerId);
            return true;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("No such container")) {
                return true;
            }
            log.warn("容器销毁失败 {}: {}", containerId, e.getMessage());
            return false;
        }
    }

    public boolean destroyByName(String containerName) {
        if (!isAvailable()) return true;
        try {
            List<Container> list = provider.getDockerClient().listContainersCmd()
                    .withShowAll(true).withNameFilter(List.of(containerName)).exec();
            for (Container c : list) {
                String[] names = c.getNames();
                if (names != null && names.length > 0 && ("/" + containerName).equals(names[0])) {
                    return destroy(c.getId());
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("按名称销毁容器失败 {}: {}", containerName, e.getMessage());
            return false;
        }
    }

    /** 查询容器状态。 */
    public ContainerStatus inspectStatus(String containerId) {
        if (!isAvailable() || containerId == null || containerId.startsWith("sim-")) {
            return ContainerStatus.DESTROYED;
        }
        try {
            InspectContainerResponse info = provider.getDockerClient().inspectContainerCmd(containerId).exec();
            InspectContainerResponse.ContainerState state = info.getState();
            if (state == null) return ContainerStatus.ERROR;
            if (state.getDead() || state.getOOMKilled()) return ContainerStatus.ERROR;
            if (state.getRunning()) return ContainerStatus.RUNNING;
            if (state.getRestarting()) return ContainerStatus.PENDING;
            return ContainerStatus.STOPPED;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("No such container")) {
                return ContainerStatus.DESTROYED;
            }
            return ContainerStatus.ERROR;
        }
    }

    /** 重置容器：销毁后重建。 */
    public ContainerResult reset(ContainerConfig config) {
        destroyByName(config.containerName());
        return create(config);
    }

    private boolean ensureImage(DockerClient client, String image) {
        // 1. 本地已有直接用
        try {
            client.inspectImageCmd(image).exec();
            return true;
        } catch (Exception ignored) {
            log.warn("本地镜像不存在: {}", image);
        }

        // 2. 默认不联网拉取，快速失败，由上层给出「请先导入镜像」的明确提示
        if (!pullEnabled) {
            return false;
        }

        // 3. 显式开启拉取时才尝试，超时收紧到 60s
        try {
            CompletableFuture<Boolean> pull = new CompletableFuture<>();
            client.pullImageCmd(image)
                    .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<>() {
                        @Override
                        public void onNext(PullResponseItem item) {
                            if (item.isErrorIndicated()) {
                                pull.complete(false);
                            }
                        }
                        @Override
                        public void onComplete() {
                            pull.complete(true);
                        }
                        @Override
                        public void onError(Throwable t) {
                            pull.complete(false);
                        }
                    }).awaitCompletion(Duration.ofSeconds(60).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            return pull.getNow(false);
        } catch (Exception e) {
            log.warn("镜像拉取超时或失败 {}: {}", image, e.getMessage());
            return false;
        }
    }

    private List<String> buildEnv(ContainerConfig config) {
        List<String> env = new ArrayList<>();
        env.add("CSP_TEAM_ID=" + config.getTeamId());
        env.add("CSP_USER_ID=" + (config.getUserId() != null ? config.getUserId() : ""));
        env.add("CSP_CHALLENGE_ID=" + config.getQuestionId());
        if (config.getContestId() != null) {
            env.add("CSP_CONTEST_ID=" + config.getContestId());
        }
        if (config.getFlag() != null && !config.getFlag().isBlank()) {
            env.add("CSP_FLAG=" + config.getFlag());
            env.add("FLAG=" + config.getFlag());
        }
        if (config.getEnv() != null) {
            config.getEnv().forEach((k, v) -> env.add(k + "=" + v));
        }
        return env;
    }

    private void tryRemoveByName(DockerClient client, String name) {
        try {
            List<Container> list = client.listContainersCmd().withShowAll(true).withNameFilter(List.of(name)).exec();
            for (Container c : list) {
                try { client.removeContainerCmd(c.getId()).withForce(true).exec(); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    private void tryRemove(DockerClient client, String id) {
        try { client.removeContainerCmd(id).withForce(true).exec(); } catch (Exception ignored) {}
    }

}
