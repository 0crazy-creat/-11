package com.csp.cfs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** CFS 分层拓扑模板（cfs_topology）。 */
@Data
@TableName("cfs_topology")
public class CfsTopology implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 所属赛事（NULL=通用模板） */
    private Long contestId;
    private String code;
    private String name;
    private String description;
    /** 层数（1-3） */
    private Integer layerCount;
    /** DRAFT/PUBLISHED */
    private String status;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}