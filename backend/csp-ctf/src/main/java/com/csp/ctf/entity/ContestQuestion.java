package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 赛事题目关联（contest_question）。 */
@Data
@TableName("contest_question")
public class ContestQuestion implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contestId;
    private Long questionId;
    private Integer sortNo;
    /** 覆盖原题分数，为空则用题目默认分 */
    private Integer score;
    /** 1 可见 0 隐藏 */
    private Integer visible;
}
