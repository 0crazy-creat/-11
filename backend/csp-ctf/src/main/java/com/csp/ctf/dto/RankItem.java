package com.csp.ctf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 排行榜条目。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankItem {
    private int rank;
    private Long teamId;
    private String teamName;
    private long score;
    private int solved;
    private LocalDateTime lastSolveAt;

    /** 是否获得过一血（该赛事内任一道题的首个正确提交）。 */
    private Boolean firstBlood;
}
