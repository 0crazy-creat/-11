package com.csp.awd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** AWD 流量捕获记录。 */
@Data
@TableName("awd_traffic_capture")
public class AwdTrafficCapture implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 赛事ID */
    private Long contestId;
    
    /** 轮次 */
    private Integer roundNo;
    
    /** 战队ID */
    private Long teamId;
    
    /** 靶机ID */
    private Long targetId;
    
    /** 捕获文件路径（MinIO对象名） */
    private String captureFile;
    
    /** 文件大小（字节） */
    private Long fileSize;
    
    /** 捕获时长（秒） */
    private Integer duration;
    
    /** 数据包数量 */
    private Integer packetCount;
    
    /** 协议统计（JSON） */
    private String protocolStats;
    
    /** 存储类型（MINIO/LOCAL/NAS） */
    private String storageType;
    
    /** 状态（CAPTURING/COMPLETED/FAILED/EXPIRED） */
    private String status;
    
    /** 错误信息 */
    private String errorMessage;
    
    /** 开始时间 */
    private LocalDateTime startedAt;
    
    /** 完成时间 */
    private LocalDateTime completedAt;
    
    /** 过期时间 */
    private LocalDateTime expiresAt;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
