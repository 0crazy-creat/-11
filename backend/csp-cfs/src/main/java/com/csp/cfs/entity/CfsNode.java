package com.csp.cfs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** CFS 层内靶机节点（cfs_node），复用统一题库 question 作为任务/靶场。 */
@Data
@TableName("cfs_node")
public class CfsNode implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long topologyId;
    private Long layerId;
    private Integer layerNo;
    private String nodeCode;
    private String name;
    /** 关联题库题目（允许跨 bank_type） */
    private Long questionId;
    /** 前置任务节点ID（非空则必须先完成该任务才可提交） */
    private Long preTaskId;
    private String imageName;
    private Integer containerPort;
    /** 1=本层入口机 */
    private Integer isEntry;
    /** 本节点 flag 分值 */
    private Integer score;
    private String flag;
    private Integer dynamicFlag;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}