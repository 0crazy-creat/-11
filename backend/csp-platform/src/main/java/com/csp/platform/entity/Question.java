package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.csp.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 统一题库题目：理论 / CTF / 场景 / AWD 共用一张表，用 bankType 区分。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class Question extends BaseEntity {

    /** THEORY/CTF/SCENARIO/AWD */
    private String bankType;
    /** Web/Misc/Crypto/Reverse/Pwn */
    private String category;
    private String title;
    /** 题面 Markdown */
    private String content;
    /** 理论题标准答案 */
    private String answer;
    /** 静态 flag（CTF 类） */
    private String flag;
    private Integer score;
    /** EASY/MEDIUM/HARD/INSANE */
    private String difficulty;
    /** 知识点标签 JSON 数组字符串 */
    private String tags;
    private String attachment;
    private String writeup;
    /** 靶场镜像 */
    private String imageName;
    private String cpuLimit;
    private String memLimit;
    /** 1 启用 0 停用 */
    private Integer status;
    /** 赛制特有扩展 JSON */
    private String ext;
}