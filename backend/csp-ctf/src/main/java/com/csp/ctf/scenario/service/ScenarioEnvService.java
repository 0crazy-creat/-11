package com.csp.ctf.scenario.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.entity.CtfQuestion;
import com.csp.ctf.entity.ContestQuestion;
import com.csp.ctf.mapper.ContestQuestionMapper;
import com.csp.ctf.mapper.CtfQuestionMapper;
import com.csp.ctf.scenario.entity.ScenarioEnv;
import com.csp.infra.docker.DockerProperties;
import com.github.dockerjava.api.DockerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 场景多机靶场环境控制：预检 / 拉起 / 销毁 / 依赖树。
 *
 * <p>预检（precheck）在开赛前一键执行，逐场景检查：
 * <ol>
 *   <li>编排模板能否解析（节点数、是否有唯一入口节点）</li>
 *   <li>每个节点镜像在本机是否存在（Docker 不可用时降级为 skipped）</li>
 *   <li>宿主端口池是否还有空闲端口（端口冲突检测）</li>
 *   <li>任务依赖图是否存在环（有环则赛事无法正常解锁）</li>
 * </ol>
 * 任一场景 error 即整体不通过，前端据此阻断开赛。</p>
 */
@Slf4j
@Service
public class ScenarioEnvService {

    private final ScenarioOrchestrator orchestrator;
    private final ScenarioDependencyGuard dependencyGuard;
    private final CtfQuestionMapper questionMapper;
    private final ContestQuestionMapper contestQuestionMapper;
    private final DockerClient dockerClient;
    private final DockerProperties dockerProperties;

    @Autowired
    public ScenarioEnvService(ScenarioOrchestrator orchestrator,
                              ScenarioDependencyGuard dependencyGuard,
                              CtfQuestionMapper questionMapper,
                              ContestQuestionMapper contestQuestionMapper,
                              @Autowired(required = false) DockerClient dockerClient,
                              DockerProperties dockerProperties) {
        this.orchestrator = orchestrator;
        this.dependencyGuard = dependencyGuard;
        this.questionMapper = questionMapper;
        this.contestQuestionMapper = contestQuestionMapper;
        this.dockerClient = dockerClient;
        this.dockerProperties = dockerProperties;
    }

    private static final String BANK_TYPE = "SCENARIO";

    // ================================================================== 预检

    /** 单条检查项。 */
    public record CheckItem(String scene, String code, String level, String message) {}

    /** 预检报告。 */
    public record PrecheckReport(Long contestId, boolean passed, boolean dockerAvailable,
                                 int scenarioCount, int errorCount, int warnCount,
                                 List<CheckItem> items) {}

    /** 整场赛事一键预检。 */
    public PrecheckReport precheckContest(Long contestId) {
        List<CtfQuestion> scenarios = scenarioQuestions(contestId);
        List<CheckItem> items = new ArrayList<>();
        boolean dockerOk = orchestrator.dockerAvailable();

        if (!dockerOk) {
            items.add(new CheckItem("-", "DOCKER", "WARN",
                    "Docker 不可用，环境将以模拟模式拉起（结构可测，容器不真实运行）"));
        }

        for (CtfQuestion sc : scenarios) {
            items.addAll(precheckOne(contestId, sc, dockerOk));
        }
        if (scenarios.isEmpty()) {
            items.add(new CheckItem("-", "NO_SCENARIO", "WARN", "该赛事未下发任何场景实操赛题目"));
        }

        // 端口池余量
        int start = dockerProperties.getPortStart();
        int end = dockerProperties.getPortEnd();
        int free = 0;
        for (int p = start; p < end && free < 50; p++) {
            if (!orchestrator.portUsed(p)) free++;
        }
        if (free < 20) {
            items.add(new CheckItem("-", "PORT_POOL", "ERROR",
                    "可用宿主端口不足（端口池 " + start + "-" + end + " 剩余 < 20），无法承载多机场景"));
        } else {
            items.add(new CheckItem("-", "PORT_POOL", "OK", "端口池可用（探测到 " + free + "+ 个空闲端口）"));
        }

        int errs = (int) items.stream().filter(i -> "ERROR".equals(i.level())).count();
        int warns = (int) items.stream().filter(i -> "WARN".equals(i.level())).count();
        return new PrecheckReport(contestId, errs == 0, dockerOk,
                scenarios.size(), errs, warns, items);
    }

    /** 单场景预检。 */
    public PrecheckReport precheckScenario(Long contestId, Long scenarioId) {
        CtfQuestion sc = requireScenario(contestId, scenarioId);
        boolean dockerOk = orchestrator.dockerAvailable();
        List<CheckItem> items = new ArrayList<>(precheckOne(contestId, sc, dockerOk));
        int errs = (int) items.stream().filter(i -> "ERROR".equals(i.level())).count();
        int warns = (int) items.stream().filter(i -> "WARN".equals(i.level())).count();
        return new PrecheckReport(contestId, errs == 0, dockerOk, 1, errs, warns, items);
    }

    private List<CheckItem> precheckOne(Long contestId, CtfQuestion sc, boolean dockerOk) {
        List<CheckItem> items = new ArrayList<>();
        String name = sc.getTitle() == null ? ("#" + sc.getId()) : sc.getTitle();
        String tpl = templateOf(sc);
        if (tpl == null || tpl.isBlank()) {
            items.add(new CheckItem(name, "TEMPLATE", "ERROR", "场景未配置 compose 编排模板"));
            return items;
        }
        List<ScenarioOrchestrator.NodeSpec> nodes;
        try {
            nodes = orchestrator.parseTemplate(tpl, orchestrator.entryNodeOf(tpl));
        } catch (Exception e) {
            items.add(new CheckItem(name, "TEMPLATE", "ERROR", "编排模板解析失败：" + e.getMessage()));
            return items;
        }
        long exposed = nodes.stream().filter(ScenarioOrchestrator.NodeSpec::isExposed).count();
        if (exposed != 1) {
            items.add(new CheckItem(name, "ENTRY", "ERROR",
                    "必须有且仅有一个对外入口节点，实际 " + exposed + " 个"));
        } else {
            items.add(new CheckItem(name, "ENTRY", "OK",
                    "入口节点唯一：" + nodes.stream().filter(ScenarioOrchestrator.NodeSpec::isExposed)
                            .findFirst().map(ScenarioOrchestrator.NodeSpec::getName).orElse("-")));
        }
        if (nodes.size() < 2) {
            items.add(new CheckItem(name, "TOPOLOGY", "WARN",
                    "该场景仅 " + nodes.size() + " 个节点，未构成多机内网拓扑"));
        } else {
            items.add(new CheckItem(name, "TOPOLOGY", "OK",
                    "多机拓扑 " + nodes.size() + " 个节点（" + (nodes.size() - 1) + " 个内网节点互通，仅入口暴露）"));
        }

        // 镜像存在性
        for (ScenarioOrchestrator.NodeSpec n : nodes) {
            items.add(checkImage(name, n, dockerOk));
        }

        // 任务依赖环检测
        try {
            List<Long> cycle = dependencyGuard.detectCycle(contestId, sc.getId());
            if (cycle != null && !cycle.isEmpty()) {
                items.add(new CheckItem(name, "DEPENDENCY", "ERROR", "任务依赖存在环：" + cycle));
            } else {
                int taskCount = dependencyGuard.scenarioTasks(sc.getId()).size();
                List<Long> order = dependencyGuard.topologicalOrder(contestId, sc.getId());
                items.add(new CheckItem(name, "DEPENDENCY", "OK",
                        "任务依赖无环，共 " + taskCount + " 个任务，解锁顺序 " + order));
            }
        } catch (Exception e) {
            items.add(new CheckItem(name, "DEPENDENCY", "WARN", "依赖检查失败：" + e.getMessage()));
        }
        return items;
    }

    private CheckItem checkImage(String scene, ScenarioOrchestrator.NodeSpec n, boolean dockerOk) {
        String image = n.getImage();
        if (image == null || image.isBlank()) {
            return new CheckItem(scene, "IMAGE:" + n.getName(), "ERROR", "节点 " + n.getName() + " 未配置镜像");
        }
        if (!dockerOk) {
            return new CheckItem(scene, "IMAGE:" + n.getName(), "WARN",
                    "镜像 " + image + " 存在性未校验（Docker 不可用）");
        }
        try {
            dockerClient.inspectImageCmd(image).exec();
            return new CheckItem(scene, "IMAGE:" + n.getName(), "OK", "镜像 " + image + " 已存在");
        } catch (Exception e) {
            return new CheckItem(scene, "IMAGE:" + n.getName(), "ERROR",
                    "镜像 " + image + " 不存在（需 docker pull " + image + "）");
        }
    }

    // ================================================================== 拉起 / 销毁 / 查询

    /** 拉起某队某场景的多机环境。 */
    public ScenarioEnv provision(Long contestId, Long teamId, Long scenarioId) {
        CtfQuestion sc = requireScenario(contestId, scenarioId);
        String tpl = templateOf(sc);
        if (tpl == null || tpl.isBlank()) {
            throw BizException.of("该场景未配置多机编排模板");
        }
        return orchestrator.provision(contestId, teamId, scenarioId, tpl, orchestrator.entryNodeOf(tpl));
    }

    public boolean destroy(Long contestId, Long teamId, Long scenarioId) {
        return orchestrator.destroy(contestId, teamId, scenarioId);
    }

    public ScenarioEnv findEnv(Long contestId, Long teamId, Long scenarioId) {
        return orchestrator.findEnv(contestId, teamId, scenarioId);
    }

    public List<ScenarioEnv> listEnvs(Long contestId, Long teamId) {
        return orchestrator.listEnvs(contestId, teamId);
    }

    // ================================================================== 依赖树

    /** 场景任务依赖树（供前端任务树页渲染解锁关系）。 */
    public Map<String, Object> dependencyTree(Long contestId, Long scenarioId, Long teamId) {
        requireScenario(contestId, scenarioId);
        Map<Long, List<Long>> graph = dependencyGuard.buildGraph(contestId, scenarioId);
        List<Long> order = dependencyGuard.topologicalOrder(contestId, scenarioId);
        Set<Long> solved = teamId == null ? Set.of() : dependencyGuard.solvedTaskIds(contestId, teamId);

        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Long id : graph.keySet()) {
            CtfQuestion q = questionMapper.selectById(id);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("taskId", id);
            m.put("title", q == null ? String.valueOf(id) : q.getTitle());
            m.put("score", q == null ? null : q.getScore());
            m.put("preTaskIds", preOf(contestId, id));
            m.put("solved", solved.contains(id));
            m.put("unlockMode", dependencyGuard.unlockMode(contestId, id));
            nodes.add(m);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("contestId", contestId);
        out.put("scenarioId", scenarioId);
        out.put("hasCycle", !dependencyGuard.detectCycle(contestId, scenarioId).isEmpty());
        out.put("topologicalOrder", order);
        out.put("nodes", nodes);
        return out;
    }

    /** 某任务的前置任务校验（提交前可先问一次）。 */
    public Map<String, Object> checkDependency(Long contestId, Long teamId, Long taskId) {
        List<Long> pres = dependencyGuard.preTaskIds(contestId, taskId);
        Set<Long> solved = dependencyGuard.solvedTaskIds(contestId, teamId);
        boolean ok = true;
        try {
            dependencyGuard.requirePreTasksDone(contestId, teamId, taskId);
        } catch (BizException e) {
            ok = false;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("taskId", taskId);
        out.put("preTaskIds", pres);
        out.put("unlockMode", dependencyGuard.unlockMode(contestId, taskId));
        out.put("solvedTaskIds", solved);
        out.put("canSubmit", ok);
        return out;
    }

    // ================================================================== 内部

    private List<Long> preOf(Long contestId, Long taskId) {
        return dependencyGuard.preTaskIds(contestId, taskId);
    }

    /** 从题目 ext 读 compose 模板（ext.composeTpl / ext.compose）。 */
    private String templateOf(CtfQuestion q) {
        String ext = q.getExt();
        if (ext == null || ext.isBlank()) return null;
        try {
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(ext);
            for (String k : new String[]{"composeTpl", "compose", "template", "orchestration"}) {
                var v = node.get(k);
                if (v != null && !v.isNull()) {
                    return v.isTextual() ? v.asText() : v.toString();
                }
            }
        } catch (Exception ignored) {
            // 模板不是 JSON ext，忽略
        }
        return null;
    }

    private List<CtfQuestion> scenarioQuestions(Long contestId) {
        List<ContestQuestion> bindings = contestQuestionMapper.selectList(
                new LambdaQueryWrapper<ContestQuestion>()
                        .eq(ContestQuestion::getContestId, contestId)
                        .eq(ContestQuestion::getVisible, 1));
        if (bindings.isEmpty()) return List.of();
        List<Long> ids = bindings.stream().map(ContestQuestion::getQuestionId).toList();
        return questionMapper.selectList(new LambdaQueryWrapper<CtfQuestion>()
                .in(CtfQuestion::getId, ids)
                .eq(CtfQuestion::getBankType, BANK_TYPE)
                .eq(CtfQuestion::getStatus, 1));
    }

    private CtfQuestion requireScenario(Long contestId, Long scenarioId) {
        CtfQuestion q = questionMapper.selectById(scenarioId);
        if (q == null || !BANK_TYPE.equals(q.getBankType())) {
            throw new BizException(ResultCode.NOT_FOUND, "场景题 " + scenarioId + " 不存在");
        }
        return q;
    }
}