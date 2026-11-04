package com.csp.cfs.engine;

import com.csp.cfs.entity.CfsLayer;
import com.csp.cfs.entity.CfsNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CFS 递进得分策略：越深层分值越高，且层内命中入口机额外加成。
 *
 * <p>分值来源优先级：节点自定义 score > 层 baseScore + 层内序位 × scoreStep。
 * 严格保证 {@code score(LAYER_n) > score(LAYER_{n-1})}（由拓扑校验兜底）。</p>
 */
@Component
@RequiredArgsConstructor
public class CfsScorePolicy {

    /** 入口机额外奖励系数（拿下入口即打通本层关键路径）。 */
    private static final double ENTRY_BONUS = 0.1;

    /**
     * 计算节点 flag 得分。
     */
    public int scoreOf(CfsNode node, CfsLayer layer) {
        int base;
        if (node.getScore() != null && node.getScore() > 0) {
            base = node.getScore();
        } else if (layer != null) {
            int bs = layer.getBaseScore() == null ? 100 : layer.getBaseScore();
            base = bs;
        } else {
            int no = node.getLayerNo() == null ? 1 : node.getLayerNo();
            base = 100 * no;
        }
        if (node.getIsEntry() != null && node.getIsEntry() == 1) {
            base = base + (int) Math.round(base * ENTRY_BONUS);
        }
        return base;
    }

    /** 校验层分值随深度递增（供单测/防御性断言）。 */
    public boolean strictlyAscending(List<CfsLayer> layers) {
        Integer prev = null;
        for (CfsLayer l : layers.stream().sorted(java.util.Comparator.comparing(CfsLayer::getLayerNo)).toList()) {
            int s = l.getBaseScore() == null ? 0 : l.getBaseScore();
            if (prev != null && s <= prev) return false;
            prev = s;
        }
        return true;
    }
}