package com.csp.platform.dto;

import com.csp.platform.entity.Question;
import lombok.Data;

/** 赛事题目视图：关联信息 + 题目摘要。 */
@Data
public class ContestQuestionVO {
    private Long bindId;
    private Long questionId;
    private Integer sortNo;
    private Integer score;
    private Integer visible;
    private String title;
    private String bankType;
    private String category;
    private String difficulty;

    public static ContestQuestionVO of(com.csp.ctf.entity.ContestQuestion cq, Question q) {
        ContestQuestionVO v = new ContestQuestionVO();
        v.bindId = cq.getId();
        v.questionId = cq.getQuestionId();
        v.sortNo = cq.getSortNo();
        v.score = cq.getScore() != null ? cq.getScore() : (q != null ? q.getScore() : null);
        v.visible = cq.getVisible();
        if (q != null) {
            v.title = q.getTitle();
            v.bankType = q.getBankType();
            v.category = q.getCategory();
            v.difficulty = q.getDifficulty();
        }
        return v;
    }
}
