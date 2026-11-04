package com.csp.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 赛事归档快照视图。 */
@Data
public class ArchiveVO {
    private Long contestId;
    private String contestCode;
    private String contestName;
    private String contestType;
    private String status;
    private String filingNo;
    private String filingOrg;
    private String remark;
    /** 题目数 / 战队数 / 提交数 / 参赛人数 */
    private Long questionCount;
    private Long teamCount;
    private Long submissionCount;
    private Long playerCount;
    private String checksum;
    private LocalDateTime archivedAt;
    private Long archivedBy;
}
