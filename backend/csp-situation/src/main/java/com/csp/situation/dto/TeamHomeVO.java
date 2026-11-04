package com.csp.situation.dto;

import com.csp.awd.dto.TargetVO;
import com.csp.ctf.dto.RankItem;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/** 战队主页：队伍档案 + 排名 + 靶机状态。 */
@Data
@Builder
public class TeamHomeVO {
    private Long teamId;
    private String teamName;
    private String orgCode;
    private String logo;
    /** 参赛状态 PENDING/APPROVED/REJECTED/QUIT */
    private String status;
    private int members;
    /** 当前名次（未上榜为 0） */
    private int rank;
    private long score;
    private int solved;
    /** 附近名次（含本队，最多 5 条） */
    private List<RankItem> nearby;
    /** 本队靶机 */
    private List<TargetVO> targets;
}
