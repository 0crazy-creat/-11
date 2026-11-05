-- ============================================================
-- 战队改为全局独立模块：team / team_member
-- contest_team 退化为「赛事参赛记录」，通过 team_id 关联全局战队
-- ============================================================

CREATE TABLE IF NOT EXISTS team (
  id               BIGINT       NOT NULL COMMENT '主键',
  name             VARCHAR(128) NOT NULL COMMENT '战队名（应用层唯一）',
  logo             VARCHAR(512) DEFAULT NULL,
  description      TEXT         DEFAULT NULL COMMENT '战队简介',
  slogan           VARCHAR(255) DEFAULT NULL COMMENT '战队口号',
  captain_id       BIGINT       NOT NULL COMMENT '队长用户ID',
  members          INT          NOT NULL DEFAULT 1 COMMENT '成员数',
  max_members      INT          NOT NULL DEFAULT 8 COMMENT '人数上限',
  invite_code      VARCHAR(32)  DEFAULT NULL COMMENT '邀请码',
  invite_expire_at DATETIME     DEFAULT NULL COMMENT '邀请码过期时间',
  deleted          TINYINT      NOT NULL DEFAULT 0,
  created_by       BIGINT       DEFAULT NULL,
  created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by       BIGINT       DEFAULT NULL,
  updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_team_captain (captain_id),
  UNIQUE KEY uk_global_invite (invite_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局战队';

CREATE TABLE IF NOT EXISTS team_member (
  id        BIGINT      NOT NULL COMMENT '主键',
  team_id   BIGINT      NOT NULL,
  user_id   BIGINT      NOT NULL,
  role      VARCHAR(16) NOT NULL DEFAULT 'MEMBER' COMMENT 'CAPTAIN/MEMBER',
  joined_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tm (team_id, user_id),
  KEY idx_tm_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局战队成员';

ALTER TABLE contest_team
  ADD COLUMN team_id BIGINT DEFAULT NULL COMMENT '全局战队ID；个人参赛为 NULL' AFTER contest_id;

ALTER TABLE contest_team ADD KEY idx_team_global (team_id);
