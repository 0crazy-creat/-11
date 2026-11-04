package com.csp.situation.dto;

import com.csp.awd.dto.TargetVO;
import com.csp.ctf.dto.RankItem;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** 3D 态势大屏一次性数据包（单请求渲染，避免大屏轮询多次抖动）。 */
@Data
@Builder
public class ScreenVO {
    private OverviewVO overview;
    private List<RankItem> top;
    /** 得分分布：分数区间 -> 队伍数 */
    private Map<String, Long> scoreDistribution;
    /** 靶机监控（全赛事） */
    private List<TargetVO> targets;
    private long serverTime;
}
