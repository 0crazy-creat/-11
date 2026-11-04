-- ============================================================
-- AWDP（增强攻防）防御修补闭环 —— 增量表
-- 参考 AthenaCTF AWD-PLUS：修补包上传 → 自动校验 → 应用重启 → 防御分结算
-- 说明：01_schema.sql 为基线，本文件为增量，不改动基线。
-- ============================================================
SET NAMES utf8mb4;

-- 防御修补提交记录（与攻击 flag 提交 submission 表完全分离）
CREATE TABLE IF NOT EXISTS awd_defense_submission (
  id            BIGINT       NOT NULL COMMENT '主键（雪花ID）',
  contest_id    BIGINT       NOT NULL COMMENT '赛事ID（contest.type 必须为 AWDP）',
  round_id      BIGINT       DEFAULT NULL COMMENT '轮次ID（awd_round.id）',
  round_no      INT            DEFAULT NULL COMMENT '轮次号（冗余，便于按轮统计）',
  team_id       BIGINT       NOT NULL COMMENT '提交战队ID',
  question_id   BIGINT       NOT NULL COMMENT '题目/靶机ID',
  user_id       BIGINT       DEFAULT NULL COMMENT '提交人用户ID',
  file_name     VARCHAR(255) DEFAULT NULL COMMENT '原始修补包文件名',
  file_size     BIGINT       DEFAULT 0 COMMENT '修补包大小（字节）',
  file_sha256   VARCHAR(64)  DEFAULT NULL COMMENT '修补包 SHA-256（防篡改/去重）',
  store_path    VARCHAR(512) DEFAULT NULL COMMENT '服务端落盘路径',
  package_type  VARCHAR(16)  DEFAULT NULL COMMENT 'ZIP / PATCH',
  status        VARCHAR(32)  NOT NULL DEFAULT 'PENDING'
                COMMENT 'PENDING/CHECKING/APPLYING/VERIFIED/REJECTED/FAILED/COMPROMISED',
  check_script  VARCHAR(255) DEFAULT NULL COMMENT '本次使用的 check 脚本',
  check_output  TEXT         DEFAULT NULL COMMENT 'check 脚本输出（截断保存）',
  apply_output  TEXT         DEFAULT NULL COMMENT '修补包应用输出（截断保存）',
  restart_ok    TINYINT      NOT NULL DEFAULT 0 COMMENT '应用后重启靶机是否成功',
  health_ok     TINYINT      NOT NULL DEFAULT 0 COMMENT '修补后健康检查是否通过',
  compromised   TINYINT      NOT NULL DEFAULT 0 COMMENT '修补后被攻破标记（1=仍被攻破，本轮防御分为0）',
  defense_score INT          NOT NULL DEFAULT 0 COMMENT '本次结算的防御分增量（可正可负）',
  remark        VARCHAR(512) DEFAULT NULL COMMENT '战队备注/修补说明',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_def_contest_team (contest_id, team_id),
  KEY idx_def_round (contest_id, round_no),
  KEY idx_def_status (contest_id, status),
  KEY idx_def_sha (contest_id, file_sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AWDP 防御修补提交记录';