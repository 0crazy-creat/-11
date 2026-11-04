package com.csp.cfs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CFS 分层定义（cfs_layer）：层号、前置依赖层、递进基础分、独立网络段、是否暴露端口。
 */
@Data
@TableName("cfs_layer")
public class CfsLayer implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long topologyId;
    /** 1/2/3 */
    private Integer layerNo;
    /** LAYER1/LAYER2/LAYER3 */
    private String layerCode;
    private String name;
    /** 解锁本层所需前置层号，0=无需前置 */
    private Integer prevLayerNo;
    /** 本层 flag 基础分值（越深层越高） */
    private Integer baseScore;
    /** 层内节点递增加分步长 */
    private Integer scoreStep;
    /** 整层完成奖励分 */
    private Integer layerBonus;
    /** 本层独立网络段，如 172.31.1.0/24 */
    private String networkCidr;
    /** 1=对外暴露宿主端口（仅入口层） */
    private Integer exposePort;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}