package com.csp.cfs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** CFS 队伍拓扑实例（cfs_instance）：记录解锁进度与累计得分。 */
@Data
@TableName("cfs_instance")
public class CfsInstance implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contestId;
    private Long teamId;
    private Long topologyId;
    /** CREATING/READY/STOPPED/ERROR */
    private String status;
    /** 已解锁的最高层号（入口层天然解锁，初始 1） */
    private Integer unlockedLayer;
    /** 已解出层号集合，形如 ",1,2," */
    private String solvedLayers;
    private Long totalScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}