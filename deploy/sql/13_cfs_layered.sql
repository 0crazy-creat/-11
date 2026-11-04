-- ============================================================
-- CFS 场景化渗透赛：分层网络 + 递进解锁 + 递进得分
-- 设计要点：
--   1. 一个 CFS 赛事的每支参赛队拥有一套独立拓扑实例（cfs_instance）
--   2. 拓扑按 NetworkLayer 分层（LAYER1 外网 / LAYER2 内网 / LAYER3 深网）
--   3. 每层为一组靶机（cfs_node），独立 Docker network 分段
--   4. 只有 LAYER1（外网入口）节点暴露宿主端口，内层仅层内可达
--   5. 节点持有本层 flag；拿下外层 flag 才解锁下一层（前置依赖）
--   6. 递进得分：层级越深，flag 基础分值越高
-- ============================================================
SET NAMES utf8mb4;

-- ---------- 分层拓扑模板（赛前定义，跨赛事复用） ----------
CREATE TABLE IF NOT EXISTS cfs_topology (
  id           BIGINT       NOT NULL,
  contest_id   BIGINT       DEFAULT NULL COMMENT '所属赛事（NULL=通用模板）',
  code         VARCHAR(64)  NOT NULL COMMENT '拓扑编码',
  name         VARCHAR(255) NOT NULL COMMENT '拓扑名称',
  description  TEXT         DEFAULT NULL,
  layer_count  INT          NOT NULL DEFAULT 3 COMMENT '层数(1-3)',
  status       VARCHAR(32)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED',
  deleted      TINYINT      NOT NULL DEFAULT 0,
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_topology_code (code),
  KEY idx_topology_contest (contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CFS 分层拓扑模板';

-- ---------- 层定义（每层独立的网络与解锁策略） ----------
CREATE TABLE IF NOT EXISTS cfs_layer (
  id            BIGINT       NOT NULL,
  topology_id   BIGINT       NOT NULL,
  layer_no      INT          NOT NULL COMMENT '1=LAYER1外网 2=LAYER2内网 3=LAYER3深网',
  layer_code    VARCHAR(32)  NOT NULL COMMENT 'LAYER1/LAYER2/LAYER3',
  name          VARCHAR(128) NOT NULL COMMENT '层名称，如“外网入口”',
  -- 前置依赖：必须解出 prev_layer_no 层的任意 flag 才解锁本层（层1无前置，为0）
  prev_layer_no INT          NOT NULL DEFAULT 0 COMMENT '解锁本层所需的前置层号，0=无需前置',
  -- 递进得分：本层 flag 基础分值，越深层越高
  base_score    INT          NOT NULL DEFAULT 100 COMMENT '本层 flag 基础分值',
  score_step    INT          NOT NULL DEFAULT 100 COMMENT '层内节点递增加分步长',
  layer_bonus   INT          NOT NULL DEFAULT 0 COMMENT '整层完成奖励分',
  -- 网络隔离：本层独立 Docker network 段
  network_cidr  VARCHAR(32)  DEFAULT NULL COMMENT '本层网络段，如 172.31.1.0/24',
  expose_port   TINYINT      NOT NULL DEFAULT 0 COMMENT '1=对外暴露宿主端口（仅入口层为1）',
  deleted       TINYINT      NOT NULL DEFAULT 0,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_layer_topology_no (topology_id, layer_no),
  KEY idx_layer_topology (topology_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CFS 分层定义（含解锁前置与递进分值）';

-- ---------- 节点定义（层内靶机，复用 question 作为靶场/任务题） ----------
CREATE TABLE IF NOT EXISTS cfs_node (
  id            BIGINT       NOT NULL,
  topology_id   BIGINT       NOT NULL,
  layer_id      BIGINT       NOT NULL,
  layer_no      INT          NOT NULL,
  node_code     VARCHAR(64)  NOT NULL COMMENT '节点编码（层内唯一）',
  name          VARCHAR(255) NOT NULL,
  question_id   BIGINT       DEFAULT NULL COMMENT '关联题库题目（可跨 bank_type，CTF/SCENARIO/AWD 均可）',
  pre_task_id   BIGINT       DEFAULT NULL COMMENT '前置任务节点ID（非空则必须先完成该任务才可提交）',
  image_name    VARCHAR(255) DEFAULT NULL COMMENT '靶机镜像（为空则用 question.image_name）',
  container_port INT         DEFAULT 80 COMMENT '容器内服务端口',
  is_entry      TINYINT      NOT NULL DEFAULT 0 COMMENT '1=本层入口机（唯一对外暴露）',
  score         INT          NOT NULL DEFAULT 100 COMMENT '本节点 flag 分值',
  flag          VARCHAR(255) DEFAULT NULL COMMENT '静态 flag（动态 flag 由 FlagUtils 派生）',
  dynamic_flag  TINYINT      NOT NULL DEFAULT 1 COMMENT '1=按队伍动态派生 flag',
  deleted       TINYINT      NOT NULL DEFAULT 0,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_node (topology_id, node_code),
  KEY idx_node_layer (topology_id, layer_no),
  KEY idx_node_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CFS 层内靶机节点';

-- ---------- 队伍拓扑实例（每队一套容器化拓扑） ----------
CREATE TABLE IF NOT EXISTS cfs_instance (
  id            BIGINT       NOT NULL,
  contest_id    BIGINT       NOT NULL,
  team_id       BIGINT       NOT NULL,
  topology_id   BIGINT       NOT NULL,
  status        VARCHAR(32)  NOT NULL DEFAULT 'CREATING' COMMENT 'CREATING/READY/STOPPED/ERROR',
  -- 已解锁的最高层号（1..layer_count），初始=1（入口层天然解锁）
  unlocked_layer INT         NOT NULL DEFAULT 1,
  -- 已解出的层号集合，形如 ",1,2,"（便于 LIKE 判定；主判定仍以 submission 为准）
  solved_layers  VARCHAR(64) DEFAULT ',',
  total_score   BIGINT       NOT NULL DEFAULT 0,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_instance (contest_id, team_id),
  KEY idx_instance_status (contest_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CFS 队伍拓扑实例（解锁进度）';

-- ---------- 节点运行实例（队伍 × 节点） ----------
CREATE TABLE IF NOT EXISTS cfs_node_instance (
  id             BIGINT       NOT NULL,
  instance_id    BIGINT       NOT NULL,
  contest_id     BIGINT       NOT NULL,
  team_id        BIGINT       NOT NULL,
  node_id        BIGINT       NOT NULL,
  layer_no       INT          NOT NULL,
  container_id   VARCHAR(128) DEFAULT NULL,
  container_name VARCHAR(191) DEFAULT NULL,
  host           VARCHAR(64)  DEFAULT NULL,
  host_port      INT          DEFAULT NULL COMMENT '仅入口节点有值（对外暴露）',
  inner_ip       VARCHAR(64)  DEFAULT NULL COMMENT '层内网 IP，仅同层可达',
  network_name   VARCHAR(128) DEFAULT NULL COMMENT '本层 Docker network 名',
  status         VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/STOPPED/ERROR',
  -- 该队该节点的实际 flag（动态派生后落库，便于靶机注入与判题比对）
  flag           VARCHAR(255) DEFAULT NULL,
  solved         TINYINT      NOT NULL DEFAULT 0,
  solved_at      DATETIME     DEFAULT NULL,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ni (instance_id, node_id),
  KEY idx_ni_layer (instance_id, layer_no),
  KEY idx_ni_contest_team (contest_id, team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CFS 节点运行实例';

-- ---------- 解锁审计（谁在第几层拿下 flag、解锁了哪一层） ----------
CREATE TABLE IF NOT EXISTS cfs_unlock_log (
  id           BIGINT      NOT NULL,
  contest_id   BIGINT      NOT NULL,
  team_id      BIGINT      NOT NULL,
  node_id      BIGINT      DEFAULT NULL,
  layer_no     INT         NOT NULL COMMENT '被拿下的层号',
  unlocked     INT         NOT NULL COMMENT '本次操作后解锁到的最高层号',
  score        INT         NOT NULL DEFAULT 0,
  created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_unlock_team (contest_id, team_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CFS 层解锁审计日志';

-- 题目 ext 允许 CFS 维度（bank_type 仍沿用统一题库，CFS 用 ext 标注 layer）
-- 组卷允许跨 bank_type：ContestService.bindQuestions 已对 CFS 放行