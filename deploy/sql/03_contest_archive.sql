-- ============================================================
-- 赛事备案归档快照表（只追加，不可更新/删除）
-- ============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS contest_archive (
  id               BIGINT       NOT NULL,
  contest_id       BIGINT       NOT NULL,
  contest_code     VARCHAR(64)  NOT NULL,
  contest_name     VARCHAR(255) NOT NULL,
  contest_type     VARCHAR(32)  NOT NULL,
  contest_status   VARCHAR(32)  NOT NULL,
  filing_no        VARCHAR(128) NOT NULL COMMENT '备案编号',
  filing_org       VARCHAR(255) DEFAULT NULL COMMENT '备案单位',
  remark           VARCHAR(2000) DEFAULT NULL,
  question_count   BIGINT       NOT NULL DEFAULT 0,
  team_count       BIGINT       NOT NULL DEFAULT 0,
  submission_count BIGINT       NOT NULL DEFAULT 0,
  player_count     BIGINT       NOT NULL DEFAULT 0,
  checksum         CHAR(64)     NOT NULL COMMENT '快照 SHA-256',
  snapshot         LONGTEXT     DEFAULT NULL COMMENT '全量快照 JSON',
  archived_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  archived_by      BIGINT       DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_archive_contest (contest_id),
  KEY idx_archive_filing (filing_no),
  KEY idx_archive_time (archived_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事备案归档快照';
