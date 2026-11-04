package com.csp.cfs.engine;

import com.csp.cfs.entity.CfsLayer;
import com.csp.cfs.entity.CfsNode;
import com.csp.cfs.entity.CfsNodeInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 分层网络编排 + 递进得分单测。 */
class CfsNetworkOrchestratorTest {

    private final CfsNetworkOrchestrator orchestrator = new CfsNetworkOrchestrator();
    private final CfsScorePolicy scorePolicy = new CfsScorePolicy();

    @Test
    @DisplayName("每层独立网络，层内节点网络名一致、跨层不同")
    void perLayerNetworkIsolation() {
        List<CfsLayer> layers = List.of(layer(1, 100, 1), layer(2, 300, 0), layer(3, 600, 0));
        List<CfsNode> nodes = List.of(node(1L, 1, "web", 1), node(2L, 2, "db", 0), node(3L, 3, "core", 0));

        var plan = orchestrator.plan(9L, 7L, layers, nodes, 0);

        assertEquals(3, plan.getNetworks().size());
        assertEquals(3, plan.getNetworks().stream().distinct().count());
        assertTrue(plan.getNetworks().get(0).endsWith("-l1"));
        assertTrue(plan.getNetworks().get(2).endsWith("-l3"));

        Map<Integer, CfsNodeInstance> byLayer = plan.getNodes().stream()
                .collect(java.util.stream.Collectors.toMap(CfsNodeInstance::getLayerNo, x -> x));
        assertEquals("csp-cfs-9-7-l1", byLayer.get(1).getNetworkName());
        assertEquals("csp-cfs-9-7-l2", byLayer.get(2).getNetworkName());
    }

    @Test
    @DisplayName("仅入口层入口机暴露宿主端口，内层无宿主端口")
    void onlyEntryExposesHostPort() {
        List<CfsLayer> layers = List.of(layer(1, 100, 1), layer(2, 300, 0), layer(3, 600, 0));
        List<CfsNode> nodes = List.of(node(1L, 1, "web", 1), node(2L, 2, "db", 0), node(3L, 3, "core", 1));

        var plan = orchestrator.plan(9L, 7L, layers, nodes, 0);
        for (CfsNodeInstance ni : plan.getNodes()) {
            if (ni.getLayerNo() == 1) {
                assertNotNull(ni.getHostPort(), "入口层应暴露端口");
                assertTrue(ni.getHostPort() >= CfsNetworkOrchestrator.PORT_START);
            } else {
                assertNull(ni.getHostPort(), "内层不得暴露宿主端口: LAYER" + ni.getLayerNo());
                assertNull(ni.getHost(), "内层不得给出宿主地址");
            }
            assertNotNull(ni.getInnerIp(), "每个节点都有层内 IP");
        }
    }

    @Test
    @DisplayName("动态 flag 同队稳定、跨队不同、跨层前缀不同")
    void dynamicFlagIsTeamScoped() {
        CfsNode n = node(5L, 2, "db", 0);
        n.setTopologyId(3L);
        String a = orchestrator.resolveFlag(n, 100L);
        String b = orchestrator.resolveFlag(n, 100L);
        String c = orchestrator.resolveFlag(n, 200L);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertTrue(a.startsWith("flag{cfs_2_"));
    }

    @Test
    @DisplayName("递进得分：越深层分值越高，入口机有加成")
    void progressiveScoring() {
        CfsLayer l1 = layer(1, 100, 1);
        CfsLayer l2 = layer(2, 300, 0);
        CfsLayer l3 = layer(3, 600, 0);

        CfsNode inner1 = node(1L, 1, "web", 0);
        inner1.setScore(100);
        CfsNode entry1 = node(2L, 1, "entry", 1);
        entry1.setScore(100);
        CfsNode n2 = node(3L, 2, "db", 0);
        n2.setScore(300);
        CfsNode n3 = node(4L, 3, "core", 0);
        n3.setScore(600);

        int s1 = scorePolicy.scoreOf(inner1, l1);
        int s2 = scorePolicy.scoreOf(n2, l2);
        int s3 = scorePolicy.scoreOf(n3, l3);
        assertTrue(s1 < s2 && s2 < s3, "分值须严格递增: " + s1 + "/" + s2 + "/" + s3);
        assertTrue(scorePolicy.scoreOf(entry1, l1) > s1, "入口机应有加成");
        assertTrue(scorePolicy.strictlyAscending(List.of(l1, l2, l3)));
        // 入参乱序不影响判定（内部按层号排序）
        assertTrue(scorePolicy.strictlyAscending(List.of(l3, l1, l2)));
        // 真正倒挂的分值必须被判为非法
        CfsLayer flat = layer(2, 300, 0);
        CfsLayer high = layer(1, 900, 1);
        assertFalse(scorePolicy.strictlyAscending(List.of(high, flat)));
    }

    private CfsLayer layer(int no, int base, int expose) {
        CfsLayer l = new CfsLayer();
        l.setLayerNo(no);
        l.setBaseScore(base);
        l.setScoreStep(50);
        l.setExposePort(expose);
        return l;
    }

    private CfsNode node(Long id, int layerNo, String code, int entry) {
        CfsNode n = new CfsNode();
        n.setId(id);
        n.setTopologyId(3L);
        n.setLayerNo(layerNo);
        n.setNodeCode(code);
        n.setIsEntry(entry);
        n.setDynamicFlag(1);
        return n;
    }
}