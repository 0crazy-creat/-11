package com.csp.cfs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** CFS 层解锁审计（cfs_unlock_log）。 */
@Data
@TableName("cfs_unlock_log")
public class CfsUnlockLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contestId;
    private Long teamId;
    private Long nodeId;
    /** 被拿下的层号 */
    private Integer layerNo;
    /** 本次操作后解锁到的最高层号 */
    private Integer unlocked;
    private Integer score;
    private LocalDateTime createdAt;
}