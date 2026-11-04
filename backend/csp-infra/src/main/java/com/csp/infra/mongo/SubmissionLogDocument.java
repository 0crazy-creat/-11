package com.csp.infra.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 非结构化提交记录（MongoDB）：存原始提交内容、代码、附件，便于取证与回溯。
 */
@Data
@Document(collection = "submission_log")
@CompoundIndex(name = "idx_contest_user_time", def = "{'contestId':1,'userId':1,'createdAt':-1}")
public class SubmissionLogDocument {

    @Id
    private String id;

    @Indexed
    private Long contestId;
    @Indexed
    private Long userId;
    private Long teamId;
    private Long questionId;

    private String submitType;
    /** 原始提交内容（可能包含代码，MySQL 只存归一化摘要） */
    private String rawContent;
    private Boolean correct;
    private Integer score;
    private Integer roundNo;
    private String ip;
    private String deviceId;
    private Long elapsedMs;
    private Map<String, Object> ext;

    @Indexed
    private LocalDateTime createdAt;
}
