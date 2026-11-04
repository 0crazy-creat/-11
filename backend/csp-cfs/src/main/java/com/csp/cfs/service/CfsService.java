package com.csp.cfs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.cfs.dto.CfsSubmitCmd;
import com.csp.cfs.dto.CfsSubmitResult;
import com.csp.cfs.dto.CfsTopologyVO;
import com.csp.cfs.engine.CfsNetworkOrchestrator;
import com.csp.cfs.engine.CfsScorePolicy;
import com.csp.cfs.entity.*;
import com.csp.cfs.enums.NetworkLayer;
import com.csp.cfs.guard.CfsUnlockGuard;
import com.csp.cfs.mapper.*;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.common.util.FlagUtils;
import com.csp.ctf.entity.Contest;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.ContestTeamMember;
import com.csp.ctf.entity.Submission;
import com.csp.ctf.mapper.ContestMapper;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.ctf.mapper.ContestTeamMemberMapper;
import com.csp.ctf.mapper.SubmissionMapper;
import com.csp.ctf.service.ContestGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CFS 核心服务：拓扑实例化、按层递进提交 flag、解锁推进、拓扑视图。
 *
 * <p>关键约束：</p>
 * <ol>
 *   <li><b>前置依赖</b>：未解锁层的 flag 直接拒绝提交（{@link CfsUnlockGuard}）；</li>
 *   <li><b>递进得分</b>：分值由 {@link CfsScorePolicy} 按层深度计算，越深层越高；</li>
 *   <li><b>幂等计分</b>：同一队伍同一节点只计分一次；</li>
 *   <li><b>解锁推进</b>：每次成功命中后重算 unlockedLayer 并落审计。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CfsService {

    private static final String CF_TYPE = "CFS";

    private final CfsTopologyMapper topologyMapper;
    private final CfsLayerMapper layerMapper;
    private final CfsNodeMapper nodeMapper;
    private final CfsInstanceMapper instanceMapper;
    private final CfsNodeInstanceMapper nodeInstanceMapper;
    private final CfsUnlockLogMapper unlockLogMapper;
    private final ContestMapper contestMapper;
    private final ContestTeamMapper teamMapper;
    private final ContestTeamMemberMapper teamMemberMapper;
    private final SubmissionMapper submissionMapper;
    private final ContestGuard contestGuard;
    private final CfsUnlockGuard unlockGuard;
    private final CfsScorePolicy scorePolicy;
    private final CfsNetworkOrchestrator orchestrator;

    // =====================================================================
    // 实例化：为队伍拉起分层拓扑
    // =====================================================================

    /** 为队伍创建/复用拓扑实例（开赛或首次访问时调用，幂等）。 */
    @Transactional(rollbackFor = Exception.class)
    public CfsInstance ensureInstance(Long contestId, Long teamId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) throw BizException.notFound("赛事");
        requireCfs(contest);

        CfsInstance exist = instanceMapper.selectOne(new LambdaQueryWrapper<CfsInstance>()
                .eq(CfsInstance::getContestId, contestId)
                .eq(CfsInstance::getTeamId, teamId));
        if (exist != null) return exist;

        CfsTopology topology = findTopology(contestId);
        List<CfsLayer> layers = layersOf(topology.getId());
        if (layers.isEmpty()) throw new BizException(ResultCode.CONFLICT, "CFS 拓扑缺少层定义，无法开赛");
        List<CfsNode> nodes = nodesOf(topology.getId());
        if (nodes.isEmpty()) throw new BizException(ResultCode.CONFLICT, "CFS 拓扑缺少靶机节点，无法开赛");

        Long count = instanceMapper.selectCount(new LambdaQueryWrapper<CfsInstance>()
                .eq(CfsInstance::getContestId, contestId));
        int cursor = (int) ((count == null ? 0 : count) * Math.max(1, nodes.size()));

        CfsNetworkOrchestrator.Plan plan = orchestrator.plan(contestId, teamId, layers, nodes, cursor);

        CfsInstance instance = new CfsInstance();
        instance.setContestId(contestId);
        instance.setTeamId(teamId);
        instance.setTopologyId(topology.getId());
        instance.setStatus("READY");
        instance.setUnlockedLayer(unlockGuard.initialUnlockedLayer(layers.size()));
        instance.setSolvedLayers(",");
        instance.setTotalScore(0L);
        instanceMapper.insert(instance);

        for (CfsNodeInstance ni : plan.getNodes()) {
            ni.setInstanceId(instance.getId());
            ni.setStatus("RUNNING");
            nodeInstanceMapper.insert(ni);
        }
        log.info("CFS 实例创建 contest={} team={} layers={} nodes={} networks={}",
                contestId, teamId, layers.size(), plan.getNodes().size(), plan.getNetworks());
        return instance;
    }

    /** 开赛：为所有已通过审核的战队批量拉起分层拓扑。 */
    @Transactional(rollbackFor = Exception.class)
    public int startContest(Long contestId) {
        Contest contest = contestGuard.requireAnswering(contestId);
        requireCfs(contest);
        List<ContestTeam> teams = teamMapper.selectList(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getContestId, contestId)
                .eq(ContestTeam::getStatus, "APPROVED"));
        int n = 0;
        for (ContestTeam t : teams) {
            ensureInstance(contestId, t.getId());
            n++;
        }
        log.info("CFS 开赛完成 contest={} 战队实例数={}", contestId, n);
        return n;
    }

    // =====================================================================
    // 提交：按层递进 + 解锁校验
    // =====================================================================

    /** 提交 flag：解锁前置校验 → 常量时间比对 → 推进解锁与递进计分。 */
    @Transactional(rollbackFor = Exception.class)
    public CfsSubmitResult submit(Long userId, CfsSubmitCmd cmd) {
        Contest contest = contestGuard.requireAnswering(cmd.getContestId());
        requireCfs(contest);

        ContestTeam team = currentTeam(userId, cmd.getContestId());
        if (team == null) throw new BizException(ResultCode.FORBIDDEN, "未报名该赛事，无法提交");
        if (!"APPROVED".equals(team.getStatus())) {
            throw new BizException(ResultCode.FORBIDDEN, "战队尚未通过审核：" + team.getStatus());
        }

        CfsInstance instance = ensureInstance(cmd.getContestId(), team.getId());

        CfsNode node = nodeMapper.selectById(cmd.getNodeId());
        if (node == null || !Objects.equals(node.getTopologyId(), instance.getTopologyId())) {
            throw BizException.notFound("CFS 节点");
        }

        // ① 拓扑约束：未解锁层直接拒绝（禁止跳关）
        unlockGuard.requireLayerUnlocked(instance, node);

        // ①' 任务级前置依赖：pre_task_id 非空时，必须先完成该任务
        requirePreTaskSolved(instance, node);

        CfsNodeInstance ni = nodeInstanceMapper.selectOne(new LambdaQueryWrapper<CfsNodeInstance>()
                .eq(CfsNodeInstance::getInstanceId, instance.getId())
                .eq(CfsNodeInstance::getNodeId, node.getId()));
        if (ni == null) throw BizException.notFound("CFS 节点实例");

        int unlockedBefore = instance.getUnlockedLayer() == null ? 1 : instance.getUnlockedLayer();
        long totalBefore = instance.getTotalScore() == null ? 0L : instance.getTotalScore();
        List<Integer> solvedLayers = parseLayers(instance.getSolvedLayers());
        Long elapsed = calcElapsed(cmd.getStartTs());

        if (ni.getSolved() != null && ni.getSolved() == 1) {
            recordSubmission(userId, cmd, team.getId(), node, true, 0);
            return CfsSubmitResult.builder()
                    .correct(true).score(0).layerNo(node.getLayerNo())
                    .unlockedBefore(unlockedBefore).unlockedAfter(unlockedBefore)
                    .layerUnlocked(false).newlyUnlockedLayer(null)
                    .totalScore(totalBefore).solvedLayers(solvedLayers)
                    .message("该节点已攻破，本次不重复计分")
                    .elapsedMs(elapsed).build();
        }

        String submitted = FlagUtils.normalize(cmd.getFlag());
        boolean correct = !submitted.isEmpty()
                && FlagUtils.safeEquals(FlagUtils.normalize(ni.getFlag()), submitted);

        if (!correct) {
            recordSubmission(userId, cmd, team.getId(), node, false, 0);
            return CfsSubmitResult.builder()
                    .correct(false).score(0).layerNo(node.getLayerNo())
                    .unlockedBefore(unlockedBefore).unlockedAfter(unlockedBefore)
                    .layerUnlocked(false).newlyUnlockedLayer(null)
                    .totalScore(totalBefore).solvedLayers(solvedLayers)
                    .message("flag 错误")
                    .elapsedMs(elapsed).build();
        }

        // ② 递进得分
        CfsLayer layer = layerMapper.selectById(node.getLayerId());
        int score = scorePolicy.scoreOf(node, layer);

        ni.setSolved(1);
        ni.setSolvedAt(LocalDateTime.now());
        nodeInstanceMapper.updateById(ni);

        // ②' 整层完成奖励：本层全部节点首次攻破时额外加分（去重靠 unlock_log 的 nodeId=null 行）
        int layerBonus = layerBonusIfCompleted(instance, node.getLayerNo());
        score += layerBonus;
        if (layerBonus > 0) {
            CfsUnlockLog bonusRow = new CfsUnlockLog();
            bonusRow.setContestId(cmd.getContestId());
            bonusRow.setTeamId(team.getId());
            bonusRow.setNodeId(null);
            bonusRow.setLayerNo(node.getLayerNo());
            bonusRow.setUnlocked(instance.getUnlockedLayer() == null ? 1 : instance.getUnlockedLayer());
            bonusRow.setScore(layerBonus);
            bonusRow.setCreatedAt(LocalDateTime.now());
            unlockLogMapper.insert(bonusRow);
        }

        solvedLayers.add(node.getLayerNo());
        Set<Integer> solvedSet = new HashSet<>(solvedLayers);
        int layerCount = layersOf(instance.getTopologyId()).size();
        int unlockedAfter = unlockGuard.recomputeUnlockedLayer(solvedSet, layerCount);

        instance.setSolvedLayers(toLayerString(solvedSet));
        instance.setUnlockedLayer(unlockedAfter);
        instance.setTotalScore(totalBefore + score);
        instanceMapper.updateById(instance);

        recordSubmission(userId, cmd, team.getId(), node, true, score);

        Integer newlyUnlocked = unlockedAfter > unlockedBefore ? unlockedAfter : null;
        CfsUnlockLog row = new CfsUnlockLog();
        row.setContestId(cmd.getContestId());
        row.setTeamId(team.getId());
        row.setNodeId(node.getId());
        row.setLayerNo(node.getLayerNo());
        row.setUnlocked(unlockedAfter);
        row.setScore(score);
        row.setCreatedAt(LocalDateTime.now());
        unlockLogMapper.insert(row);

        String msg = "flag 正确 +" + score + " 分（LAYER" + node.getLayerNo() + "）";
        if (layerBonus > 0) {
            msg += "；含整层完成奖励 +" + layerBonus + " 分";
        }
        if (newlyUnlocked != null) {
            msg += "；已解锁 LAYER" + newlyUnlocked + "（" + NetworkLayer.ofNo(newlyUnlocked).getLabel() + "）";
        }
        return CfsSubmitResult.builder()
                .correct(true).score(score).layerNo(node.getLayerNo())
                .unlockedBefore(unlockedBefore).unlockedAfter(unlockedAfter)
                .layerUnlocked(newlyUnlocked != null).newlyUnlockedLayer(newlyUnlocked)
                .totalScore(instance.getTotalScore()).solvedLayers(new ArrayList<>(solvedSet))
                .message(msg).elapsedMs(elapsed).build();
    }

    // =====================================================================
    // 拓扑视图（前端渲染已解锁/未解锁层）
    // =====================================================================

    /** 当前队伍的 CFS 拓扑视图：逐层标注解锁状态与节点可达性。 */
    public CfsTopologyVO topologyView(Long contestId, Long userId) {
        Contest contest = contestGuard.requireContest(contestId);
        requireCfs(contest);
        ContestTeam team = currentTeam(userId, contestId);
        if (team == null) {
            // 管理员/裁判/教师免报名：以该赛第一支队伍视角预览拓扑（不影响其真实成绩）
            if (!isPrivileged()) {
                throw new BizException(ResultCode.FORBIDDEN, "未报名该赛事");
            }
            team = firstTeamOf(contestId);
            if (team == null) throw new BizException(ResultCode.NOT_FOUND, "该赛事暂无队伍，无法预览拓扑");
        }

        CfsInstance instance = ensureInstance(contestId, team.getId());
        CfsTopology topology = topologyMapper.selectById(instance.getTopologyId());
        List<CfsLayer> layers = layersOf(topology.getId());
        List<CfsNode> nodes = nodesOf(topology.getId());

        Map<Long, CfsNodeInstance> niByNode = nodeInstanceMapper.selectList(
                        new LambdaQueryWrapper<CfsNodeInstance>()
                                .eq(CfsNodeInstance::getInstanceId, instance.getId()))
                .stream().collect(Collectors.toMap(CfsNodeInstance::getNodeId, x -> x, (a, b) -> a));

        Set<Integer> solvedLayers = new HashSet<>(parseLayers(instance.getSolvedLayers()));
        int unlocked = instance.getUnlockedLayer() == null ? 1 : instance.getUnlockedLayer();

        List<CfsTopologyVO.LayerVO> layerVOs = new ArrayList<>();
        for (CfsLayer l : layers) {
            boolean isUnlocked = l.getLayerNo() <= unlocked;
            boolean isSolved = solvedLayers.contains(l.getLayerNo());
            String reason = null;
            if (!isUnlocked) {
                int prev = l.getPrevLayerNo() == null ? 0 : l.getPrevLayerNo();
                reason = "需先攻破 LAYER" + prev + "（" + NetworkLayer.ofNo(prev).getLabel() + "）任意一处 flag";
            }
            List<CfsTopologyVO.NodeVO> nodeVOs = nodes.stream()
                    .filter(n -> Objects.equals(n.getLayerNo(), l.getLayerNo()))
                    .map(n -> toNodeVO(n, niByNode.get(n.getId()), isUnlocked))
                    .toList();
            layerVOs.add(CfsTopologyVO.LayerVO.builder()
                    .layerId(l.getId()).layerNo(l.getLayerNo()).layerCode(l.getLayerCode())
                    .name(l.getName()).prevLayerNo(l.getPrevLayerNo())
                    .baseScore(l.getBaseScore()).networkCidr(l.getNetworkCidr())
                    .exposePort(l.getExposePort())
                    .unlocked(isUnlocked).solved(isSolved)
                    .lockedReason(reason)
                    .nodes(nodeVOs)
                    .build());
        }

        return CfsTopologyVO.builder()
                .contestId(contestId).teamId(team.getId()).instanceId(instance.getId())
                .topologyId(topology.getId()).topologyName(topology.getName())
                .unlockedLayer(unlocked).solvedLayers(new ArrayList<>(solvedLayers))
                .totalScore(instance.getTotalScore())
                .layers(layerVOs)
                .build();
    }

    /** 特权角色（管理员/裁判/教师/超管/平台管理员）免报名。 */
    private boolean isPrivileged() {
        try {
            var u = com.csp.security.core.UserContext.get();
            if (u == null) return false;
            if (com.csp.security.core.UserContext.isAdmin()) return true;
            if (u.hasPermission("*") || u.hasPermission("awd:control") || u.hasPermission("contest:update")) return true;
            for (String r : new String[]{"ADMIN","SUPER_ADMIN","PLATFORM_ADMIN","JUDGE","TEACHER"}) {
                if (u.hasRole(r)) return true;
            }
        } catch (Throwable ignore) {
        }
        return false;
    }

    /** 该赛第一支已通过审核的队伍（管理员预览用）。 */
    private ContestTeam firstTeamOf(Long contestId) {
        var list = teamMapper.selectList(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getContestId, contestId)
                .orderByAsc(ContestTeam::getId)
                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 队伍解锁进度（轻量）。 */
    public CfsInstance myInstance(Long contestId, Long userId) {
        ContestTeam team = currentTeam(userId, contestId);
        if (team == null) return null;
        return ensureInstance(contestId, team.getId());
    }


    /**
     * 任务级前置依赖校验：若节点配置了 preTaskId，则该前置任务必须已被本队攻破，
     * 否则拒绝提交并给出明确提示。
     */
    private void requirePreTaskSolved(CfsInstance instance, CfsNode node) {
        Long preTaskId = node.getPreTaskId();
        if (preTaskId == null) return;
        CfsNode pre = nodeMapper.selectById(preTaskId);
        if (pre == null) return;
        CfsNodeInstance preNi = nodeInstanceMapper.selectOne(new LambdaQueryWrapper<CfsNodeInstance>()
                .eq(CfsNodeInstance::getInstanceId, instance.getId())
                .eq(CfsNodeInstance::getNodeId, preTaskId));
        boolean solved = preNi != null && preNi.getSolved() != null && preNi.getSolved() == 1;
        if (!solved) {
            throw new BizException(ResultCode.FORBIDDEN,
                    "需先完成前置任务 " + pre.getName() + "（" + pre.getNodeCode()
                            + "，LAYER" + pre.getLayerNo() + "）");
        }
    }

    /**
     * 整层完成奖励：本次提交后若该层全部节点已攻破且首次完成，返回奖励分，否则 0。
     */
    private int layerBonusIfCompleted(CfsInstance instance, int layerNo) {
        CfsLayer layer = layerMapper.selectOne(new LambdaQueryWrapper<CfsLayer>()
                .eq(CfsLayer::getTopologyId, instance.getTopologyId())
                .eq(CfsLayer::getLayerNo, layerNo));
        if (layer == null || layer.getLayerBonus() == null || layer.getLayerBonus() <= 0) return 0;
        List<CfsNode> layerNodes = nodeMapper.selectList(new LambdaQueryWrapper<CfsNode>()
                .eq(CfsNode::getTopologyId, instance.getTopologyId())
                .eq(CfsNode::getLayerNo, layerNo)
                .eq(CfsNode::getDeleted, 0));
        if (layerNodes.isEmpty()) return 0;
        List<Long> ids = layerNodes.stream().map(CfsNode::getId).toList();
        Long solvedCnt = nodeInstanceMapper.selectCount(new LambdaQueryWrapper<CfsNodeInstance>()
                .eq(CfsNodeInstance::getInstanceId, instance.getId())
                .in(CfsNodeInstance::getNodeId, ids)
                .eq(CfsNodeInstance::getSolved, 1));
        boolean allSolved = solvedCnt != null && solvedCnt >= ids.size();
        if (!allSolved) return 0;
        // 防重复：已有本层奖励审计（node_id 为空且 score=bonus）则不再计
        Long bonusRows = unlockLogMapper.selectCount(new LambdaQueryWrapper<CfsUnlockLog>()
                .eq(CfsUnlockLog::getContestId, instance.getContestId())
                .eq(CfsUnlockLog::getTeamId, instance.getTeamId())
                .eq(CfsUnlockLog::getLayerNo, layerNo)
                .isNull(CfsUnlockLog::getNodeId));
        if (bonusRows != null && bonusRows > 0) return 0;
        return layer.getLayerBonus();
    }

// ---------- internal ----------

    private void requireCfs(Contest contest) {
        if (!CF_TYPE.equalsIgnoreCase(contest.getType())) {
            throw new BizException(ResultCode.BAD_REQUEST, "非 CFS 赛事（type=" + contest.getType() + "）");
        }
    }

    private CfsTopologyVO.NodeVO toNodeVO(CfsNode n, CfsNodeInstance ni, boolean layerUnlocked) {
        boolean solved = ni != null && ni.getSolved() != null && ni.getSolved() == 1;
        boolean expose = layerUnlocked && n.getIsEntry() != null && n.getIsEntry() == 1
                && ni != null && ni.getHostPort() != null;
        return CfsTopologyVO.NodeVO.builder()
                .nodeId(n.getId())
                .nodeInstanceId(ni == null ? null : ni.getId())
                .nodeCode(n.getNodeCode()).name(n.getName()).layerNo(n.getLayerNo())
                .questionId(n.getQuestionId()).isEntry(n.getIsEntry()).score(n.getScore())
                .solved(solved)
                .host(expose ? ni.getHost() : null)
                .hostPort(expose ? ni.getHostPort() : null)
                .innerIp(layerUnlocked && ni != null ? ni.getInnerIp() : null)
                .reachable(layerUnlocked)
                .status(ni == null ? "PENDING" : ni.getStatus())
                .build();
    }

    private CfsTopology findTopology(Long contestId) {
        List<CfsTopology> list = topologyMapper.selectList(new LambdaQueryWrapper<CfsTopology>()
                .eq(CfsTopology::getContestId, contestId)
                .eq(CfsTopology::getDeleted, 0)
                .orderByDesc(CfsTopology::getCreatedAt));
        if (list.isEmpty()) throw new BizException(ResultCode.CONFLICT, "该 CFS 赛事尚未配置分层拓扑");
        return list.get(0);
    }

    private List<CfsLayer> layersOf(Long topologyId) {
        return layerMapper.selectList(new LambdaQueryWrapper<CfsLayer>()
                .eq(CfsLayer::getTopologyId, topologyId)
                .eq(CfsLayer::getDeleted, 0)
                .orderByAsc(CfsLayer::getLayerNo));
    }

    private List<CfsNode> nodesOf(Long topologyId) {
        return nodeMapper.selectList(new LambdaQueryWrapper<CfsNode>()
                .eq(CfsNode::getTopologyId, topologyId)
                .eq(CfsNode::getDeleted, 0)
                .orderByAsc(CfsNode::getLayerNo).orderByAsc(CfsNode::getNodeCode));
    }

    private ContestTeam currentTeam(Long userId, Long contestId) {
        ContestTeamMember m = teamMemberMapper.selectOne(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getContestId, contestId)
                .eq(ContestTeamMember::getUserId, userId));
        return m == null ? null : teamMapper.selectById(m.getTeamId());
    }

    private void recordSubmission(Long userId, CfsSubmitCmd cmd, Long teamId, CfsNode node,
                                  boolean correct, int score) {
        Submission s = new Submission();
        s.setContestId(cmd.getContestId());
        s.setTeamId(teamId);
        s.setUserId(userId);
        s.setQuestionId(node.getQuestionId() == null ? node.getId() : node.getQuestionId());
        s.setSubmitType("FLAG");
        s.setContent(FlagUtils.sha256(FlagUtils.normalize(cmd.getFlag())));
        s.setCorrect(correct ? 1 : 0);
        s.setScore(score);
        s.setDeviceId(cmd.getDeviceId());
        s.setCreatedAt(LocalDateTime.now());
        submissionMapper.insert(s);
    }

    private List<Integer> parseLayers(String s) {
        if (s == null || s.isBlank()) return new ArrayList<>();
        return Arrays.stream(s.split(","))
                .map(String::trim).filter(x -> !x.isEmpty())
                .map(Integer::parseInt).distinct().sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String toLayerString(Set<Integer> layers) {
        StringBuilder sb = new StringBuilder(",");
        layers.stream().sorted().forEach(x -> sb.append(x).append(','));
        return sb.toString();
    }

    private Long calcElapsed(Long startTs) {
        if (startTs == null || startTs <= 0) return null;
        long el = System.currentTimeMillis() - startTs;
        return el < 0 ? 0L : el;
    }
}