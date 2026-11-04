package com.csp.arena.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/** 靶场总览统计。 */
@Data
@Builder
public class StatsVO {
    private long templateCount;
    private long instanceCount;
    private long runningCount;
    private long stoppedCount;
    private long errorCount;
    private long healthyCount;
    /** 分类 -> 实例数 */
    private Map<String, Long> categoryDistribution;
    /** 状态 -> 数量 */
    private Map<String, Long> statusDistribution;
    private long totalCpuPercent;
    private double totalMemMb;
}
