USE csp;
-- Writeup（题解）模块：选手提交题解，审核后公开
CREATE TABLE IF NOT EXISTS `writeup` (
  `id` bigint NOT NULL,
  `contest_id` bigint DEFAULT NULL COMMENT '所属赛事，可为空（练习题解）',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `team_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL COMMENT '作者',
  `title` varchar(255) NOT NULL,
  `content` mediumtext COMMENT 'Markdown 正文',
  `status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  `review_remark` varchar(512) DEFAULT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  `reviewed_at` datetime DEFAULT NULL,
  `view_count` int NOT NULL DEFAULT 0,
  `like_count` int NOT NULL DEFAULT 0,
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_wu_question` (`question_id`),
  KEY `idx_wu_contest` (`contest_id`),
  KEY `idx_wu_user` (`user_id`),
  KEY `idx_wu_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题解（Writeup）';
