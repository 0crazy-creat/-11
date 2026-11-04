package com.csp.ctf.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 题目视图（管理端可见 flag；参赛端不应使用此 VO）。 */
@Data
@Builder
public class ChallengeVO {
    private Long id;
    private Long contestId;
    private String title;
    private String category;
    private String difficulty;
    private Integer score;
    private String flag;
    private String description;
    private String attachmentUrl;
    private String tags;
    private String answer;
    private String bankType;
    private Integer status;
    private Integer visible;
    private Integer sortNo;
    private LocalDateTime createdAt;
}
