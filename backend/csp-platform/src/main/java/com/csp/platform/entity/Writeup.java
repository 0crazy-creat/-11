package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.csp.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 题解（Writeup）：选手提交解题思路，审核通过后对其他选手可见。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("writeup")
public class Writeup extends BaseEntity {

    private Long contestId;
    private Long questionId;
    private Long teamId;
    private Long userId;
    private String title;
    /** Markdown 正文 */
    private String content;
    /** PENDING / APPROVED / REJECTED */
    private String status;
    private String reviewRemark;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private Integer viewCount;
    private Integer likeCount;
}
