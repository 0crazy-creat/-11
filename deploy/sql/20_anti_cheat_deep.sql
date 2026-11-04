-- ================================================================
-- 20: CTF 反作弊深化 - 设备指纹 + 作弊审计日志
-- ================================================================

-- 设备指纹表：记录前端采集的设备特征，用于多账号检测
CREATE TABLE IF NOT EXISTS device_fingerprint (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id       VARCHAR(128) NOT NULL COMMENT '前端生成的设备唯一标识',
    user_id         BIGINT NOT NULL COMMENT '关联用户',
    contest_id      BIGINT NOT NULL COMMENT '关联赛事',
    fingerprint     JSON NOT NULL COMMENT '设备特征（浏览器、屏幕、字体、Canvas等）',
    ip_address      VARCHAR(64) DEFAULT NULL COMMENT '采集时IP',
    user_agent      VARCHAR(512) DEFAULT NULL COMMENT '浏览器UA',
    first_seen_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次出现',
    last_seen_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后出现',
    trust_score     INT NOT NULL DEFAULT 100 COMMENT '信任分（0-100，低于60标记为可疑）',
    risk_flags      VARCHAR(255) DEFAULT NULL COMMENT '风险标记（MULTI_ACCOUNT,VPN,SPOOFED等）',
    UNIQUE KEY uk_df_device_user (device_id, user_id),
    KEY idx_df_contest (contest_id),
    KEY idx_df_user (user_id),
    KEY idx_df_trust (trust_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备指纹';

-- 作弊审计日志表：记录反作弊检测事件
CREATE TABLE IF NOT EXISTS cheat_audit_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    contest_id      BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    team_id         BIGINT DEFAULT NULL,
    audit_type      VARCHAR(50) NOT NULL COMMENT '检测类型（RATE_LIMIT,BRUTE_FORCE,MACHINE_SPEED,MULTI_IP,MULTI_DEVICE,SAME_ANSWER）',
    severity        VARCHAR(20) NOT NULL DEFAULT 'LOW' COMMENT '严重程度（LOW,MEDIUM,HIGH,CRITICAL）',
    risk_score      INT NOT NULL DEFAULT 0 COMMENT '风险分（0-100）',
    detail          JSON DEFAULT NULL COMMENT '详细信息（JSON格式）',
    ip_address      VARCHAR(64) DEFAULT NULL,
    device_id       VARCHAR(128) DEFAULT NULL,
    action_taken    VARCHAR(50) DEFAULT NULL COMMENT '采取措施（WARNING,BLOCKED,SUSPENDED）',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_cal_contest (contest_id),
    KEY idx_cal_user (user_id),
    KEY idx_cal_type (audit_type),
    KEY idx_cal_severity (severity),
    KEY idx_cal_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作弊审计日志';

-- 预置检测规则配置（JSON格式，可扩展）
INSERT INTO cheat_audit_log (contest_id, user_id, audit_type, severity, risk_score, detail, action_taken)
VALUES (0, 0, 'SYSTEM_INIT', 'LOW', 0, 
    '{"rules": {
        "RATE_LIMIT_PER_MINUTE": 20,
        "BRUTE_FORCE_WINDOW_MINUTES": 5,
        "BRUTE_FORCE_THRESHOLD": 60,
        "MIN_HUMAN_ELAPSED_MS": 800,
        "IP_SHARED_ACCOUNTS_THRESHOLD": 5,
        "IP_WINDOW_MINUTES": 10,
        "DEVICE_SHARED_USERS_THRESHOLD": 3,
        "ANSWER_SIMILARITY_THRESHOLD": 0.95
    }}', 
    'INIT')
ON DUPLICATE KEY UPDATE detail = VALUES(detail);

-- 示例：查询某赛事的高风险用户
-- SELECT u.username, cal.audit_type, cal.severity, cal.risk_score, cal.detail
-- FROM cheat_audit_log cal
-- JOIN sys_user u ON cal.user_id = u.id
-- WHERE cal.contest_id = ? AND cal.risk_score >= 60
-- ORDER BY cal.risk_score DESC, cal.created_at DESC;

-- 示例：查询某设备的多账号使用情况
-- SELECT df.device_id, COUNT(DISTINCT df.user_id) AS user_count, 
--        GROUP_CONCAT(DISTINCT u.username) AS users
-- FROM device_fingerprint df
-- JOIN sys_user u ON df.user_id = u.id
-- WHERE df.contest_id = ?
-- GROUP BY df.device_id
-- HAVING user_count > 1;
