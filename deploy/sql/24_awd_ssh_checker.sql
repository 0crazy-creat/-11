-- AWD：靶机 SSH 连接信息 + 服务可用性 + checker 配置

ALTER TABLE awd_target
    ADD COLUMN ssh_port       INT          DEFAULT NULL COMMENT 'SSH 宿主端口',
    ADD COLUMN ssh_user       VARCHAR(32)  DEFAULT 'root' COMMENT 'SSH 用户',
    ADD COLUMN ssh_password   VARCHAR(64)  DEFAULT NULL COMMENT 'SSH 密码',
    ADD COLUMN service_ok     TINYINT      DEFAULT NULL COMMENT '服务检测结果 1正常 0异常',
    ADD COLUMN last_check_msg VARCHAR(255) DEFAULT NULL COMMENT '最近一次检测信息';

CREATE TABLE IF NOT EXISTS awd_checker (
    id           BIGINT       NOT NULL COMMENT '主键（雪花）',
    contest_id   BIGINT       NOT NULL COMMENT '赛事 ID',
    question_id  BIGINT       NOT NULL COMMENT '题目 ID',
    name         VARCHAR(64)  NOT NULL DEFAULT 'default' COMMENT 'checker 名称',
    check_type   VARCHAR(16)  NOT NULL DEFAULT 'HTTP' COMMENT 'HTTP / TCP',
    target_path  VARCHAR(128)          DEFAULT '/' COMMENT 'HTTP 检测路径',
    expect_status INT                  DEFAULT 200 COMMENT '期望 HTTP 状态码',
    timeout_ms   INT          NOT NULL DEFAULT 3000 COMMENT '超时毫秒',
    enabled      TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_by   BIGINT                DEFAULT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   BIGINT                DEFAULT NULL,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_contest (contest_id),
    KEY idx_question (question_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AWD checker 配置';
