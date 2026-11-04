SET NAMES utf8mb4;
USE csp;

CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT NOT NULL PRIMARY KEY,
  contest_id BIGINT NULL,
  title VARCHAR(255) NOT NULL,
  content MEDIUMTEXT NULL,
  scope VARCHAR(32) NOT NULL DEFAULT 'PLATFORM',
  pinned TINYINT NOT NULL DEFAULT 0,
  publish_at DATETIME NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
  created_by BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_ann_contest (contest_id),
  KEY idx_ann_scope (scope, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO sys_role (id, code, name, description, builtin)
VALUES (3,'TEACHER','教师','课程组卷、练习场运营管理',1);

INSERT IGNORE INTO announcement (id, contest_id, title, content, scope, pinned, publish_at, status, created_by)
VALUES (3100000000000000001, NULL,
 '平台上线公告', '## 网络安全竞赛平台已上线\n\n- 支持 CTF / AWD / AWDP / 理论赛 / 场景赛\n- 选手端：赛事大厅 → 我的赛事 → 答题\n- 管理端：赛事管理 / 题库管理 / 靶场编排\n\n> 如遇问题请联系管理员。',
 'PLATFORM', 1, NOW(), 'PUBLISHED', 1000000000000000001),
(3100000000000000002, 2117274592061263874,
 'CTF 挑战赛开赛提醒', '**2026 网络安全 CTF 挑战赛** 已开赛，共 10 道题，祝各位选手取得好成绩！',
 'CONTEST', 0, NOW(), 'PUBLISHED', 1000000000000000001);
