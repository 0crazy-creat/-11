package com.csp.cfs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.cfs.dto.CfsTopologySaveCmd;
import com.csp.cfs.entity.CfsLayer;
import com.csp.cfs.entity.CfsNode;
import com.csp.cfs.entity.CfsTopology;
import com.csp.cfs.enums.NetworkLayer;
import com.csp.cfs.guard.CfsUnlockGuard;
import com.csp.cfs.mapper.CfsLayerMapper;
import com.csp.cfs.mapper.CfsNodeMapper;
import com.csp.cfs.mapper.CfsTopologyMapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CFS 分层拓扑管理：创建/查询拓扑、层与节点定义，并做拓扑约束校验。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CfsTopologyService {

    /** 默认各层基础分（越深层越高）。 */
    private static final int[] DEFAULT_BASE_SCORES = {100, 300, 600};
    private static final int DEFAULT_SCORE_STEP = 50;

    private final CfsTopologyMapper topologyMapper;
    private final CfsLayerMapper layerMapper;
    private final CfsNodeMapper nodeMapper;
    private final CfsUnlockGuard unlockGuard;

    /** 新建分层拓扑（含层与节点定义）。 */
    @Transactional(rollbackFor = Exception.class)
    public CfsTopology create(CfsTopologySaveCmd cmd) {
        Long dup = topologyMapper.selectCount(new LambdaQueryWrapper<CfsTopology>()
                .eq(CfsTopology::getCode, cmd.getCode())
                .eq(CfsTopology::getDeleted, 0));
        if (dup != null && dup > 0) throw new BizException(ResultCode.CONFLICT, "拓扑编码已存在：" + cmd.getCode());

        List<CfsLayer> layers = buildLayers(cmd);
        unlockGuard.validateLayerDefs(layers);
        int layerCount = layers.size();

        CfsTopology t = new CfsTopology();
        t.setContestId(cmd.getContestId());
        t.setCode(cmd.getCode());
        t.setName(cmd.getName());
        t.setDescription(cmd.getDescription());
        t.setLayerCount(layerCount);
        t.setStatus("DRAFT");
        t.setDeleted(0);
        topologyMapper.insert(t);

        Map<Integer, CfsLayer> byNo = new HashMap<>();
        for (CfsLayer l : layers) {
            l.setTopologyId(t.getId());
            l.setLayerCode(NetworkLayer.ofNo(l.getLayerNo()).name());
            l.setDeleted(0);
            layerMapper.insert(l);
            byNo.put(l.getLayerNo(), l);
        }

        List<CfsNode> nodes = buildNodes(cmd, byNo, layerCount);
        unlockGuard.validateNodes(nodes, layerCount);
        for (CfsNode n : nodes) {
            n.setTopologyId(t.getId());
            n.setLayerId(byNo.get(n.getLayerNo()).getId());
            n.setDeleted(0);
            nodeMapper.insert(n);
        }
        log.info("CFS 拓扑创建完成 code={} layers={} nodes={}", t.getCode(), layerCount, nodes.size());
        return t;
    }

    public CfsTopology get(Long topologyId) {
        CfsTopology t = topologyMapper.selectById(topologyId);
        if (t == null || (t.getDeleted() != null && t.getDeleted() == 1)) {
            throw BizException.notFound("CFS 拓扑");
        }
        return t;
    }

    /** 按赛事查最新拓扑（CFS 赛事一键起拓扑用）。 */
    public CfsTopology latestByContest(Long contestId) {
        List<CfsTopology> list = topologyMapper.selectList(new LambdaQueryWrapper<CfsTopology>()
                .eq(CfsTopology::getContestId, contestId)
                .eq(CfsTopology::getDeleted, 0)
                .orderByDesc(CfsTopology::getCreatedAt));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 拓扑列表（管理端）：contestId 为空返回全部，否则按赛事过滤。 */
    public List<CfsTopology> listByContest(Long contestId) {
        LambdaQueryWrapper<CfsTopology> w = new LambdaQueryWrapper<CfsTopology>()
                .eq(CfsTopology::getDeleted, 0)
                .orderByDesc(CfsTopology::getCreatedAt);
        if (contestId != null) {
            w.eq(CfsTopology::getContestId, contestId);
        }
        return topologyMapper.selectList(w);
    }

    public List<CfsLayer> layers(Long topologyId) {
        return layerMapper.selectList(new LambdaQueryWrapper<CfsLayer>()
                .eq(CfsLayer::getTopologyId, topologyId)
                .eq(CfsLayer::getDeleted, 0)
                .orderByAsc(CfsLayer::getLayerNo));
    }

    public List<CfsNode> nodes(Long topologyId) {
        return nodeMapper.selectList(new LambdaQueryWrapper<CfsNode>()
                .eq(CfsNode::getTopologyId, topologyId)
                .eq(CfsNode::getDeleted, 0)
                .orderByAsc(CfsNode::getLayerNo)
                .orderByAsc(CfsNode::getNodeCode));
    }

    public List<CfsNode> nodesOfLayer(Long topologyId, int layerNo) {
        return nodeMapper.selectList(new LambdaQueryWrapper<CfsNode>()
                .eq(CfsNode::getTopologyId, topologyId)
                .eq(CfsNode::getLayerNo, layerNo)
                .eq(CfsNode::getDeleted, 0)
                .orderByAsc(CfsNode::getNodeCode));
    }

    /** 发布拓扑（之后可用于开赛拉拓扑）。 */
    @Transactional(rollbackFor = Exception.class)
    public CfsTopology publish(Long topologyId) {
        CfsTopology t = get(topologyId);
        List<CfsLayer> layers = layers(topologyId);
        unlockGuard.validateLayerDefs(layers);
        unlockGuard.validateNodes(nodes(topologyId), layers.size());
        t.setStatus("PUBLISHED");
        topologyMapper.updateById(t);
        return t;
    }

    // ---------- internal ----------

    private List<CfsLayer> buildLayers(CfsTopologySaveCmd cmd) {
        List<CfsTopologySaveCmd.LayerCmd> cmds = cmd.getLayers().stream()
                .sorted(Comparator.comparing(CfsTopologySaveCmd.LayerCmd::getLayerNo))
                .toList();
        List<CfsLayer> out = new ArrayList<>();
        for (CfsTopologySaveCmd.LayerCmd lc : cmds) {
            int no = lc.getLayerNo();
            NetworkLayer nl = NetworkLayer.ofNo(no);
            CfsLayer l = new CfsLayer();
            l.setLayerNo(no);
            l.setLayerCode(nl.name());
            l.setName(lc.getName() == null || lc.getName().isBlank() ? nl.getLabel() : lc.getName());
            l.setPrevLayerNo(lc.getPrevLayerNo() == null ? nl.getPrevNo() : lc.getPrevLayerNo());
            int defScore = no - 1 < DEFAULT_BASE_SCORES.length ? DEFAULT_BASE_SCORES[no - 1] : 100 * no;
            l.setBaseScore(lc.getBaseScore() == null ? defScore : lc.getBaseScore());
            l.setScoreStep(lc.getScoreStep() == null ? DEFAULT_SCORE_STEP : lc.getScoreStep());
            l.setNetworkCidr(lc.getNetworkCidr() == null || lc.getNetworkCidr().isBlank()
                    ? "172.31." + no + ".0/24" : lc.getNetworkCidr());
            // 只有入口层暴露端口；其余层一律不暴露
            l.setExposePort(no == CfsUnlockGuard.ENTRY_LAYER ? 1
                    : (lc.getExposePort() == null ? 0 : lc.getExposePort()));
            out.add(l);
        }
        return out;
    }

    private List<CfsNode> buildNodes(CfsTopologySaveCmd cmd, Map<Integer, CfsLayer> byNo, int layerCount) {
        if (cmd.getNodes() == null || cmd.getNodes().isEmpty()) return List.of();
        Map<Integer, List<CfsTopologySaveCmd.NodeCmd>> grouped = cmd.getNodes().stream()
                .collect(Collectors.groupingBy(CfsTopologySaveCmd.NodeCmd::getLayerNo));
        List<CfsNode> out = new ArrayList<>();
        grouped.forEach((no, list) -> {
            CfsLayer layer = byNo.get(no);
            if (layer == null) {
                throw new BizException(ResultCode.BAD_REQUEST, "节点引用了不存在的层：" + no);
            }
            int idx = 0;
            for (CfsTopologySaveCmd.NodeCmd nc : list) {
                CfsNode n = new CfsNode();
                n.setLayerNo(no);
                n.setNodeCode(nc.getNodeCode());
                n.setName(nc.getName());
                n.setQuestionId(nc.getQuestionId());
                n.setImageName(nc.getImageName());
                n.setContainerPort(nc.getContainerPort() == null ? 80 : nc.getContainerPort());
                n.setIsEntry(nc.getIsEntry() == null ? (no == 1 ? 1 : 0) : nc.getIsEntry());
                // 递进得分：层基础分 + 层内序位 × 步长（越深层越高）
                int score = nc.getScore() != null ? nc.getScore()
                        : layer.getBaseScore() + idx * (layer.getScoreStep() == null ? 0 : layer.getScoreStep());
                n.setScore(score);
                n.setFlag(nc.getFlag());
                n.setDynamicFlag(nc.getDynamicFlag() == null ? 1 : nc.getDynamicFlag());
                out.add(n);
                idx++;
            }
        });
        return out;
    }

    /** 便捷视图：拓扑 → 层/节点映射。 */
    public Map<Integer, CfsLayer> layerMap(Long topologyId) {
        return layers(topologyId).stream().collect(Collectors.toMap(CfsLayer::getLayerNo, Function.identity()));
    }
}