package com.csp.awd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** AWD 流量分析结果。 */
@Data
@TableName("awd_traffic_analysis")
public class AwdTrafficAnalysis implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 关联的捕获记录ID */
    private Long captureId;
    
    /** 分析类型（ATTACK_DETECT,VULN_SCAN,ANOMALY_DETECT） */
    private String analysisType;
    
    /** 严重程度（INFO,LOW,MEDIUM,HIGH,CRITICAL） */
    private String severity;
    
    /** 发现结果（JSON数组） */
    private String findings;
    
    /** 分析摘要 */
    private String summary;
    
    /** 置信度（0-1） */
    private BigDecimal confidence;
    
    /** 分析器（规则引擎/ML模型） */
    private String analyzer;
    
    /** 分析时间 */
    private LocalDateTime analyzedAt;
}
