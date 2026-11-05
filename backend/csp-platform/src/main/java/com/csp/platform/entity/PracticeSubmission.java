package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.csp.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 练习模式提交记录（与赛事 submission 隔离）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("practice_submission")
public class PracticeSubmission extends BaseEntity {

    private Long userId;
    private Long questionId;
    /** 1 正确 / 0 错误 */
    private Integer correct;
    /** 本次得分，重复解出记 0 */
    private Integer score;
    private String content;
    private String ip;
}
