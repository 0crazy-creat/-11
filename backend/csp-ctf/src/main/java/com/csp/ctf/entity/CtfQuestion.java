package com.csp.ctf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.csp.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 统一题库题目（question）在 CTF 子系统内的只读映射。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class CtfQuestion extends BaseEntity {

    /** THEORY/CTF/SCENARIO/AWD */
    private String bankType;
    private String category;
    private String title;
    private String content;
    /** 理论题标准答案 */
    private String answer;
    /** 静态 flag */
    private String flag;
    private Integer score;
    private String difficulty;
    private String tags;
    private String attachment;
    private String imageName;
    private Integer status;
    private String ext;
}
