package com.csp.ctf.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 榜单明细：总分/阶段榜数据 + 表头 + 解题矩阵（一血标识来源）。 */
@Data
@Builder
public class BoardDetailVO {
    private List<RankItem> ranks;
    private List<ChallengeMeta> challenges;
    private List<SolveCell> cells;
    private List<StageVO> stages;
    /** 榜单快照时间，前端展示“更新于” */
    private LocalDateTime snapshotAt;
    /** 数据来源：REDIS 实时榜 / DB 快照 */
    private String source;
}
