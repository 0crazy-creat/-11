package com.csp.cfs.engine;

import com.csp.cfs.entity.CfsLayer;
import com.csp.cfs.entity.CfsNode;
import com.csp.cfs.entity.CfsNodeInstance;
import com.csp.common.util.FlagUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CFS 分层网络编排器。
 *
 * <p>模拟商业 CFS 靶场的多段网络：</p>
 * <ul>
 *   <li>每层一个独立 Docker network（{@code csp-cfs-{contest}-{team}-l{no}}），层内互通、跨层默认隔离；</li>
 *   <li>仅入口层（LAYER1）的入口机映射宿主端口，内层节点只分配层内 IP，宿主不可直达；</li>
 *   <li>为每台靶机派生队伍独立的动态 flag（写入节点实例，供靶机注入与判题比对）。</li>
 * </ul>
 *
 * <p>当前实现输出的是「网络编排计划」（plan）而非真实 docker 调用——真实拉起由部署层
 * 依据 plan 执行，这样在无 Docker 的 CI / 单测环境也能完整验证解锁与得分链路。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CfsNetworkOrchestrator {

    /** 每层网络的节点内网起始偏移。 */
    private static final int IP_START = 10;

    /** 端口池起点（CFS 专用，避免与 AWD 20000-30000 冲突）。 */
    public static final int PORT_START = 31000;
    public static final int PORT_END = 39000;

    /**
     * 生成某队伍的分层编排计划。
     *
     * @param contestId 赛事
     * @param teamId    队伍
     * @param layers    层定义（有序）
     * @param nodes     全部节点
     * @param hostPortCursor 入口端口分配游标（由调用方持久化）
     * @return 计划（含每个节点的 network / ip / 是否暴露 / flag）
     */
    public Plan plan(Long contestId, Long teamId, List<CfsLayer> layers, List<CfsNode> nodes, int hostPortCursor) {
        List<CfsNodeInstance> planned = new ArrayList<>();
        int portCursor = hostPortCursor;
        int layerCount = layers.size();

        for (CfsLayer layer : layers) {
            int no = layer.getLayerNo();
            String networkName = networkName(contestId, teamId, no);
            boolean expose = layer.getExposePort() != null && layer.getExposePort() == 1;
            int idx = 0;
            for (CfsNode node : nodes) {
                if (node.getLayerNo() == null || node.getLayerNo() != no) continue;
                CfsNodeInstance ni = new CfsNodeInstance();
                ni.setContestId(contestId);
                ni.setTeamId(teamId);
                ni.setNodeId(node.getId());
                ni.setLayerNo(no);
                ni.setNetworkName(networkName);
                ni.setContainerName(containerName(contestId, teamId, node.getNodeCode()));
                ni.setStatus("PENDING");
                ni.setSolved(0);
                ni.setFlag(resolveFlag(node, teamId));
                // 仅入口层的入口机映射宿主端口；其余仅层内 IP
                if (expose && node.getIsEntry() != null && node.getIsEntry() == 1) {
                    ni.setHost("127.0.0.1");
                    ni.setHostPort(nextPort(portCursor++));
                }
                ni.setInnerIp("172.31." + no + "." + (IP_START + idx));
                planned.add(ni);
                idx++;
            }
        }

        return Plan.builder()
                .layerCount(layerCount)
                .networks(layers.stream().map(l -> networkName(contestId, teamId, l.getLayerNo())).toList())
                .nodes(planned)
                .nextPortCursor(portCursor)
                .build();
    }

    /** 层网络名：层间隔离的关键（不同网络默认不可互通）。 */
    public String networkName(Long contestId, Long teamId, int layerNo) {
        return "csp-cfs-" + contestId + "-" + teamId + "-l" + layerNo;
    }

    public String containerName(Long contestId, Long teamId, String nodeCode) {
        return "cfs-" + contestId + "-" + teamId + "-" + nodeCode;
    }

    /** 动态 flag 派生：同一队伍同一节点稳定，跨队不同。 */
    public String resolveFlag(CfsNode node, Long teamId) {
        boolean dynamic = node.getDynamicFlag() == null || node.getDynamicFlag() == 1;
        if (!dynamic && node.getFlag() != null && !node.getFlag().isBlank()) {
            return node.getFlag();
        }
        String seed = "cfs:" + node.getTopologyId() + ":" + node.getNodeCode();
        String body = FlagUtils.hmacSha256Hex(seed, String.valueOf(teamId)).substring(0, 24);
        return "flag{cfs_" + node.getLayerNo() + "_" + body + "}";
    }

    private int nextPort(int cursor) {
        int span = Math.max(1, PORT_END - PORT_START);
        int port = PORT_START + Math.floorMod(cursor, span);
        return port;
    }

    /** 编排计划。 */
    @Data
    @Builder
    public static class Plan {
        private int layerCount;
        private List<String> networks;
        private List<CfsNodeInstance> nodes;
        private int nextPortCursor;
    }
}