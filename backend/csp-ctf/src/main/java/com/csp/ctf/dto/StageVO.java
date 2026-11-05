package com.csp.ctf.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 赛阶段：按比赛时间窗自动切片的回放锚点。
 * <p>endAt 即该阶段的 asOf 时刻，阶段榜 = 截至 endAt 的累计榜。</p>
 */
@Data
@Builder
public class StageVO {
    /** 1-based 序号 */
    private int index;
    private String label;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    /** 阶段内的正确解题次数 */
    private int solves;
    /** 是否为进行中的阶段 */
    private boolean current;
}
