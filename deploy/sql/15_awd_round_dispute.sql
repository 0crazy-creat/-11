-- =====================================================================
-- 15_awd_round_dispute.sql
-- AWD 轮次控制域增量：轮次中断字段 + 申诉仲裁表
--
-- 幂等设计：
--   * MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，故用 information_schema
--     + 预处理语句实现等价幂等（可重复执行，不报 1060 Duplicate column）。
--   * 表用 CREATE TABLE IF NOT EXISTS。
--   * 索引用 information_schema.STATISTICS 判存在后再建。
--   * 全部语句对已有数据安全（新增列均可空、无默认值回填）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. awd_round 增加三个可空列
--    interrupt_type   : SOFT / HARD / NULL（软中断不改变状态，硬中断置 ABORTED）
--    interrupt_reason : 中断原因（操作人、原因说明，JSON 或纯文本）
--    settled_at       : 结算完成时间（SETTLING → FINISHED 的时刻）
-- ---------------------------------------------------------------------
SET @db := DATABASE();

SET @sql := (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'awd_round' AND COLUMN_NAME = 'interrupt_type') = 0,
    'ALTER TABLE awd_round ADD COLUMN interrupt_type VARCHAR(16) NULL DEFAULT NULL COMMENT ''中断类型：SOFT/HARD，NULL 表示未中断'' AFTER duration_s',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'awd_round' AND COLUMN_NAME = 'interrupt_reason') = 0,
    'ALTER TABLE awd_round ADD COLUMN interrupt_reason VARCHAR(512) NULL DEFAULT NULL COMMENT ''中断原因与操作人'' AFTER interrupt_type',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'awd_round' AND COLUMN_NAME = 'settled_at') = 0,
    'ALTER TABLE awd_round ADD COLUMN settled_at DATETIME NULL DEFAULT NULL COMMENT ''结算完成时间'' AFTER interrupt_reason',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 2. 申诉仲裁表 awd_round_dispute
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS awd_round_dispute (
  id             BIGINT       NOT NULL COMMENT '主键（雪花ID）',
  contest_id     BIGINT       NOT NULL COMMENT '竞赛ID',
  round_no       INT          NOT NULL COMMENT '轮次号',
  team_id        BIGINT       NOT NULL COMMENT '申诉队伍ID',
  question_id    BIGINT       NULL     DEFAULT NULL COMMENT '关联题目/靶机，NULL 表示针对整轮',
  dispute_type   VARCHAR(32)  NOT NULL DEFAULT 'SCORE' COMMENT '申诉类型：SCORE/SLA/FLAG/OTHER',
  reason         VARCHAR(1024) NOT NULL COMMENT '申诉理由（选手填写）',
  evidence       VARCHAR(1024) NULL DEFAULT NULL COMMENT '证据链接/说明',
  status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  applicant_id   BIGINT       NULL     DEFAULT NULL COMMENT '申请人用户ID',
  arbiter_id     BIGINT       NULL     DEFAULT NULL COMMENT '仲裁人用户ID',
  arbitrate_note VARCHAR(1024) NULL DEFAULT NULL COMMENT '仲裁意见',
  arbitrated_at  DATETIME     NULL DEFAULT NULL COMMENT '仲裁时间',
  rollback_score DECIMAL(12,2) NULL DEFAULT NULL COMMENT '实际回滚的分值（approve 时写入）',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (id),
  KEY idx_dispute_contest_round (contest_id, round_no),
  KEY idx_dispute_team (team_id),
  KEY idx_dispute_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AWD 轮次申诉仲裁记录';

-- ---------------------------------------------------------------------
-- 3. 轮次控制审计（中断/仲裁操作留痕，csp-awd 无平台审计表依赖，独立落库）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS awd_round_audit (
  id          BIGINT       NOT NULL COMMENT '主键（雪花ID）',
  contest_id  BIGINT       NOT NULL COMMENT '竞赛ID',
  round_no    INT          NOT NULL COMMENT '轮次号',
  action      VARCHAR(32)  NOT NULL COMMENT 'BEGIN/RUNNING/SETTLE/INTERRUPT_SOFT/INTERRUPT_HARD/DISPUTE/ARBITRATE',
  target_id   BIGINT       NULL DEFAULT NULL COMMENT '作用对象ID（申诉ID等）',
  operator_id BIGINT       NULL DEFAULT NULL COMMENT '操作人用户ID',
  operator    VARCHAR(64)  NULL DEFAULT NULL COMMENT '操作人用户名',
  detail      VARCHAR(1024) NULL DEFAULT NULL COMMENT '操作详情',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_round_audit_contest_round (contest_id, round_no),
  KEY idx_round_audit_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AWD 轮次控制审计日志';