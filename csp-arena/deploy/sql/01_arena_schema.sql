-- ============================================================
-- 独立靶场服务库结构 (MySQL 8.0)
-- 与竞赛主库 csp 隔离，使用独立库 csp_arena
-- ============================================================
CREATE DATABASE IF NOT EXISTS csp_arena DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE csp_arena;

-- 靶场模板（可复用的靶机镜像定义）
CREATE TABLE IF NOT EXISTS arena_template (
  id            BIGINT       NOT NULL PRIMARY KEY,
  code          VARCHAR(64)  NOT NULL COMMENT '模板编码，唯一',
  name          VARCHAR(128) NOT NULL COMMENT '模板名称',
  category      VARCHAR(32)  NOT NULL DEFAULT 'WEB' COMMENT 'WEB/PWN/CRYPTO/MISC/REV',
  image         VARCHAR(255) NOT NULL COMMENT 'Docker 镜像',
  container_port INT         NOT NULL DEFAULT 80 COMMENT '容器内暴露端口',
  difficulty    VARCHAR(16)  NOT NULL DEFAULT 'EASY' COMMENT 'EASY/MEDIUM/HARD',
  description   VARCHAR(1024) DEFAULT NULL,
  default_flag  VARCHAR(255) DEFAULT NULL COMMENT '默认 flag（可被实例覆盖）',
  cpu_limit     VARCHAR(16)  NOT NULL DEFAULT '1.0',
  memory_limit  VARCHAR(16)  NOT NULL DEFAULT '512m',
  ttl_minutes   INT          NOT NULL DEFAULT 120 COMMENT '实例默认存活分钟',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by    BIGINT       DEFAULT NULL,
  updated_by    BIGINT       DEFAULT NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY uk_template_code (code, deleted),
  KEY idx_template_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='靶场模板';

-- 靶机实例
CREATE TABLE IF NOT EXISTS arena_instance (
  id            BIGINT       NOT NULL PRIMARY KEY,
  template_id   BIGINT       NOT NULL COMMENT '所属模板',
  template_code VARCHAR(64)  NOT NULL,
  owner_id      BIGINT       DEFAULT NULL COMMENT '归属用户',
  owner_name    VARCHAR(64)  DEFAULT NULL,
  contest_id    BIGINT       DEFAULT NULL COMMENT '关联赛事（可空=自由练习）',
  team_id       BIGINT       DEFAULT NULL,
  container_id  VARCHAR(128) DEFAULT NULL,
  container_name VARCHAR(128) DEFAULT NULL,
  image         VARCHAR(255) NOT NULL,
  host_ip       VARCHAR(64)  DEFAULT NULL COMMENT '宿主机IP',
  host_port     INT          DEFAULT NULL COMMENT '映射到宿主机的端口',
  access_url    VARCHAR(512) DEFAULT NULL COMMENT '访问地址',
  flag          VARCHAR(255) DEFAULT NULL COMMENT '本实例 flag',
  status        VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/STOPPED/ERROR/DESTROYED',
  health        TINYINT      NOT NULL DEFAULT 1,
  cpu_percent   DOUBLE       NOT NULL DEFAULT 0,
  mem_used_mb   DOUBLE       NOT NULL DEFAULT 0,
  expires_at    DATETIME     DEFAULT NULL COMMENT '到期自动回收',
  last_check_at DATETIME     DEFAULT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by    BIGINT       DEFAULT NULL,
  updated_by    BIGINT       DEFAULT NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  KEY idx_inst_owner (owner_id),
  KEY idx_inst_contest (contest_id, team_id),
  KEY idx_inst_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='靶机实例';

-- 操作审计
CREATE TABLE IF NOT EXISTS arena_action_log (
  id          BIGINT      NOT NULL PRIMARY KEY,
  instance_id BIGINT      DEFAULT NULL,
  action      VARCHAR(32) NOT NULL COMMENT 'CREATE/RESET/DESTROY/HEALTH_CHECK/EXTEND',
  actor_id    BIGINT      DEFAULT NULL,
  actor_name  VARCHAR(64) DEFAULT NULL,
  detail      VARCHAR(1024) DEFAULT NULL,
  success     TINYINT     NOT NULL DEFAULT 1,
  cost_ms     BIGINT      NOT NULL DEFAULT 0,
  created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_log_instance (instance_id),
  KEY idx_log_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='靶场操作审计';
