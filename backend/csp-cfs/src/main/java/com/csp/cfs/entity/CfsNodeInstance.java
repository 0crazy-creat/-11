package com.csp.cfs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** CFS 节点运行实例（cfs_node_instance）：队伍 × 靶机容器的实际信息与 flag。 */
@Data
@TableName("cfs_node_instance")
public class CfsNodeInstance implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long instanceId;
    private Long contestId;
    private Long teamId;
    private Long nodeId;
    private Integer layerNo;
    private String containerId;
    private String containerName;
    private String host;
    /** 仅入口节点有值 */
    private Integer hostPort;
    /** 层内网 IP，仅同层可达 */
    private String innerIp;
    private String networkName;
    /** PENDING/RUNNING/STOPPED/ERROR */
    private String status;
    /** 该队该节点的实际 flag（动态派生） */
    private String flag;
    private Integer solved;
    private LocalDateTime solvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}