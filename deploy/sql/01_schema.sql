-- ============================================================
-- 网络安全竞赛平台 - 基础底座库结构 (MySQL 8.0)
-- 设计要点：统一用户/权限/题库；比赛粒度数据隔离；全量审计
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------- 用户与组织 ----------
CREATE TABLE IF NOT EXISTS sys_user (
  id            BIGINT       NOT NULL COMMENT '主键(雪花ID)',
  username      VARCHAR(64)  NOT NULL COMMENT '登录名',
  password      VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码哈希',
  nickname      VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
  real_name     VARCHAR(64)  DEFAULT NULL COMMENT '真实姓名',
  student_no    VARCHAR(64)  DEFAULT NULL COMMENT '学号/工号',
  phone         VARCHAR(32)  DEFAULT NULL COMMENT '手机号(主索引)',
  email         VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  avatar        VARCHAR(512) DEFAULT NULL COMMENT '头像',
  org_code      VARCHAR(64)  DEFAULT NULL COMMENT '所属院校/单位编码',
  org_name      VARCHAR(128) DEFAULT NULL COMMENT '所属院校/单位名称',
  class_name    VARCHAR(64)  DEFAULT NULL COMMENT '班级',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用 2锁定',
  mfa_enabled   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否开启双因素',
  mfa_secret    VARCHAR(128) DEFAULT NULL COMMENT 'TOTP 密钥(加密存储)',
  last_login_at DATETIME     DEFAULT NULL,
  last_login_ip VARCHAR(64)  DEFAULT NULL,
  ext           JSON         DEFAULT NULL COMMENT '扩展字段',
  deleted       TINYINT      NOT NULL DEFAULT 0,
  created_by    BIGINT       DEFAULT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by    BIGINT       DEFAULT NULL,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username),
  UNIQUE KEY uk_user_phone (phone),
  KEY idx_user_org (org_code),
  KEY idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台用户';

-- ---------- RBAC ----------
CREATE TABLE IF NOT EXISTS sys_role (
  id          BIGINT      NOT NULL,
  code        VARCHAR(64) NOT NULL COMMENT '角色码 ADMIN/JUDGE/TEACHER/PLAYER/VIEWER',
  name        VARCHAR(64) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  builtin     TINYINT     NOT NULL DEFAULT 0 COMMENT '内置角色不可删',
  deleted     TINYINT     NOT NULL DEFAULT 0,
  created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE IF NOT EXISTS sys_permission (
  id        BIGINT      NOT NULL,
  code      VARCHAR(128) NOT NULL COMMENT '权限点 如 contest:create',
  name      VARCHAR(128) NOT NULL,
  module    VARCHAR(64)  DEFAULT NULL COMMENT '所属模块',
  type      VARCHAR(16)  NOT NULL DEFAULT 'API' COMMENT 'API/MENU/BUTTON',
  parent_id BIGINT       DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_perm_code (code),
  KEY idx_perm_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点';

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  KEY idx_ur_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限';

-- ---------- 赛事底座 ----------
CREATE TABLE IF NOT EXISTS contest (
  id            BIGINT       NOT NULL,
  code          VARCHAR(64)  NOT NULL COMMENT '赛事编码',
  name          VARCHAR(255) NOT NULL COMMENT '赛事名称',
  type          VARCHAR(32)  NOT NULL COMMENT 'CTF/AWD/AWDP/THEORY/SCENARIO/CFS',
  mode          VARCHAR(32)  NOT NULL DEFAULT 'TEAM' COMMENT 'INDIVIDUAL/TEAM',
  status        VARCHAR(32)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PENDING/SIGNUP/RUNNING/FINISHED/ARCHIVED',
  signup_start  DATETIME     DEFAULT NULL,
  signup_end    DATETIME     DEFAULT NULL,
  start_at      DATETIME     DEFAULT NULL COMMENT '比赛开始',
  end_at        DATETIME     DEFAULT NULL COMMENT '比赛结束',
  max_players   INT          NOT NULL DEFAULT 1000 COMMENT '参赛上限',
  allow_late    TINYINT      NOT NULL DEFAULT 0 COMMENT '补报名开关',
  network_mode  VARCHAR(16)  NOT NULL DEFAULT 'INTERNAL' COMMENT 'INTERNAL/EXTERNAL 内外网模式',
  blackout      TINYINT      NOT NULL DEFAULT 0 COMMENT '黑灯模式',
  dynamic_flag  TINYINT      NOT NULL DEFAULT 1 COMMENT '动态flag',
  first_blood   TINYINT      NOT NULL DEFAULT 1 COMMENT '首杀奖励',
  description   TEXT         DEFAULT NULL,
  config        JSON         DEFAULT NULL COMMENT '赛制扩展配置',
  schema_name   VARCHAR(64)  DEFAULT NULL COMMENT '独立数据 Schema(比赛隔离)',
  organizer     VARCHAR(255) DEFAULT NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  created_by    BIGINT       DEFAULT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by    BIGINT       DEFAULT NULL,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_contest_code (code),
  KEY idx_contest_status (status),
  KEY idx_contest_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事主表';

CREATE TABLE IF NOT EXISTS contest_team (
  id          BIGINT      NOT NULL,
  contest_id  BIGINT      NOT NULL,
  name        VARCHAR(128) NOT NULL COMMENT '战队名',
  code        VARCHAR(64) DEFAULT NULL,
  logo        VARCHAR(512) DEFAULT NULL,
  org_code    VARCHAR(64) DEFAULT NULL,
  captain_id  BIGINT      DEFAULT NULL,
  members     INT         NOT NULL DEFAULT 0,
  status      VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/QUIT',
  score       BIGINT      NOT NULL DEFAULT 0,
  `rank`        INT         DEFAULT NULL,
  deleted     TINYINT     NOT NULL DEFAULT 0,
  created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_team_contest (contest_id, status),
  KEY idx_team_score (contest_id, score DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参赛战队';

CREATE TABLE IF NOT EXISTS contest_team_member (
  id         BIGINT   NOT NULL,
  contest_id BIGINT   NOT NULL,
  team_id    BIGINT   NOT NULL,
  user_id    BIGINT   NOT NULL,
  role       VARCHAR(16) NOT NULL DEFAULT 'MEMBER' COMMENT 'CAPTAIN/MEMBER',
  joined_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctm (contest_id, user_id),
  KEY idx_ctm_team (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战队成员';

-- ---------- 题库 ----------
CREATE TABLE IF NOT EXISTS question (
  id           BIGINT      NOT NULL,
  bank_type    VARCHAR(32) NOT NULL COMMENT 'THEORY/CTF/SCENARIO/AWD',
  category     VARCHAR(32) DEFAULT NULL COMMENT 'Web/Misc/Crypto/Reverse/Pwn',
  title        VARCHAR(255) NOT NULL,
  content      MEDIUMTEXT  DEFAULT NULL COMMENT '题面(Markdown)',
  answer       TEXT        DEFAULT NULL COMMENT '标准答案(理论题)',
  flag         VARCHAR(255) DEFAULT NULL COMMENT '静态flag',
  score        INT         NOT NULL DEFAULT 100,
  difficulty   VARCHAR(16) DEFAULT NULL COMMENT 'EASY/MEDIUM/HARD/INSANE',
  tags         JSON        DEFAULT NULL COMMENT '知识点标签',
  attachment   VARCHAR(512) DEFAULT NULL,
  writeup      MEDIUMTEXT  DEFAULT NULL,
  image_name   VARCHAR(255) DEFAULT NULL COMMENT '靶场镜像(场景/CTF环境)',
  cpu_limit    VARCHAR(32) DEFAULT NULL,
  mem_limit    VARCHAR(32) DEFAULT NULL,
  status       TINYINT     NOT NULL DEFAULT 1,
  ext          JSON        DEFAULT NULL COMMENT '赛制特有扩展字段',
  deleted      TINYINT     NOT NULL DEFAULT 0,
  created_by   BIGINT      DEFAULT NULL,
  created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_q_type (bank_type),
  KEY idx_q_cat (bank_type, category),
  FULLTEXT KEY ft_q_title (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一题库';

CREATE TABLE IF NOT EXISTS contest_question (
  id          BIGINT   NOT NULL,
  contest_id  BIGINT   NOT NULL,
  question_id BIGINT   NOT NULL,
  sort_no     INT      NOT NULL DEFAULT 0,
  score       INT      DEFAULT NULL COMMENT '覆盖原题分数',
  visible     TINYINT  NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cq (contest_id, question_id),
  KEY idx_cq_contest (contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事题目关联';

-- ---------- 提交与成绩 ----------
CREATE TABLE IF NOT EXISTS submission (
  id          BIGINT      NOT NULL,
  contest_id  BIGINT      NOT NULL,
  team_id     BIGINT      DEFAULT NULL,
  user_id     BIGINT      NOT NULL,
  question_id BIGINT      NOT NULL,
  submit_type VARCHAR(16) NOT NULL DEFAULT 'FLAG' COMMENT 'FLAG/ANSWER/ATTACK/DEFENSE/EVIDENCE',
  content     VARCHAR(512) DEFAULT NULL COMMENT '归一化后的提交内容(敏感值脱敏)',
  correct     TINYINT     NOT NULL DEFAULT 0,
  score       INT         NOT NULL DEFAULT 0,
  round_no    INT         DEFAULT NULL COMMENT 'AWD 轮次',
  ip          VARCHAR(64) DEFAULT NULL,
  device_id   VARCHAR(128) DEFAULT NULL,
  elapsed_ms  BIGINT      DEFAULT NULL COMMENT '答题耗时',
  created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_sub_contest (contest_id, created_at),
  KEY idx_sub_user (contest_id, user_id),
  KEY idx_sub_q (contest_id, question_id, correct)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提交记录';

CREATE TABLE IF NOT EXISTS scoreboard (
  id          BIGINT   NOT NULL,
  contest_id  BIGINT   NOT NULL,
  team_id     BIGINT   NOT NULL,
  score       BIGINT   NOT NULL DEFAULT 0,
  solved      INT      NOT NULL DEFAULT 0,
  attack_score BIGINT  NOT NULL DEFAULT 0,
  defense_score BIGINT NOT NULL DEFAULT 0,
  penalty     INT      NOT NULL DEFAULT 0,
  last_solve_at DATETIME DEFAULT NULL,
  rank_no     INT      DEFAULT NULL,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sb (contest_id, team_id),
  KEY idx_sb_rank (contest_id, score DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排行榜快照';

-- ---------- AWD ----------
CREATE TABLE IF NOT EXISTS awd_round (
  id          BIGINT   NOT NULL,
  contest_id  BIGINT   NOT NULL,
  round_no    INT      NOT NULL,
  status      VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/CHECKING/FINISHED',
  start_at    DATETIME DEFAULT NULL,
  end_at      DATETIME DEFAULT NULL,
  duration_s  INT      NOT NULL DEFAULT 600,
  PRIMARY KEY (id),
  UNIQUE KEY uk_round (contest_id, round_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AWD 轮次';

CREATE TABLE IF NOT EXISTS awd_target (
  id          BIGINT   NOT NULL,
  contest_id  BIGINT   NOT NULL,
  team_id     BIGINT   NOT NULL,
  question_id BIGINT   DEFAULT NULL,
  container_id VARCHAR(128) DEFAULT NULL,
  ip          VARCHAR(64) DEFAULT NULL,
  port        INT      DEFAULT NULL,
  status      VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/STOPPED/ERROR',
  health      TINYINT  NOT NULL DEFAULT 1,
  cpu_percent DOUBLE   DEFAULT 0,
  mem_used_mb DOUBLE   DEFAULT 0,
  last_check_at DATETIME DEFAULT NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_target_contest (contest_id, team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='靶机实例';

-- ---------- 审计与通知 ----------
CREATE TABLE IF NOT EXISTS audit_log (
  id         BIGINT      NOT NULL,
  trace_id   VARCHAR(64) DEFAULT NULL,
  user_id    BIGINT      DEFAULT NULL,
  username   VARCHAR(64) DEFAULT NULL,
  action     VARCHAR(128) NOT NULL COMMENT '操作类型',
  resource   VARCHAR(255) DEFAULT NULL,
  detail     JSON        DEFAULT NULL,
  ip         VARCHAR(64) DEFAULT NULL,
  ua         VARCHAR(512) DEFAULT NULL,
  result     VARCHAR(16) NOT NULL DEFAULT 'SUCCESS',
  cost_ms    BIGINT      DEFAULT NULL,
  created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_audit_user (user_id, created_at),
  KEY idx_audit_action (action, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';

CREATE TABLE IF NOT EXISTS notify_message (
  id         BIGINT      NOT NULL,
  contest_id BIGINT      DEFAULT NULL,
  user_id    BIGINT      DEFAULT NULL COMMENT '为空表示广播',
  channel    VARCHAR(32) NOT NULL DEFAULT 'INBOX' COMMENT 'INBOX/WS/EMAIL/SMS',
  title      VARCHAR(255) DEFAULT NULL,
  content    TEXT        DEFAULT NULL,
  read_flag  TINYINT     NOT NULL DEFAULT 0,
  sent_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notify_user (user_id, read_flag),
  KEY idx_notify_contest (contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信/通知';

SET FOREIGN_KEY_CHECKS = 1;
