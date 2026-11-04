-- ============================================================
-- 队伍邀请码机制扩展
-- ============================================================

-- 扩展 contest_team 表
ALTER TABLE contest_team 
  ADD COLUMN invite_code VARCHAR(32) DEFAULT NULL COMMENT '邀请码（唯一）',
  ADD COLUMN invite_expire_at DATETIME DEFAULT NULL COMMENT '邀请码过期时间',
  ADD COLUMN max_members INT DEFAULT 8 COMMENT '战队人数上限',
  ADD COLUMN description TEXT DEFAULT NULL COMMENT '战队简介',
  ADD COLUMN slogan VARCHAR(255) DEFAULT NULL COMMENT '战队口号',
  ADD UNIQUE KEY uk_team_invite_code (invite_code);

-- 新增队长转让记录表
CREATE TABLE IF NOT EXISTS contest_team_captain_transfer (
    id              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    contest_id      BIGINT   NOT NULL COMMENT '赛事ID',
    team_id         BIGINT   NOT NULL COMMENT '战队ID',
    from_captain    BIGINT   NOT NULL COMMENT '原队长用户ID',
    to_captain      BIGINT   NOT NULL COMMENT '新队长用户ID',
    reason          VARCHAR(255) DEFAULT NULL COMMENT '转让原因',
    transferred_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '转让时间',
    PRIMARY KEY (id),
    KEY idx_transfer_team (team_id),
    KEY idx_transfer_from (from_captain),
    KEY idx_transfer_to (to_captain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队长转让记录';

-- 新增邀请记录表
CREATE TABLE IF NOT EXISTS contest_team_invite (
    id              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    contest_id      BIGINT   NOT NULL COMMENT '赛事ID',
    team_id         BIGINT   NOT NULL COMMENT '战队ID',
    inviter_id      BIGINT   NOT NULL COMMENT '邀请人用户ID',
    invitee_id      BIGINT   DEFAULT NULL COMMENT '被邀请人用户ID（接受后填充）',
    invite_code     VARCHAR(32) NOT NULL COMMENT '邀请码',
    status          VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACCEPTED/EXPIRED/CANCELLED',
    expire_at       DATETIME NOT NULL COMMENT '过期时间',
    accepted_at     DATETIME DEFAULT NULL COMMENT '接受时间',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_invite_code (invite_code),
    KEY idx_invite_team (team_id),
    KEY idx_invite_invitee (invitee_id),
    KEY idx_invite_status (status, expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战队邀请记录';
