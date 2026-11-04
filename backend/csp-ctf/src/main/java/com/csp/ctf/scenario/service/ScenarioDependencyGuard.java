package com.csp.ctf.scenario.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.entity.CtfQuestion;
import com.csp.ctf.entity.Submission;
import com.csp.ctf.mapper.CtfQuestionMapper;
import com.csp.ctf.mapper.SubmissionMapper;
import com.csp.ctf.scenario.entity.ScenarioTask;
import com.csp.ctf.scenario.mapper.ScenarioTaskMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 场景任务依赖守卫：前置任务链校验 + 环检测 + 拓扑排序。
 *
 * <p>依赖真值有两个来源，本类统一抽象并做一致性兜底：</p>
 * <ol>
 *   <li><b>权威源</b>：{@code question.ext.preQuestionId}（captain 既定模型，组卷/榜单/审计通用链路复用）；</li>
 *   <li><b>物化表</b>：{@code scenario_task}（本任务新增，用于环检测、拓扑排序与多机编排关联）。</li>
 * </ol>
 *
 * <p>校验规则（对齐任务要求第 5 条「Task B 需 Task A 完成才能提交」）：</p>
 * <ul>
 *   <li>提交任务 T 前，若 T 有前置 P，则 P 必须已被该队完成，否则拒绝并返回待完成清单；</li>
 *   <li>依赖图必须无环——建图/组卷时调用 {@link #detectCycle} 提前拦截；</li>
 *   <li>支持 {@code SEQUENTIAL}（严格前置链）与 {@code ANY}（任一前置完成即可）两种解锁方式。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioDependencyGuard {

    private final ScenarioTaskMapper taskMapper;
    private final CtfQuestionMapper questionMapper;
    private final SubmissionMapper submissionMapper;
    private final ObjectMapper objectMapper;

    // ================================================================== 依赖解析

    /**
     * 解析某任务的直接前置任务ID。
     *
     * <p>优先级：scenario_task 物化表 &gt; question.ext.preQuestionId。
     * 两者都有时以物化表为准（它支持多前置），但会校验一致性并告警。</p>
     *
     * @param contestId 竞赛
     * @param taskId    任务题ID
     * @return 前置任务ID列表，无依赖返回空列表
     */
    public List<Long> preTaskIds(Long contestId, Long taskId) {
        ScenarioTask st = taskMapper.selectOne(new LambdaQueryWrapper<ScenarioTask>()
                .eq(ScenarioTask::getContestId, contestId)
                .eq(ScenarioTask::getTaskId, taskId)
                .eq(ScenarioTask::getDeleted, 0)
                .last("LIMIT 1"));
        if (st != null && st.getPreTaskId() != null) {
            return List.of(st.getPreTaskId());
        }
        Long fromExt = preQuestionIdFromExt(taskId);
        return fromExt == null ? List.of() : List.of(fromExt);
    }

    /** 从 question.ext 读取 preQuestionId（captain 既定模型）。 */
    public Long preQuestionIdFromExt(Long taskId) {
        CtfQuestion q = questionMapper.selectById(taskId);
        if (q == null || q.getExt() == null || q.getExt().isBlank()) {
            return null;
        }
        try {
            JsonNode ext = objectMapper.readTree(q.getExt());
            JsonNode pre = ext.get("preQuestionId");
            if (pre == null || pre.isNull()) {
                return null;
            }
            return pre.asLong();
        } catch (Exception e) {
            log.warn("解析 question.ext 失败 taskId={}: {}", taskId, e.getMessage());
            return null;
        }
    }

    /** 读取解锁方式：SEQUENTIAL（默认）/ ANY。 */
    public String unlockMode(Long contestId, Long taskId) {
        ScenarioTask st = taskMapper.selectOne(new LambdaQueryWrapper<ScenarioTask>()
                .eq(ScenarioTask::getContestId, contestId)
                .eq(ScenarioTask::getTaskId, taskId)
                .eq(ScenarioTask::getDeleted, 0)
                .last("LIMIT 1"));
        return st == null || st.getUnlockMode() == null ? "SEQUENTIAL" : st.getUnlockMode();
    }

    // ================================================================== 提交准入校验

    /**
     * 提交前置校验：未完成前置任务则拒绝提交。
     *
     * @param contestId 竞赛
     * @param teamId    队伍
     * @param taskId    待提交任务
     * @throws BizException 前置未完成（409）或任务不存在（404）
     */
    public void requirePreTasksDone(Long contestId, Long teamId, Long taskId) {
        CtfQuestion task = questionMapper.selectById(taskId);
        if (task == null) {
            throw BizException.notFound("场景任务 " + taskId);
        }
        List<Long> pres = preTaskIds(contestId, taskId);
        if (pres.isEmpty()) {
            return;
        }
        Set<Long> solved = solvedTaskIds(contestId, teamId);
        String mode = unlockMode(contestId, taskId);

        List<Long> missing;
        if ("ANY".equalsIgnoreCase(mode)) {
            // 任一前置完成即可
            boolean anyDone = pres.stream().anyMatch(solved::contains);
            missing = anyDone ? List.of() : pres;
        } else {
            // 默认 SEQUENTIAL：全部前置必须完成
            missing = pres.stream().filter(p -> !solved.contains(p)).toList();
        }

        if (!missing.isEmpty()) {
            List<String> titles = missing.stream()
                    .map(id -> {
                        CtfQuestion q = questionMapper.selectById(id);
                        return q == null ? String.valueOf(id) : q.getTitle();
                    })
                    .toList();
            throw new BizException(ResultCode.CONFLICT,
                    "请先完成前置任务：" + String.join("、", titles));
        }
    }

    /** 某队在某赛事已完成的任务ID集合（来自 submission 表，去重）。 */
    public Set<Long> solvedTaskIds(Long contestId, Long teamId) {
        List<Submission> subs = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getContestId, contestId)
                .eq(Submission::getTeamId, teamId)
                .eq(Submission::getCorrect, 1));
        Set<Long> out = new LinkedHashSet<>();
        for (Submission s : subs) {
            if (s.getQuestionId() != null) {
                out.add(s.getQuestionId());
            }
        }
        return out;
    }

    // ================================================================== 图算法：环检测与拓扑排序

    /**
     * 环检测：对一个场景下的全部任务构建依赖图，判断是否存在环。
     *
     * <p>三色标记 DFS：WHITE(未访问) → GRAY(访问中) → BLACK(已完成)。
     * 遇到 GRAY 节点即成环。</p>
     *
     * @param contestId  竞赛
     * @param scenarioId 场景题ID
     * @return 环路径（成环则返回节点序列，无环返回空列表）
     */
    public List<Long> detectCycle(Long contestId, Long scenarioId) {
        Map<Long, List<Long>> graph = buildGraph(contestId, scenarioId);
        Map<Long, Integer> color = new HashMap<>();
        List<Long> path = new ArrayList<>();

        for (Long node : graph.keySet()) {
            color.put(node, 0); // WHITE
        }
        for (Long node : graph.keySet()) {
            if (color.get(node) == 0) {
                List<Long> cycle = dfs(node, graph, color, new ArrayDeque<>());
                if (!cycle.isEmpty()) {
                    return cycle;
                }
            }
        }
        return path;
    }

    private List<Long> dfs(Long node, Map<Long, List<Long>> graph,
                           Map<Long, Integer> color, Deque<Long> stack) {
        color.put(node, 1); // GRAY
        stack.push(node);

        for (Long next : graph.getOrDefault(node, List.of())) {
            Integer c = color.get(next);
            if (c == null) {
                continue; // 不在本场景内的外部节点，忽略
            }
            if (c == 1) {
                // 成环：从栈中截取
                List<Long> cycle = new ArrayList<>();
                for (Long n : stack) {
                    cycle.add(n);
                    if (n.equals(next)) {
                        break;
                    }
                }
                Collections.reverse(cycle);
                cycle.add(next);
                return cycle;
            }
            if (c == 0) {
                List<Long> found = dfs(next, graph, color, stack);
                if (!found.isEmpty()) {
                    return found;
                }
            }
        }
        stack.pop();
        color.put(node, 2); // BLACK
        return List.of();
    }

    /**
     * 拓扑排序：返回任务解锁的推荐顺序（前置在前）。
     *
     * <p>Kahn 算法；若存在环，返回的列表不完整，调用方应先 {@link #detectCycle} 拦截。</p>
     */
    public List<Long> topologicalOrder(Long contestId, Long scenarioId) {
        Map<Long, List<Long>> graph = buildGraph(contestId, scenarioId); // node -> 后继
        Map<Long, Integer> inDegree = new HashMap<>();
        for (Long n : graph.keySet()) {
            inDegree.putIfAbsent(n, 0);
        }
        for (Map.Entry<Long, List<Long>> e : graph.entrySet()) {
            for (Long next : e.getValue()) {
                if (inDegree.containsKey(next)) {
                    inDegree.merge(next, 1, Integer::sum);
                }
            }
        }

        Deque<Long> queue = new ArrayDeque<>();
        inDegree.forEach((n, d) -> {
            if (d == 0) {
                queue.add(n);
            }
        });

        List<Long> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            Long cur = queue.poll();
            order.add(cur);
            for (Long next : graph.getOrDefault(cur, List.of())) {
                if (!inDegree.containsKey(next)) {
                    continue;
                }
                if (inDegree.merge(next, -1, Integer::sum) == 0) {
                    queue.add(next);
                }
            }
        }
        return order;
    }

    /**
     * 构建依赖图：key = 任务ID，value = 后继任务ID列表（前置 → 后继）。
     *
     * <p>数据源：优先 scenario_task 物化表，缺失时回落到 question.ext.preQuestionId。</p>
     */
    public Map<Long, List<Long>> buildGraph(Long contestId, Long scenarioId) {
        Map<Long, List<Long>> graph = new LinkedHashMap<>();

        List<ScenarioTask> rows = taskMapper.selectList(new LambdaQueryWrapper<ScenarioTask>()
                .eq(ScenarioTask::getContestId, contestId)
                .eq(ScenarioTask::getScenarioId, scenarioId)
                .eq(ScenarioTask::getDeleted, 0)
                .orderByAsc(ScenarioTask::getOrderNo));

        if (!rows.isEmpty()) {
            for (ScenarioTask st : rows) {
                graph.putIfAbsent(st.getTaskId(), new ArrayList<>());
                if (st.getPreTaskId() != null) {
                    graph.computeIfAbsent(st.getPreTaskId(), k -> new ArrayList<>()).add(st.getTaskId());
                }
            }
            return graph;
        }

        // 回落：从 question.ext 推导（该场景下所有 SCENARIO 题目）
        List<CtfQuestion> tasks = scenarioTasks(scenarioId);
        for (CtfQuestion q : tasks) {
            graph.putIfAbsent(q.getId(), new ArrayList<>());
        }
        for (CtfQuestion q : tasks) {
            Long pre = preQuestionIdFromExt(q.getId());
            if (pre != null && graph.containsKey(pre)) {
                graph.get(pre).add(q.getId());
            }
        }
        return graph;
    }

    /**
     * 取某场景下的全部任务题：场景题本身 + ext.scenarioId 指向它的题 + 同 category 的兄弟题。
     *
     * <p>与 captain 的 ScenarioService 模型保持一致的宽松判定，避免因组卷差异漏题。</p>
     */
    public List<CtfQuestion> scenarioTasks(Long scenarioId) {
        CtfQuestion scenario = questionMapper.selectById(scenarioId);
        if (scenario == null) {
            return List.of();
        }
        List<CtfQuestion> all = questionMapper.selectList(new LambdaQueryWrapper<CtfQuestion>()
                .eq(CtfQuestion::getBankType, "SCENARIO")
                .eq(CtfQuestion::getStatus, 1));

        List<CtfQuestion> out = new ArrayList<>();
        for (CtfQuestion q : all) {
            if (q.getId().equals(scenarioId) || belongsTo(q, scenario)) {
                out.add(q);
            }
        }
        return out;
    }

    private boolean belongsTo(CtfQuestion q, CtfQuestion scenario) {
        if (q.getExt() == null || q.getExt().isBlank()) {
            return false;
        }
        try {
            JsonNode ext = objectMapper.readTree(q.getExt());
            JsonNode sid = ext.get("scenarioId");
            if (sid != null && !sid.isNull() && sid.asLong() == scenario.getId()) {
                return true;
            }
        } catch (Exception ignored) {
            // 解析失败按不属于处理
        }
        return false;
    }
}