-- 练习模式提交记录（与赛事 submission 隔离）
CREATE TABLE IF NOT EXISTS practice_submission (
    id          BIGINT       NOT NULL COMMENT '主键（雪花）',
    user_id     BIGINT       NOT NULL COMMENT '提交用户',
    question_id BIGINT       NOT NULL COMMENT '题目 ID',
    correct     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否正确',
    score       INT          NOT NULL DEFAULT 0 COMMENT '本次得分（重复解出记 0）',
    content     VARCHAR(512)          DEFAULT NULL COMMENT '提交内容',
    ip          VARCHAR(64)           DEFAULT NULL COMMENT '来源 IP',
    created_by  BIGINT                DEFAULT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  BIGINT                DEFAULT NULL,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_question (question_id),
    KEY idx_user_correct (user_id, correct)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='练习模式提交记录';
