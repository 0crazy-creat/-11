package com.csp.ctf.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** 分数趋势：x 轴为阶段标签，每条 trace 为一支战队的累计得分。 */
@Data
@Builder
public class TimelineVO {
    private List<String> labels;
    private List<Trace> traces;

    @Data
    @Builder
    public static class Trace {
        private Long teamId;
        private String teamName;
        private List<Long> scores;
    }
}
