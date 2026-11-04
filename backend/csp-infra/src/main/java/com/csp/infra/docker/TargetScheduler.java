package com.csp.infra.docker;

import com.csp.common.exception.BizException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 靶场容器调度器：快速拉起 / 重置 / 销毁靶机，网络隔离，资源限额。
 * <p>无 Docker 环境时自动降级为模拟模式，返回可用的假端口，保证业务链路可测。</p>
 */
@Slf4j
@Service
public class TargetScheduler {

    private final DockerClient dockerClient;
    private final DockerProperties props;

    private final AtomicInteger portCursor = new AtomicInteger();
    private volatile Boolean dockerReady;

    @Autowired
    public TargetScheduler(@Autowired(required = false) DockerClient dockerClient, DockerProperties props) {
        this.dockerClient = dockerClient;
        this.props = props;
        if (dockerClient == null) {
            this.dockerReady = false;
            log.info("Docker 未启用，靶场调度将使用模拟模式");
        }
    }

    private boolean dockerAvailable() {
        if (dockerClient == null) return false;
        if (dockerReady != null) return dockerReady;
        synchronized (this) {
            if (dockerReady == null) {
                try {
                    dockerClient.pingCmd().exec();
                    // ping 通过即代表守护进程可用；enabled 仅作为额外的运维开关，
                    // 不再用它二次否决赛判结果（此前 enabled 绑定失败会导致误降级为模拟模式）。
                    dockerReady = true;
                    log.info("Docker 守护进程可用，靶场容器调度已启用（enabled 配置={}）", props.isEnabled());
                } catch (Exception e) {
                    log.warn("Docker 探测失败，使用模拟模式: {}", e.getMessage());
                    dockerReady = false;
                }
            }
            return dockerReady;
        }
    }

    /** 供健康检查/诊断使用：当前是否使用真实容器调度。 */
    public boolean isDockerAvailable() {
        return dockerAvailable();
    }

    /** 分配一个空闲宿主端口。 */
    private synchronized int allocatePort() {
        int span = Math.max(1, props.getPortEnd() - props.getPortStart());
        for (int i = 0; i < span; i++) {
            int p = props.getPortStart() + Math.floorMod(portCursor.getAndIncrement(), span);
            if (!isPortUsed(p)) return p;
        }
        throw BizException.of("端口池已耗尽，无法分配靶机端口");
    }

    private boolean isPortUsed(int port) {
        try (java.net.ServerSocket ss = new java.net.ServerSocket(port)) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /** 确保比赛专属网络存在（VLAN/网段隔离），返回网络名（Docker NetworkMode 需用名称）。 */
    private String ensureNetwork(Long contestId) {
        String name = props.getNetworkPrefix() + contestId;
        try {
            List<Network> nets = dockerClient.listNetworksCmd().withNameFilter(name).exec();
            if (nets != null && nets.stream().anyMatch(n -> name.equals(n.getName()))) return name;
            dockerClient.createNetworkCmd().withName(name).withDriver("bridge").exec();
            return name;
        } catch (Exception e) {
            log.warn("创建隔离网络失败 contest={}: {}，回退默认 bridge", contestId, e.getMessage());
            return "bridge";
        }
    }

    /**
     * 拉起靶机。
     */
    public TargetInstance create(TargetSpec spec) {
        Objects.requireNonNull(spec, "spec");
        if (spec.getImage() == null || spec.getImage().isBlank()) {
            throw BizException.of("靶机镜像不能为空");
        }

        if (!dockerAvailable()) {
            return simulate(spec, "RUNNING");
        }

        try {
            // 幂等：同名容器先清理
            removeByName(spec.containerName());

            String networkName = ensureNetwork(spec.getContestId());
            int hostPort = allocatePort();

            List<ExposedPort> exposed = List.of(ExposedPort.tcp(spec.getContainerPort()));
            Ports portBindings = new Ports();
            portBindings.bind(exposed.get(0), Ports.Binding.bindPort(hostPort));

            // 关键：自定义 bridge 网络必须用 Ports.Binding.bindPort 显式绑定并指定 hostIp，
            // 且 NetworkMode 只能写网络名（不是 ID），否则 Docker 会忽略端口映射导致容器不可达。
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withPortBindings(portBindings)
                    .withNetworkMode(networkName)
                    .withCpuQuota(toCpuQuota(spec.getCpuLimit()))
                    .withMemory(toBytes(spec.getMemoryLimit()))
                    .withRestartPolicy(RestartPolicy.unlessStoppedRestart())
                    .withAutoRemove(false);

            List<String> env = new ArrayList<>();
            if (spec.getEnv() != null) {
                spec.getEnv().forEach((k, v) -> env.add(k + "=" + v));
            }

            CreateContainerCmd cmd = dockerClient.createContainerCmd(spec.getImage())
                    .withName(spec.containerName())
                    .withEnv(env)
                    .withExposedPorts(exposed)
                    .withHostConfig(hostConfig);

            if (spec.getCmd() != null && !spec.getCmd().isEmpty()) {
                cmd.withCmd(spec.getCmd());
            }

            CreateContainerResponse resp = cmd.exec();
            String cid = resp.getId();
            dockerClient.startContainerCmd(cid).exec();

            log.info("靶机已拉起 name={} container={} port={}", spec.containerName(), cid.substring(0, 12), hostPort);

            return TargetInstance.builder()
                    .containerId(cid)
                    .containerName(spec.containerName())
                    .host("127.0.0.1")
                    .port(hostPort)
                    .ip(resolveIp(cid, networkName))
                    .status("RUNNING")
                    .simulated(false)
                    .build();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("靶机拉起失败 name={}", spec.containerName(), e);
            throw BizException.of("靶机拉起失败：" + e.getMessage());
        }
    }

    /** 重置靶机：销毁后重建（回到初始状态）。 */
    public TargetInstance reset(TargetSpec spec) {
        destroy(spec.containerName());
        return create(spec);
    }

    /** 销毁靶机。 */
    public boolean destroy(String containerName) {
        if (!dockerAvailable()) return true;
        try {
            List<Container> list = dockerClient.listContainersCmd()
                    .withShowAll(true).withNameFilter(List.of(containerName)).exec();
            for (Container c : list) {
                String name = c.getNames() == null || c.getNames().length == 0 ? "" : c.getNames()[0];
                if (!name.equals("/" + containerName)) continue;
                try { dockerClient.stopContainerCmd(c.getId()).withTimeout(10).exec(); } catch (Exception ignored) {}
                dockerClient.removeContainerCmd(c.getId()).withForce(true).exec();
                log.info("靶机已销毁 name={}", containerName);
            }
            return true;
        } catch (Exception e) {
            log.warn("靶机销毁失败 name={}: {}", containerName, e.getMessage());
            return false;
        }
    }

    private void removeByName(String name) { destroy(name); }

    /** 查询靶机运行状态。 */
    public String status(String containerName) {
        if (!dockerAvailable()) return "SIMULATED";
        try {
            List<Container> list = dockerClient.listContainersCmd()
                    .withShowAll(true).withNameFilter(List.of(containerName)).exec();
            for (Container c : list) {
                String n = c.getNames() == null || c.getNames().length == 0 ? "" : c.getNames()[0];
                if (n.equals("/" + containerName)) return c.getState();
            }
            return "NOT_FOUND";
        } catch (Exception e) {
            return "ERROR";
        }
    }

    /**
     * 读取容器资源使用（CPU%、内存MB）。
     * <p>docker-java 的 statsCmd 为异步命令，需通过回调同步等待一次采样结果。</p>
     */
    public double[] stats(String containerId) {
        if (!dockerAvailable() || containerId == null) return new double[]{0, 0};
        final java.util.concurrent.CompletableFuture<Statistics> future = new java.util.concurrent.CompletableFuture<>();
        try {
            dockerClient.statsCmd(containerId).withNoStream(true).exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<>() {
                @Override
                public void onNext(Statistics st) {
                    future.complete(st);
                }

                @Override
                public void onError(Throwable throwable) {
                    future.completeExceptionally(throwable);
                }

                @Override
                public void onComplete() {
                    future.complete(null);
                }
            });
            Statistics st = future.get(8, java.util.concurrent.TimeUnit.SECONDS);
            if (st == null || st.getMemoryStats() == null || st.getCpuStats() == null
                    || st.getCpuStats().getCpuUsage() == null) {
                return new double[]{0, 0};
            }
            double memMb = st.getMemoryStats().getUsage() == null ? 0 : st.getMemoryStats().getUsage() / 1024.0 / 1024.0;
            double cpuDelta = st.getCpuStats().getCpuUsage().getTotalUsage() == null ? 0
                    : st.getCpuStats().getCpuUsage().getTotalUsage();
            double sysDelta = st.getCpuStats().getSystemCpuUsage() == null ? 0 : st.getCpuStats().getSystemCpuUsage();
            double cpuPct = sysDelta > 0 ? cpuDelta / sysDelta * 100.0 : 0;
            return new double[]{cpuPct, memMb};
        } catch (Exception e) {
            log.debug("读取容器资源失败 {}: {}", containerId, e.getMessage());
            return new double[]{0, 0};
        } finally {
            try {
                dockerClient.statsCmd(containerId).withNoStream(true).close();
            } catch (Exception ignored) {
            }
        }
    }

    private String resolveIp(String containerId, String networkId) {
        try {
            InspectContainerResponse info = dockerClient.inspectContainerCmd(containerId).exec();
            NetworkSettings ns = info.getNetworkSettings();
            if (ns != null && ns.getNetworks() != null) {
                for (Map.Entry<String, ContainerNetwork> e : ns.getNetworks().entrySet()) {
                    if (e.getValue() != null && e.getValue().getIpAddress() != null) {
                        return e.getValue().getIpAddress();
                    }
                }
            }
            return "127.0.0.1";
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private long toCpuQuota(String cpu) {
        try {
            double v = cpu == null ? Double.parseDouble(props.getDefaultCpu()) : Double.parseDouble(cpu);
            return Math.max(1000L, (long) (v * 100000));
        } catch (Exception e) {
            return 100000L;
        }
    }

    private long toBytes(String mem) {
        if (mem == null || mem.isBlank()) mem = props.getDefaultMemory();
        String m = mem.trim().toLowerCase();
        try {
            if (m.endsWith("g")) return (long) (Double.parseDouble(m.substring(0, m.length() - 1)) * 1024 * 1024 * 1024);
            if (m.endsWith("m")) return (long) (Double.parseDouble(m.substring(0, m.length() - 1)) * 1024 * 1024);
            if (m.endsWith("k")) return (long) (Double.parseDouble(m.substring(0, m.length() - 1)) * 1024);
            return Long.parseLong(m);
        } catch (Exception e) {
            return 512L * 1024 * 1024;
        }
    }

    // ================================================================== AWDP 修补包应用

    /**
     * 在靶机容器内执行命令（同步等待退出，返回退出码与合并输出）。
     *
     * <p>用于 AWDP 修补包解压、打补丁、跑 check 脚本与健康检查。
     * 无 Docker 环境（模拟模式）时返回成功，保证赛制链路可测。</p>
     *
     * @param containerId 容器ID
     * @param cmd         命令数组，如 ["sh","-c","tar -xzf /tmp/p.tgz -C /app"]
     * @param timeoutSec  超时秒数
     */
    public ExecResult exec(String containerId, String[] cmd, int timeoutSec) {
        Objects.requireNonNull(containerId, "containerId");
        if (!dockerAvailable() || containerId.startsWith("sim-")) {
            log.info("[模拟模式] exec {} -> {}", containerId, String.join(" ", cmd));
            return new ExecResult(0, "[模拟模式] 已执行: " + String.join(" ", cmd));
        }
        try {
            com.github.dockerjava.api.command.ExecCreateCmdResponse created =
                    dockerClient.execCreateCmd(containerId)
                            .withCmd(cmd)
                            .withAttachStdout(true)
                            .withAttachStderr(true)
                            .exec();
            StringBuilder out = new StringBuilder();
            java.util.concurrent.CompletableFuture<Void> done = new java.util.concurrent.CompletableFuture<>();
            dockerClient.execStartCmd(created.getId()).exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<>() {
                @Override
                public void onNext(com.github.dockerjava.api.model.Frame frame) {
                    if (frame.getPayload() != null) out.append(new String(frame.getPayload()));
                }

                @Override
                public void onError(Throwable throwable) {
                    done.completeExceptionally(throwable);
                }

                @Override
                public void onComplete() {
                    done.complete(null);
                }
            });
            done.get(timeoutSec <= 0 ? 60 : timeoutSec, java.util.concurrent.TimeUnit.SECONDS);

            Long exit = null;
            try {
                exit = dockerClient.inspectExecCmd(created.getId()).exec().getExitCodeLong();
            } catch (Exception ignored) {
            }
            return new ExecResult(exit == null ? 0 : exit.intValue(), out.toString());
        } catch (Exception e) {
            log.warn("容器内执行失败 container={} cmd={}: {}", containerId, String.join(" ", cmd), e.getMessage());
            return new ExecResult(-1, "执行异常: " + e.getMessage());
        }
    }

    /**
     * 把本地文件（或目录打成的 tar 流）拷贝进容器。
     *
     * @param containerId  容器ID
     * @param localPath    本地文件/目录路径
     * @param remotePath   容器内目标目录（如 "/tmp" 表示拷到 /tmp/<文件名>）
     */
    public void copyIntoContainer(String containerId, java.io.File localPath, String remotePath) {
        Objects.requireNonNull(containerId, "containerId");
        Objects.requireNonNull(localPath, "localPath");
        if (!dockerAvailable() || containerId.startsWith("sim-")) {
            log.info("[模拟模式] copy {} -> {}{}", localPath.getName(), containerId, remotePath);
            return;
        }
        try {
            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withHostResource(localPath.getAbsolutePath())
                    .withRemotePath(remotePath)
                    .exec();
        } catch (Exception e) {
            throw BizException.of("修补包上传容器失败：" + e.getMessage());
        }
    }

    /** 重启容器（修补包应用后使改动生效）。 */
    public boolean restart(String containerId, int timeoutSec) {
        if (!dockerAvailable() || containerId == null || containerId.startsWith("sim-")) {
            log.info("[模拟模式] restart {}", containerId);
            return true;
        }
        try {
            dockerClient.restartContainerCmd(containerId)
                    .withTimeout(timeoutSec <= 0 ? 10 : timeoutSec).exec();
            return true;
        } catch (Exception e) {
            log.warn("容器重启失败 {}: {}", containerId, e.getMessage());
            return false;
        }
    }

    /** 容器内命令执行结果。 */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ExecResult {
        /** 退出码，0 表示成功；-1 表示执行异常 */
        private int exitCode;
        /** 合并的 stdout/stderr */
        private String output;
    }

    private TargetInstance simulate(TargetSpec spec, String status) {
        int port = allocatePort();
        log.info("[模拟模式] 靶机 {} 端口 {}", spec.containerName(), port);
        return TargetInstance.builder()
                .containerId("sim-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                .containerName(spec.containerName())
                .host("127.0.0.1")
                .port(port)
                .ip("172.20." + Math.floorMod(spec.getTeamId() == null ? 1 : spec.getTeamId().intValue(), 250) + ".10")
                .status(status)
                .simulated(true)
                .build();
    }
}
