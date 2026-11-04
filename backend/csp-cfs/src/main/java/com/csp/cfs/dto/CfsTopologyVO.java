package com.csp.cfs.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** CFS 拓扑视图：逐层展示解锁状态与节点可达性（供前端拓扑图渲染）。 */
@Data
@Builder
public class CfsTopologyVO {

    private Long contestId;
    private Long teamId;
    private Long instanceId;
    private Long topologyId;
    private String topologyName;
    /** 当前已解锁的最高层号 */
    private Integer unlockedLayer;
    /** 已解出层号 */
    private List<Integer> solvedLayers;
    private Long totalScore;
    private List<LayerVO> layers;

    /** 单层视图。 */
    @Data
    @Builder
    public static class LayerVO {
        private Long layerId;
        private Integer layerNo;
        private String layerCode;
        private String name;
        /** 前置层号，0=无 */
        private Integer prevLayerNo;
        private Integer baseScore;
        private String networkCidr;
        private Integer exposePort;
        /** 是否已解锁（可访问） */
        private Boolean unlocked;
        /** 是否已解出（本层任一 flag 已拿下） */
        private Boolean solved;
        /** 未解锁时给出提示，如“需先攻破 LAYER1” */
        private String lockedReason;
        private List<NodeVO> nodes;
    }

    /** 单节点视图。 */
    @Data
    @Builder
    public static class NodeVO {
        private Long nodeId;
        private Long nodeInstanceId;
        private String nodeCode;
        private String name;
        private Integer layerNo;
        private Long questionId;
        private Integer isEntry;
        private Integer score;
        private Boolean solved;
        /** 已解锁时给出访问地址（仅入口节点有 hostPort） */
        private String host;
        private Integer hostPort;
        private String innerIp;
        /** 仅本层可达：内层节点不给宿主地址 */
        private Boolean reachable;
        private String status;
    }
}