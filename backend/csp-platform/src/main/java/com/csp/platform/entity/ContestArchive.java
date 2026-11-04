package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 赛事备案归档快照。
 * 与 contest 表解耦：归档后即使赛事主数据变更，快照仍保持不可变（只插入不更新）。
 */
@Data
@TableName("contest_archive")
public class ContestArchive implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long contestId;
    private String contestCode;
    private String contestName;
    private String contestType;
    private String contestStatus;
    /** 备案编号 */
    private String filingNo;
    private String filingOrg;
    private String remark;
    private Long questionCount;
    private Long teamCount;
    private Long submissionCount;
    private Long playerCount;
    /** 快照内容摘要（SHA-256），用于完整性校验 */
    private String checksum;
    /** 快照 JSON 全量内容 */
    private String snapshot;
    private LocalDateTime archivedAt;
    private Long archivedBy;
}
