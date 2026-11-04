package com.csp.cfs.guard;

import com.csp.cfs.entity.CfsInstance;
import com.csp.cfs.entity.CfsLayer;
import com.csp.cfs.entity.CfsNode;
import com.csp.cfs.enums.NetworkLayer;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CFS 解锁校验（拓扑约束核心）。
 *
 * <p>规则：必须拿下外层 flag 才解锁内层靶机访问——内层节点对未解锁队伍完全不可见/不可访问，
 * 提交内层 flag 时直接拒绝（{@code FORBIDDEN}），从根上杜绝“跳关”。</p>
 *
 * <p>解锁判定以实例的 {@code unlockedLayer} 为准：入口层（LAYER1）创建实例时即解锁，
 * 之后每次成功拿下当前层任意节点 flag，解锁层推进到 {@code layerNo + 1}（若存在且尚未解锁）。</p>
 */
@Slf4j
@Component
public class CfsUnlockGuard {

    /** 入口层号：创建实例即为解锁态。 */
    public static final int ENTRY_LAYER = 1;

    /**
     * 新实例的初始解锁层（入口层天然解锁）。
     */
    public int initialUnlockedLayer(int layerCount) {
        return Math.min(ENTRY_LAYER, Math.max(1, layerCount));
    }

    /**
     * 校验某层是否已对队伍解锁。
     *
     * @return true 表示可访问（可提交该层 flag）
     */
    public boolean isLayerUnlocked(CfsInstance instance, int layerNo) {
        if (instance == null) return false;
        Integer unlocked = instance.getUnlockedLayer();
        int u = unlocked == null ? initialUnlockedLayer(layerNo) : unlocked;
        return layerNo <= u;
    }

    /**
     * 断言层已解锁，否则抛出带提示的业务异常。
     *
     * @param node 待访问/提交的节点（取其 layerNo）
     */
    public void requireLayerUnlocked(CfsInstance instance, CfsNode node) {
        if (node == null) throw BizException.notFound("CFS 节点");
        int layerNo = node.getLayerNo() == null ? 1 : node.getLayerNo();
        if (isLayerUnlocked(instance, layerNo)) return;

        NetworkLayer layer = NetworkLayer.ofNo(layerNo);
        int prev = layer.getPrevNo();
        throw new BizException(ResultCode.FORBIDDEN,
                "LAYER" + layerNo + "（" + layer.getLabel() + "）尚未解锁：需先攻破 LAYER" + prev
                        + "（" + NetworkLayer.ofNo(prev).getLabel() + "）任意一处 flag");
    }

    /**
     * 依据“已解出层集合”重新计算解锁层（幂等、可重放）。
     *
     * <p>从入口层开始逐层推进：只要当前层已解出，就尝试解锁下一层；
     * 一旦某层未解出则停止（不允许越层解锁）。</p>
     *
     * @param solvedLayerNos 已解出的层号集合
     * @param layerCount     拓扑总层数
     * @return 新的最高解锁层
     */
    public int recomputeUnlockedLayer(java.util.Set<Integer> solvedLayerNos, int layerCount) {
        int max = Math.max(1, layerCount);
        int unlocked = initialUnlockedLayer(max);
        for (int no = 1; no <= max; no++) {
            boolean solved = solvedLayerNos != null && solvedLayerNos.contains(no);
            if (!solved) break;
            if (no + 1 <= max) unlocked = no + 1;
        }
        return Math.min(unlocked, max);
    }

    /**
     * 层定义完整性校验：层号连续、入口唯一、前置链正确、分值随深度递增。
     */
    public void validateLayerDefs(List<CfsLayer> layers) {
        if (layers == null || layers.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "CFS 拓扑至少需要一层定义");
        }
        if (layers.size() > 3) {
            throw new BizException(ResultCode.BAD_REQUEST, "CFS 拓扑最多 3 层（LAYER1/LAYER2/LAYER3）");
        }
        int expect = 1;
        Integer prevBase = null;
        int exposeCount = 0;
        for (CfsLayer l : layers.stream().sorted(java.util.Comparator.comparing(CfsLayer::getLayerNo)).toList()) {
            if (l.getLayerNo() == null || l.getLayerNo() != expect) {
                throw new BizException(ResultCode.BAD_REQUEST,
                        "层号必须从 1 连续递增，期望 " + expect + "，实际 " + l.getLayerNo());
            }
            if (NetworkLayer.ofCode(l.getLayerCode()).getNo() != l.getLayerNo()) {
                throw new BizException(ResultCode.BAD_REQUEST, "层编码与层号不一致：" + l.getLayerCode());
            }
            int wantPrev = expect - 1;
            int gotPrev = l.getPrevLayerNo() == null ? 0 : l.getPrevLayerNo();
            if (gotPrev != wantPrev) {
                throw new BizException(ResultCode.BAD_REQUEST,
                        "LAYER" + expect + " 的前置层应为 " + wantPrev + "，实际 " + gotPrev);
            }
            if (prevBase != null && (l.getBaseScore() == null || l.getBaseScore() <= prevBase)) {
                throw new BizException(ResultCode.BAD_REQUEST,
                        "递进得分要求越深层分值越高：LAYER" + expect + " 分值须大于 LAYER" + (expect - 1));
            }
            if (l.getExposePort() != null && l.getExposePort() == 1) exposeCount++;
            prevBase = l.getBaseScore();
            expect++;
        }
        if (exposeCount != 1) {
            throw new BizException(ResultCode.BAD_REQUEST,
                    "必须且只能有一层对外暴露端口（入口层 LAYER1），当前暴露层数=" + exposeCount);
        }
        CfsLayer entry = layers.stream().filter(x -> x.getLayerNo() == 1).findFirst().orElse(null);
        if (entry == null || entry.getExposePort() == null || entry.getExposePort() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "入口层 LAYER1 必须设置 exposePort=1");
        }
    }

    /**
     * 校验节点：层号存在、入口机只在入口层、层内入口唯一。
     */
    public void validateNodes(List<CfsNode> nodes, int layerCount) {
        if (nodes == null) return;
        java.util.Map<Integer, Integer> entryPerLayer = new java.util.HashMap<>();
        for (CfsNode n : nodes) {
            int no = n.getLayerNo() == null ? 0 : n.getLayerNo();
            if (no < 1 || no > layerCount) {
                throw new BizException(ResultCode.BAD_REQUEST, "节点 " + n.getNodeCode() + " 层号越界：" + no);
            }
            if (n.getIsEntry() != null && n.getIsEntry() == 1) {
                if (no != ENTRY_LAYER) {
                    throw new BizException(ResultCode.BAD_REQUEST,
                            "只有入口层 LAYER1 可设置入口机，节点 " + n.getNodeCode() + " 位于 LAYER" + no);
                }
                entryPerLayer.merge(no, 1, Integer::sum);
            }
        }
        entryPerLayer.forEach((no, cnt) -> {
            if (cnt > 1) {
                throw new BizException(ResultCode.BAD_REQUEST, "LAYER" + no + " 入口机唯一，当前 " + cnt + " 个");
            }
        });
    }
}