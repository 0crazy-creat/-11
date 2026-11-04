package com.csp.cfs.guard;

import com.csp.cfs.entity.CfsInstance;
import com.csp.cfs.entity.CfsLayer;
import com.csp.cfs.entity.CfsNode;
import com.csp.cfs.enums.NetworkLayer;
import com.csp.common.exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 解锁校验单测：覆盖前置依赖、逐层推进、跳关拒绝、层定义合法性。
 */
class CfsUnlockGuardTest {

    private final CfsUnlockGuard guard = new CfsUnlockGuard();

    private CfsInstance instance(int unlocked) {
        CfsInstance i = new CfsInstance();
        i.setUnlockedLayer(unlocked);
        i.setSolvedLayers(",");
        return i;
    }

    private CfsNode node(int layerNo) {
        CfsNode n = new CfsNode();
        n.setLayerNo(layerNo);
        n.setNodeCode("n" + layerNo);
        return n;
    }

    @Test
    @DisplayName("入口层天然解锁，内层初始锁定")
    void entryUnlockedInnerLocked() {
        CfsInstance i = instance(1);
        assertTrue(guard.isLayerUnlocked(i, 1));
        assertFalse(guard.isLayerUnlocked(i, 2));
        assertFalse(guard.isLayerUnlocked(i, 3));
    }

    @Test
    @DisplayName("跳关提交内层 flag 被拒绝（FORBIDDEN）")
    void cannotSkipLayer() {
        CfsInstance i = instance(1);
        BizException ex = assertThrows(BizException.class, () -> guard.requireLayerUnlocked(i, node(3)));
        assertEquals(403, ex.getCode());
        assertTrue(ex.getMessage().contains("LAYER3"));
        assertTrue(ex.getMessage().contains("LAYER2"));
    }

    @Test
    @DisplayName("拿下 LAYER1 后解锁 LAYER2，但 LAYER3 仍锁定")
    void sequentialUnlock() {
        assertEquals(2, guard.recomputeUnlockedLayer(Set.of(1), 3));
        assertEquals(3, guard.recomputeUnlockedLayer(Set.of(1, 2), 3));
    }

    @Test
    @DisplayName("越层解出集合也不能越权解锁")
    void noSkipInRecompute() {
        // 只解出了 1 和 3（3 本不可能），2 未解 → 最高只能到 2
        assertEquals(2, guard.recomputeUnlockedLayer(Set.of(1, 3), 3));
        // 只有 2、3 而无 1 → 停在入口层
        assertEquals(1, guard.recomputeUnlockedLayer(Set.of(2, 3), 3));
    }

    @Test
    @DisplayName("层定义：仅入口层暴露端口，且分值随深度递增")
    void validLayerDefs() {
        List<CfsLayer> layers = List.of(
                layer(1, 0, 100, 1),
                layer(2, 1, 300, 0),
                layer(3, 2, 600, 0));
        assertDoesNotThrow(() -> guard.validateLayerDefs(layers));
    }

    @Test
    @DisplayName("层定义：内层暴露端口或分值倒挂被拒绝")
    void invalidLayerDefs() {
        List<CfsLayer> exposeInner = List.of(
                layer(1, 0, 100, 1),
                layer(2, 1, 300, 1));
        assertThrows(BizException.class, () -> guard.validateLayerDefs(exposeInner));

        List<CfsLayer> descending = List.of(
                layer(1, 0, 500, 1),
                layer(2, 1, 300, 0));
        assertThrows(BizException.class, () -> guard.validateLayerDefs(descending));

        List<CfsLayer> wrongPrev = List.of(
                layer(1, 0, 100, 1),
                layer(2, 0, 300, 0));
        assertThrows(BizException.class, () -> guard.validateLayerDefs(wrongPrev));
    }

    @Test
    @DisplayName("节点：只有 LAYER1 可设入口机")
    void entryOnlyOnLayer1() {
        CfsNode inner = new CfsNode();
        inner.setNodeCode("inner");
        inner.setLayerNo(2);
        inner.setIsEntry(1);
        assertThrows(BizException.class, () -> guard.validateNodes(List.of(inner), 3));
    }

    @Test
    @DisplayName("网络层枚举映射")
    void layerEnum() {
        assertEquals(1, NetworkLayer.ofCode("layer1").getNo());
        assertEquals(0, NetworkLayer.LAYER1.getPrevNo());
        assertEquals(2, NetworkLayer.LAYER3.getPrevNo());
        assertThrows(IllegalArgumentException.class, () -> NetworkLayer.ofNo(4));
    }

    private CfsLayer layer(int no, int prev, int base, int expose) {
        CfsLayer l = new CfsLayer();
        l.setLayerNo(no);
        l.setLayerCode(NetworkLayer.ofNo(no).name());
        l.setPrevLayerNo(prev);
        l.setBaseScore(base);
        l.setScoreStep(50);
        l.setExposePort(expose);
        return l;
    }
}