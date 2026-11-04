-- ============================================================
-- 容器生命周期 & 动态 Flag 持久化（幂等，可重复执行）
-- 参考 GZCTF 的 Container + FlagContext 模型
-- ============================================================
SET NAMES utf8mb4;

-- ---------- 容器实例表 ----------
CREATE TABLE IF NOT EXISTS challenge_container (
  id              BIGINT       NOT NULL COMMENT '主键(雪花ID)',
  contest_id      BIGINT       NOT NULL COMMENT '赛事ID',
  team_id         BIGINT       NOT NULL COMMENT '战队ID',
  question_id     BIGINT       NOT NULL COMMENT '题目ID',
  user_id         BIGINT       DEFAULT NULL COMMENT '操作用户',
  image           VARCHAR(255) NOT NULL COMMENT 'Docker 镜像名',
  container_id    VARCHAR(128) DEFAULT NULL COMMENT 'Docker 容器ID',
  container_name  VARCHAR(255) NOT NULL COMMENT '容器名称(唯一)',
  status          VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/STOPPED/DESTROYED/ERROR',
  started_at      DATETIME     DEFAULT NULL COMMENT '实际启动时间',
  expect_stop_at  DATETIME     DEFAULT NULL COMMENT '预期停止时间(TTL)',
  ip              VARCHAR(64)  DEFAULT NULL COMMENT '容器内网IP',
  port            INT          DEFAULT NULL COMMENT '容器内端口',
  public_ip       VARCHAR(64)  DEFAULT NULL COMMENT '对外IP',
  public_port     INT          DEFAULT NULL COMMENT '对外映射端口',
  is_proxy        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否走平台代理',
  network_mode    VARCHAR(16)  NOT NULL DEFAULT 'ISOLATED' COMMENT 'OPEN/ISOLATED/CUSTOM',
  cpu_limit       DOUBLE       NOT NULL DEFAULT 1.0 COMMENT 'CPU限额(核)',
  memory_limit    INT          NOT NULL DEFAULT 512 COMMENT '内存限额(MB)',
  storage_limit   INT          NOT NULL DEFAULT 256 COMMENT '存储限额(MB)',
  simulated       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否模拟模式',
  last_operation_at DATETIME   DEFAULT NULL COMMENT '最后一次操作时间(限流用)',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_container_name (container_name),
  KEY idx_cc_contest (contest_id),
  KEY idx_cc_team (contest_id, team_id),
  KEY idx_cc_status (status),
  KEY idx_cc_expect_stop (expect_stop_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='靶机容器实例';

-- ---------- 动态 Flag 上下文表 ----------
CREATE TABLE IF NOT EXISTS flag_context (
  id              BIGINT       NOT NULL COMMENT '主键(雪花ID)',
  contest_id      BIGINT       NOT NULL,
  team_id         BIGINT       NOT NULL,
  question_id     BIGINT       NOT NULL,
  flag            VARCHAR(255) NOT NULL COMMENT '分配给该队该题的flag',
  flag_type       VARCHAR(16)  NOT NULL DEFAULT 'DYNAMIC' COMMENT 'STATIC/DYNAMIC',
  is_correct      TINYINT      DEFAULT NULL COMMENT '是否已正确提交: NULL未提交/0错误/1正确',
  submitted_at    DATETIME     DEFAULT NULL COMMENT '正确提交时间',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_fc_instance (contest_id, team_id, question_id),
  KEY idx_fc_contest (contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态Flag上下文(每队每題唯一)';
