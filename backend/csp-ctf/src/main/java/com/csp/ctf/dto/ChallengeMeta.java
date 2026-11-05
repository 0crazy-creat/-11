package com.csp.ctf.dto;

import lombok.Data;

/** 榜单表头：赛事题目元信息 + 解题热度。 */
@Data
public class ChallengeMeta {
    private Long questionId;
    private String title;
    private String category;
    private String difficulty;
    /** 赛事配置分值（未配置时回退题库分值） */
    private Integer score;
    private Integer sortNo;
    /** 已解出队伍数 */
    private Integer solvedCount;
    /** 参赛队伍数，用于正确率 */
    private Integer totalCount;
}
