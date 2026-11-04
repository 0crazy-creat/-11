package com.csp.infra.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/** 提交明细仓储。 */
public interface SubmissionLogRepository extends MongoRepository<SubmissionLogDocument, String> {
    List<SubmissionLogDocument> findByContestIdAndUserIdOrderByCreatedAtDesc(Long contestId, Long userId);
    long countByContestIdAndQuestionIdAndCorrectTrue(Long contestId, Long questionId);
}
