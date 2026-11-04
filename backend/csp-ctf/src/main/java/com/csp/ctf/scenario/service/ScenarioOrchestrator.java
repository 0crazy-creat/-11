package com.csp.ctf.scenario.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.exception.BizException;
import com.csp.ctf.scenario.entity.ScenarioEnv;
import com.csp.ctf.scenario.mapper.ScenarioEnvMapper;
import com.csp.infra.docker.DockerProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.dockerjava.api.model.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 场景多机靶场编排器。
 *
 * <p><b>多机拓扑</b>：一个场景 = 一个 Docker 自定义 bridge 网络 + N 个容器
 * （外网 Web / 内网 DB / 跳板机）。内网容器之间通过「服务名」互相解析，
 * <b>只有入口节点</b>（entry）绑定宿主端口对外暴露，其余节点仅内网可达。</p>
 *
 * <p><b>为什么不用 TargetScheduler</b>：{@code TargetScheduler.create} 是「单容器 + 独立端口池 +
 * 独占网络」模型，每个容器各自 ensureNetwork，无法让多容器共享同一网络实现内网互通。
 * 本编排器复用同一个 {@link DockerClient} Bean（不自建第二套客户端），
 * 自行创建共享网络并按节点名设置网络别名。</p>
 *
 * <p><b>降级</b>：Docker 不可用时进入模拟模式，返回结构完整但 {@code simulated=true}
 * 的节点清单，保证业务链路（提交/判分/依赖）可端到端测试。</p>
 */
@Slf4j
@Service
public class ScenarioOrchestrator {

    private final DockerClient dockerClient;
    private final DockerProperties dockerProperties;
    private final ScenarioEnvMapper envMapper;
    private final ObjectMapper objectMapper;

    private final AtomicInteger portCursor = new AtomicInteger();
    private volatile Boolean dockerReady;

    @Autowired
    public ScenarioOrchestrator(@Autowired(required = false) DockerClient dockerClient,
                                DockerProperties dockerProperties,
                                ScenarioEnvMapper envMapper,
                                ObjectMapper objectMapper) {
        this.dockerClient = dockerClient;
        this.dockerProperties = dockerProperties;
        this.envMapper = envMapper;
        this.objectMapper = objectMapper;
        if (dockerClient == null) {
            this.dockerReady = false;
            log.info("ScenarioOrchestrator: Docker 未启用，使用模拟模式");
        }
    }

    /** 编排模板中的一个节点。 */
    @Data
    public static class NodeSpec {
        /** 节点名（即容器内服务名，内网互通靠它） */
        private String name;
        /** 镜像 */
        private String image;
        /** 容器内监听端口 */
        private int port = 80;
        /** 是否对外暴露（仅入口节点为 true） */
        private boolean exposed;
        /** 环境变量 */
        private Map<String, String> env = new LinkedHashMap<>();
        /** 启动命令 */
        private List<String> cmd;
        /** CPU 限额 */
        private String cpuLimit;
        /** 内存限额 */
        private String memoryLimit;
    }

    /** 编排结果。 */
    @Data
    public static class OrchestrationResult {
        private String networkName;
        private String entryNode;
        private String entryHost;
        private int entryPort;
        private int nodeCount;
        private List<Map<String, Object>> nodes = new ArrayList<>();
        private boolean simulated;
        private String status;
    }

    // ================================================================== 公共能力

    /** Docker 是否可用（带缓存）。 */
    public boolean dockerAvailable() {
        if (dockerReady != null) {
            return dockerReady;
        }
        synchronized (this) {
            if (dockerReady == null) {
                try {
                    dockerClient.pingCmd().exec();
                    dockerReady = true;
                    log.info("ScenarioOrchestrator: Docker 可用，多机编排已启用");
                } catch (Exception e) {
                    dockerReady = false;
                    log.warn("ScenarioOrchestrator: Docker 不可用，降级为模拟模式: {}", e.getMessage());
                }
            }
            return dockerReady;
        }
    }

    /** 分配一个空闲宿主端口（优先避开 docker 端口池区间外）。 */
    private synchronized int allocatePort() {
        int start = Math.max(1, dockerProperties.getPortStart());
        int end = Math.max(start + 1, dockerProperties.getPortEnd());
        int span = end - start;
        for (int i = 0; i < span; i++) {
            int p = start + Math.floorMod(portCursor.getAndIncrement(), span);
            if (!isPortUsed(p)) {
                return p;
            }
        }
        throw BizException.of("无可用宿主端口（端口池 " + start + "-" + end + " 已耗尽）");
    }

    private boolean isPortUsed(int port) {
        try (java.net.ServerSocket ignored = new java.net.ServerSocket(port)) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /** 判断某宿主端口当前是否被占用（预检用）。 */
    public boolean portUsed(int port) {
        return isPortUsed(port);
    }

    // ================================================================== 模板解析

    /**
     * 解析编排模板。支持两种格式：
     * <ul>
     *   <li>JSON：{@code {"entry":"web","nodes":[{"name":"web","image":"nginx","port":80,"exposed":true,...}]}}</li>
     *   <li>简化 YAML：按 {@code nodes:} 与缩进 {@code - name: xx} 逐行解析（不引入 YAML 依赖）</li>
     * </ul>
     *
     * @throws BizException 模板格式错误
     */
    public List<NodeSpec> parseTemplate(String tpl, String entryNode) {
        if (tpl == null || tpl.isBlank()) {
            throw BizException.of("编排模板不能为空");
        }
        String trimmed = tpl.trim();
        List<NodeSpec> nodes;
        if (trimmed.startsWith("{")) {
            nodes = parseJson(trimmed);
        } else {
            nodes = parseSimpleYaml(trimmed);
        }
        if (nodes.isEmpty()) {
            throw BizException.of("编排模板解析后没有任何节点");
        }

        // 确定入口节点：优先显式 entryNode，其次模板中第一个 exposed 节点，最后第一个节点
        String entry = entryNode;
        if (entry == null || entry.isBlank()) {
            entry = nodes.stream().filter(NodeSpec::isExposed).map(NodeSpec::getName)
                    .findFirst().orElse(nodes.get(0).getName());
        }
        boolean found = false;
        for (NodeSpec n : nodes) {
            if (n.getName() == null || n.getName().isBlank()) {
                throw BizException.of("编排模板存在无名节点");
            }
            if (n.getImage() == null || n.getImage().isBlank()) {
                throw BizException.of("节点 " + n.getName() + " 缺少镜像");
            }
            if (n.getName().equals(entry)) {
                n.setExposed(true);
                found = true;
            } else {
                // 非入口节点一律不对外暴露
                n.setExposed(false);
            }
        }
        if (!found) {
            throw BizException.of("入口节点 " + entry + " 不在模板节点列表中");
        }
        return nodes;
    }

    /** 提取模板声明的入口节点名。 */
    public String entryNodeOf(String tpl) {
        try {
            JsonNode root = objectMapper.readTree(tpl);
            JsonNode e = root.get("entry");
            return e == null || e.isNull() ? null : e.asText();
        } catch (Exception e) {
            return null;
        }
    }

    private List<NodeSpec> parseJson(String tpl) {
        List<NodeSpec> out = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(tpl);
            JsonNode arr = root.get("nodes");
            if (arr == null || !arr.isArray()) {
                throw BizException.of("编排模板缺少 nodes 数组");
            }
            for (JsonNode n : arr) {
                NodeSpec spec = new NodeSpec();
                spec.setName(text(n, "name"));
                spec.setImage(text(n, "image"));
                spec.setPort(n.has("port") ? n.get("port").asInt(80) : 80);
                spec.setExposed(n.has("exposed") && n.get("exposed").asBoolean(false));
                spec.setCpuLimit(text(n, "cpuLimit"));
                spec.setMemoryLimit(text(n, "memoryLimit"));
                if (n.has("env") && n.get("env").isObject()) {
                    Map<String, String> env = new LinkedHashMap<>();
                    n.get("env").fields().forEachRemaining(e -> env.put(e.getKey(), e.getValue().asText()));
                    spec.setEnv(env);
                }
                if (n.has("cmd") && n.get("cmd").isArray()) {
                    List<String> cmd = new ArrayList<>();
                    n.get("cmd").forEach(c -> cmd.add(c.asText()));
                    spec.setCmd(cmd);
                }
                out.add(spec);
            }
            return out;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw BizException.of("编排模板 JSON 解析失败：" + e.getMessage());
        }
    }

    /**
     * 极简 YAML 解析：只支持本场景所需的扁平结构。
     * <pre>
     * entry: web
     * nodes:
     *   - name: web
     *     image: nginx:alpine
     *     port: 80
     *     exposed: true
     *   - name: db
     *     image: mysql:8
     *     port: 3306
     * </pre>
     */
    private List<NodeSpec> parseSimpleYaml(String tpl) {
        List<NodeSpec> out = new ArrayList<>();
        NodeSpec cur = null;
        for (String raw : tpl.split("\n")) {
            String line = raw.replace("\r", "");
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("#") || t.equals("nodes:") || t.startsWith("entry:")) {
                continue;
            }
            if (t.startsWith("- ")) {
                cur = new NodeSpec();
                out.add(cur);
                t = t.substring(2).trim();
            }
            if (cur == null || !t.contains(":")) {
                continue;
            }
            int idx = t.indexOf(':');
            String k = t.substring(0, idx).trim();
            String v = t.substring(idx + 1).trim();
            if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                v = v.substring(1, v.length() - 1);
            }
            switch (k) {
                case "name" -> cur.setName(v);
                case "image" -> cur.setImage(v);
                case "port" -> cur.setPort(parseInt(v, 80));
                case "exposed" -> cur.setExposed(Boolean.parseBoolean(v));
                case "cpuLimit", "cpu" -> cur.setCpuLimit(v);
                case "memoryLimit", "memory" -> cur.setMemoryLimit(v);
                default -> { /* 忽略未知键，保持宽松 */ }
            }
        }
        return out;
    }

    private int parseInt(String v, int def) {
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }

    private String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    // ================================================================== 拉起 / 销毁

    /**
     * 拉起一个场景的多机环境。
     *
     * <p>幂等：同 (contestId, teamId, scenarioId) 已存在 RUNNING 环境则直接复用。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public ScenarioEnv provision(Long contestId, Long teamId, Long scenarioId,
                                 String composeTpl, String entryNode) {
        // 幂等复用
        ScenarioEnv exist = findEnv(contestId, teamId, scenarioId);
        if (exist != null && "RUNNING".equals(exist.getStatus())) {
            log.info("场景环境已存在，复用 contest={} team={} scenario={}", contestId, teamId, scenarioId);
            return exist;
        }

        List<NodeSpec> nodes = parseTemplate(composeTpl, entryNode);
        String entry = nodes.stream().filter(NodeSpec::isExposed).map(NodeSpec::getName)
                .findFirst().orElseThrow(() -> BizException.of("模板无入口节点"));

        ScenarioEnv env = exist == null ? new ScenarioEnv() : exist;
        env.setContestId(contestId);
        env.setTeamId(teamId);
        env.setScenarioId(scenarioId);
        env.setComposeTpl(composeTpl);
        env.setEntryNode(entry);
        env.setStatus("CREATING");
        env.setDeleted(0);
        // network_name 为 NOT NULL，先落占位值，编排成功后由 result 覆盖
        if (env.getNetworkName() == null || env.getNetworkName().isBlank()) {
            env.setNetworkName(networkName(contestId, teamId, scenarioId));
        }
        if (env.getNodeCount() == null) env.setNodeCount(0);
        LocalDateTime now = LocalDateTime.now();
        if (env.getCreateTime() == null) env.setCreateTime(now);
        env.setUpdateTime(now);
        if (exist == null) {
            envMapper.insert(env);
        } else {
            envMapper.updateById(env);
        }

        OrchestrationResult result;
        try {
            result = doProvision(contestId, teamId, scenarioId, nodes, entry);
            env.setStatus("RUNNING");
            env.setFailReason(null);
        } catch (Exception e) {
            log.error("场景多机编排失败 contest={} team={} scenario={}", contestId, teamId, scenarioId, e);
            env.setStatus("FAILED");
            env.setFailReason(truncate(e.getMessage(), 500));
            env.setUpdateTime(LocalDateTime.now());
            envMapper.updateById(env);
            throw BizException.of("场景环境拉起失败：" + e.getMessage());
        }

        env.setNetworkName(result.getNetworkName());
        env.setEntryNode(result.getEntryNode());
        env.setEntryHost(result.getEntryHost());
        env.setEntryPort(result.getEntryPort());
        env.setNodeCount(result.getNodeCount());
        env.setDetail(toJson(result.getNodes()));
        env.setUpdateTime(LocalDateTime.now());
        envMapper.updateById(env);

        log.info("场景多机环境就绪 contest={} team={} scenario={} network={} nodes={} entry={}:{}",
                contestId, teamId, scenarioId, result.getNetworkName(), result.getNodeCount(),
                result.getEntryHost(), result.getEntryPort());
        return env;
    }

    private OrchestrationResult doProvision(Long contestId, Long teamId, Long scenarioId,
                                            List<NodeSpec> nodes, String entry) {
        OrchestrationResult r = new OrchestrationResult();
        r.setEntryNode(entry);
        r.setNodeCount(nodes.size());

        String networkName = networkName(contestId, teamId, scenarioId);
        r.setNetworkName(networkName);

        if (!dockerAvailable()) {
            // 模拟模式：给出结构完整的假实例，保证业务链路可测
            r.setSimulated(true);
            r.setStatus("RUNNING");
            r.setEntryHost("127.0.0.1");
            r.setEntryPort(allocatePort());
            for (NodeSpec n : nodes) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", n.getName());
                m.put("image", n.getImage());
                m.put("internal", !n.getName().equals(entry));
                m.put("ip", "172.28.0." + (nodes.indexOf(n) + 2));
                m.put("simulated", true);
                r.getNodes().add(m);
            }
            log.info("场景多机编排走模拟模式 network={} nodes={}", networkName, nodes.size());
            return r;
        }

        // 1) 创建自定义 bridge 网络（若已存在则复用）
        ensureNetwork(networkName);

        // 2) 逐节点拉起容器：全部加入同一网络，仅入口绑定宿主端口
        int entryPort = 0;
        for (NodeSpec n : nodes) {
            boolean isEntry = n.getName().equals(entry);
            int hostPort = 0;
            if (isEntry) {
                hostPort = allocatePort();
                entryPort = hostPort;
            }

            String containerName = containerName(contestId, teamId, scenarioId, n.getName());
            removeIfExists(containerName);

            List<ExposedPort> exposed = List.of(ExposedPort.tcp(n.getPort()));
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withNetworkMode(networkName)
                    .withCpuQuota(toCpuQuota(n.getCpuLimit()))
                    .withMemory(toBytes(n.getMemoryLimit()))
                    .withRestartPolicy(RestartPolicy.unlessStoppedRestart())
                    .withAutoRemove(false);

            if (isEntry) {
                // 仅入口节点做端口映射，内网节点不暴露
                Ports bindings = new Ports();
                bindings.bind(exposed.get(0), Ports.Binding.bindPort(hostPort));
                hostConfig.withPortBindings(bindings);
            }

            List<String> envList = new ArrayList<>();
            if (n.getEnv() != null) {
                n.getEnv().forEach((k, v) -> envList.add(k + "=" + v));
            }
            // 镜像必需默认环境变量兜底（如 mysql 缺 MYSQL_ROOT_PASSWORD 会无限 restart）
            applyImageRequiredEnv(n.getImage(), envList);
            // 把同网络兄弟节点的服务名注入环境变量，便于应用内互相寻址
            envList.add("SCENARIO_NETWORK=" + networkName);
            envList.add("SCENARIO_NODE=" + n.getName());

            CreateContainerCmd cmd = dockerClient.createContainerCmd(n.getImage())
                    .withName(containerName)
                    .withEnv(envList)
                    .withExposedPorts(exposed)
                    .withHostConfig(hostConfig)
                    // 关键：网络别名 = 节点名，内网其它容器可用该名字解析
                    .withAliases(n.getName());

            if (n.getCmd() != null && !n.getCmd().isEmpty()) {
                cmd.withCmd(n.getCmd());
            } else {
                // 未显式给命令时注入保活命令，避免 alpine 等无前台进程镜像
                // 启动后立即退出导致容器反复 Restarting、内网服务名无法解析
                List<String> keepAlive = keepAliveCmd(n.getImage());
                if (keepAlive != null) {
                    cmd.withCmd(keepAlive);
                }
            }

            CreateContainerResponse resp = cmd.exec();
            String cid = resp.getId();
            dockerClient.startContainerCmd(cid).exec();

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", n.getName());
            m.put("container", cid.substring(0, Math.min(12, cid.length())));
            m.put("image", n.getImage());
            m.put("internal", !isEntry);
            m.put("ip", resolveIp(cid, networkName));
            m.put("hostPort", isEntry ? hostPort : null);
            m.put("simulated", false);
            r.getNodes().add(m);

            log.info("场景节点已拉起 name={} container={} internal={} hostPort={}",
                    n.getName(), cid.substring(0, 12), !isEntry, isEntry ? hostPort : null);
        }

        r.setSimulated(false);
        r.setStatus("RUNNING");
        r.setEntryHost("127.0.0.1");
        r.setEntryPort(entryPort);
        return r;
    }

    /** 销毁场景环境（容器 + 网络）。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean destroy(Long contestId, Long teamId, Long scenarioId) {
        ScenarioEnv env = findEnv(contestId, teamId, scenarioId);
        if (env == null) {
            return false;
        }
        if (dockerAvailable()) {
            try {
                List<com.github.dockerjava.api.command.InspectContainerResponse> list =
                        dockerClient.listContainersCmd().withShowAll(true).exec().stream()
                                .filter(c -> c.getNames() != null && Arrays.stream(c.getNames())
                                        .anyMatch(nm -> nm.contains("csp-sc-" + contestId + "-" + teamId + "-" + scenarioId)))
                                .map(c -> dockerClient.inspectContainerCmd(c.getId()).exec())
                                .toList();
                for (var insp : list) {
                    dockerClient.stopContainerCmd(insp.getId()).exec();
                    dockerClient.removeContainerCmd(insp.getId()).withForce(true).exec();
                }
                if (env.getNetworkName() != null) {
                    try {
                        dockerClient.removeNetworkCmd(env.getNetworkName()).exec();
                    } catch (Exception ignored) {
                        // 网络可能已被清理
                    }
                }
            } catch (Exception e) {
                log.warn("销毁场景环境异常（忽略）: {}", e.getMessage());
            }
        }
        env.setStatus("DESTROYED");
        env.setUpdateTime(LocalDateTime.now());
        envMapper.updateById(env);
        return true;
    }

    // ================================================================== 查询

    /** 查询某队某场景的环境实例。 */
    public ScenarioEnv findEnv(Long contestId, Long teamId, Long scenarioId) {
        return envMapper.selectOne(new LambdaQueryWrapper<ScenarioEnv>()
                .eq(ScenarioEnv::getContestId, contestId)
                .eq(ScenarioEnv::getTeamId, teamId)
                .eq(ScenarioEnv::getScenarioId, scenarioId)
                .eq(ScenarioEnv::getDeleted, 0)
                .last("LIMIT 1"));
    }

    /** 某队在某赛事下的全部场景环境。 */
    public List<ScenarioEnv> listEnvs(Long contestId, Long teamId) {
        return envMapper.selectList(new LambdaQueryWrapper<ScenarioEnv>()
                .eq(ScenarioEnv::getContestId, contestId)
                .eq(ScenarioEnv::getTeamId, teamId)
                .eq(ScenarioEnv::getDeleted, 0)
                .orderByDesc(ScenarioEnv::getCreateTime));
    }

    // ================================================================== 内部工具

    public String networkName(Long contestId, Long teamId, Long scenarioId) {
        return dockerProperties.getNetworkPrefix() + "sc-" + contestId + "-" + teamId + "-" + scenarioId;
    }

    /**
     * 为已知镜像注入必需默认环境变量（模板未显式给出时兜底）。
     * 场景模板只描述拓扑，不应要求出题人重复写镜像引导参数。
     */
    private void applyImageRequiredEnv(String image, List<String> envList) {
        if (image == null) return;
        String img = image.toLowerCase();
        java.util.function.BiConsumer<String, String> put = (k, v) -> {
            boolean exists = envList.stream().anyMatch(e -> e.startsWith(k + "="));
            if (!exists) envList.add(k + "=" + v);
        };
        if (img.startsWith("mysql") || img.startsWith("mariadb") || img.startsWith("percona")) {
            put.accept("MYSQL_ROOT_PASSWORD", "scenario_root");
            if (img.startsWith("mysql")) put.accept("MYSQL_DATABASE", "scenario");
        } else if (img.startsWith("postgres")) {
            put.accept("POSTGRES_PASSWORD", "scenario_root");
        } else if (img.startsWith("rabbitmq")) {
            put.accept("RABBITMQ_DEFAULT_USER", "scenario");
            put.accept("RABBITMQ_DEFAULT_PASS", "scenario_root");
        }
    }

    /**
     * 对无前台常驻进程的镜像返回保活命令；有自带入口点的镜像返回 null 保持默认。
     * 目前覆盖 alpine/busybox 这类常被当作跳板机的精简镜像。
     */
    private List<String> keepAliveCmd(String image) {
        if (image == null) return null;
        String img = image.toLowerCase();
        if (img.startsWith("alpine") || img.contains("alpine") || img.startsWith("busybox")) {
            return java.util.List.of("sh", "-c", "while true; do sleep 3600; done");
        }
        return null;
    }

    public String containerName(Long contestId, Long teamId, Long scenarioId, String node) {
        // Docker 容器名只允许 [a-zA-Z0-9][a-zA-Z0-9_.-]
        String safe = node == null ? "node" : node.replaceAll("[^a-zA-Z0-9_.-]", "-");
        return "csp-sc-" + contestId + "-" + teamId + "-" + scenarioId + "-" + safe;
    }

    private void ensureNetwork(String name) {
        try {
            dockerClient.inspectNetworkCmd().withNetworkId(name).exec();
            return; // 已存在
        } catch (Exception ignored) {
            // 不存在，创建
        }
        dockerClient.createNetworkCmd()
                .withName(name)
                .withDriver("bridge")
                .exec();
        log.info("已创建场景内网 bridge 网络: {}", name);
    }

    private void removeIfExists(String containerName) {
        try {
            dockerClient.removeContainerCmd(containerName).withForce(true).exec();
        } catch (Exception ignored) {
            // 不存在则忽略
        }
    }

    private String resolveIp(String containerId, String networkName) {
        try {
            var insp = dockerClient.inspectContainerCmd(containerId).exec();
            var networks = insp.getNetworkSettings().getNetworks();
            if (networks != null && networks.containsKey(networkName)) {
                return networks.get(networkName).getIpAddress();
            }
        } catch (Exception e) {
            log.debug("解析容器 IP 失败: {}", e.getMessage());
        }
        return null;
    }

    private Long toCpuQuota(String limit) {
        if (limit == null || limit.isBlank()) {
            return null;
        }
        try {
            return (long) (Double.parseDouble(limit) * 100_000);
        } catch (Exception e) {
            return null;
        }
    }

    private Long toBytes(String mem) {
        if (mem == null || mem.isBlank()) {
            return null;
        }
        String s = mem.trim().toLowerCase();
        try {
            long mul = 1;
            if (s.endsWith("g")) {
                mul = 1024L * 1024 * 1024;
                s = s.substring(0, s.length() - 1);
            } else if (s.endsWith("m")) {
                mul = 1024L * 1024;
                s = s.substring(0, s.length() - 1);
            } else if (s.endsWith("k")) {
                mul = 1024L;
                s = s.substring(0, s.length() - 1);
            }
            return Long.parseLong(s) * mul;
        } catch (Exception e) {
            return null;
        }
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}