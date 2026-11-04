-- =====================================================================
-- 16_scenario_task.sql
-- 场景实操赛（SCENARIO）：多机靶场任务依赖关系 + 容器编排实例
--
-- 设计原则（遵循 captain 裁决）：
--   * 题目主体复用 question 表（bank_type='SCENARIO'），不另建 scenario 主表，
--     使组卷/报名/榜单/审计等通用链路自动复用；
--   * 依赖关系主存 question.ext.preQuestionId，本表为「依赖链 + 编排」的
--     强类型物化视图，用于环检测、拓扑排序与多机编排关联；
--   * 全部幂等：CREATE TABLE IF NOT EXISTS + information_schema 判存在的
--     ALTER TABLE，可重复执行不报错。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 任务依赖关系（Scenario → Task 树）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS scenario_task (
  id             BIGINT       NOT NULL COMMENT '主键（雪花ID）',
  contest_id     BIGINT       NOT NULL COMMENT '竞赛ID',
  scenario_id    BIGINT       NOT NULL COMMENT '场景题ID（question.id，bank_type=SCENARIO）',
  task_id        BIGINT       NOT NULL COMMENT '任务题ID（question.id，bank_type=SCENARIO）',
  pre_task_id    BIGINT       NULL     DEFAULT NULL COMMENT '前置任务ID，NULL 表示无依赖',
  order_no       INT          NOT NULL DEFAULT 0 COMMENT '同场景内排序',
  title          VARCHAR(255) NULL DEFAULT NULL COMMENT '任务标题快照',
  score          INT          NOT NULL DEFAULT 100 COMMENT '任务分值快照',
  unlock_mode    VARCHAR(16)  NOT NULL DEFAULT 'SEQUENTIAL' COMMENT '解锁方式：SEQUENTIAL/ANY',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_scenario_task (contest_id, scenario_id, task_id),
  KEY idx_st_pre (contest_id, pre_task_id),
  KEY idx_st_scenario (contest_id, scenario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景实操赛任务依赖关系';

-- ---------------------------------------------------------------------
-- 2. 多机靶场编排实例（一个场景 = 一个 docker network + 多个容器）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS scenario_env (
  id             BIGINT       NOT NULL COMMENT '主键（雪花ID）',
  contest_id     BIGINT       NOT NULL COMMENT '竞赛ID',
  team_id        BIGINT       NOT NULL COMMENT '战队ID',
  scenario_id    BIGINT       NOT NULL COMMENT '场景题ID',
  network_name   VARCHAR(128) NOT NULL COMMENT 'Docker 自定义网络名（内网互通）',
  compose_tpl    MEDIUMTEXT   NULL     DEFAULT NULL COMMENT 'compose 编排模板（YAML/JSON）',
  entry_node     VARCHAR(64)  NULL     DEFAULT NULL COMMENT '对外暴露的入口节点名',
  entry_host     VARCHAR(64)  NULL     DEFAULT NULL COMMENT '入口访问 host',
  entry_port     INT          NULL     DEFAULT NULL COMMENT '入口映射到宿主机的端口',
  status         VARCHAR(16)  NOT NULL DEFAULT 'CREATING' COMMENT 'CREATING/RUNNING/FAILED/DESTROYED',
  node_count     INT          NOT NULL DEFAULT 0 COMMENT '已拉起容器数',
  detail         VARCHAR(1024) NULL    DEFAULT NULL COMMENT '节点明细（JSON：name/image/ip/internal）',
  fail_reason    VARCHAR(512) NULL     DEFAULT NULL COMMENT '失败原因',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_scenario_env (contest_id, team_id, scenario_id),
  KEY idx_se_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景实操赛多机靶场编排实例';

-- ---------------------------------------------------------------------
-- 3. 预检报告（开赛前一键预检的历史留痕）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS scenario_precheck (
  id             BIGINT       NOT NULL COMMENT '主键（雪花ID）',
  contest_id     BIGINT       NOT NULL COMMENT '竞赛ID',
  scenario_id    BIGINT       NULL     DEFAULT NULL COMMENT '场景题ID，NULL 表示整场预检',
  passed         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否全部通过：0否 1是',
  image_ok       INT          NOT NULL DEFAULT 0 COMMENT '镜像检查通过数',
  image_missing  VARCHAR(1024) NULL    DEFAULT NULL COMMENT '缺失镜像清单（逗号分隔）',
  port_conflict  VARCHAR(1024) NULL    DEFAULT NULL COMMENT '端口冲突清单',
  tpl_error      VARCHAR(1024) NULL    DEFAULT NULL COMMENT '模板解析错误',
  detail         MEDIUMTEXT   NULL     DEFAULT NULL COMMENT '完整报告（JSON）',
  operator_id    BIGINT       NULL     DEFAULT NULL COMMENT '操作人',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_sp_contest (contest_id),
  KEY idx_sp_passed (passed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景实操赛环境预检报告';

-- ---------------------------------------------------------------------
-- 4. 为 scenario_task 补齐可能缺失的列（对老库幂等）
-- ---------------------------------------------------------------------
SET @db := DATABASE();

SET @sql := (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'scenario_task' AND COLUMN_NAME = 'unlock_mode') = 0,
    'ALTER TABLE scenario_task ADD COLUMN unlock_mode VARCHAR(16) NOT NULL DEFAULT ''SEQUENTIAL'' COMMENT ''解锁方式'' AFTER score',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 5. 索引 question(bank_type, category)，加速场景树查询（幂等）
-- ---------------------------------------------------------------------
SET @sql := (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'question' AND INDEX_NAME = 'idx_question_bank_cat') = 0,
    'ALTER TABLE question ADD INDEX idx_question_bank_cat (bank_type, category)',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- ---------------------------------------------------------------------
-- 6. 时间列兜底默认值（老库若以旧版 DDL 建表则 NOT NULL 且无默认值，
--    MyBatis 全局填充器只填 createdAt/updatedAt，无法覆盖 createTime/updateTime）
-- ---------------------------------------------------------------------
ALTER TABLE scenario_env      MODIFY COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE scenario_env      MODIFY COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE scenario_task     MODIFY COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE scenario_task     MODIFY COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
