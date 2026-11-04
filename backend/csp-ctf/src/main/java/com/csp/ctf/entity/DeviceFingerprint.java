package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 设备指纹：记录前端采集的设备特征，用于多账号检测。 */
@Data
@TableName("device_fingerprint")
public class DeviceFingerprint implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 前端生成的设备唯一标识 */
    private String deviceId;
    
    /** 关联用户 */
    private Long userId;
    
    /** 关联赛事 */
    private Long contestId;
    
    /** 设备特征（JSON格式：浏览器、屏幕、字体、Canvas等） */
    private String fingerprint;
    
    /** 采集时IP */
    private String ipAddress;
    
    /** 浏览器UA */
    private String userAgent;
    
    /** 首次出现时间 */
    private LocalDateTime firstSeenAt;
    
    /** 最后出现时间 */
    private LocalDateTime lastSeenAt;
    
    /** 信任分（0-100，低于60标记为可疑） */
    private Integer trustScore;
    
    /** 风险标记（MULTI_ACCOUNT,VPN,SPOOFED等） */
    private String riskFlags;
}
