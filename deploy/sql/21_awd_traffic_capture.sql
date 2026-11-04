-- ================================================================
-- 21: AWD 流量取证 - 网络流量捕获与存储
-- ================================================================

-- AWD 流量捕获表：记录每轮每队的网络流量
CREATE TABLE IF NOT EXISTS awd_traffic_capture (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    contest_id      BIGINT NOT NULL COMMENT '赛事ID',
    round_no        INT NOT NULL COMMENT '轮次',
    team_id         BIGINT NOT NULL COMMENT '战队ID',
    target_id       BIGINT NOT NULL COMMENT '靶机ID',
    capture_file    VARCHAR(512) NOT NULL COMMENT '捕获文件路径（MinIO对象名）',
    file_size       BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    duration        INT DEFAULT 0 COMMENT '捕获时长（秒）',
    packet_count    INT DEFAULT 0 COMMENT '数据包数量',
    protocol_stats  JSON DEFAULT NULL COMMENT '协议统计（TCP/UDP/ICMP等）',
    storage_type    VARCHAR(20) NOT NULL DEFAULT 'MINIO' COMMENT '存储类型（MINIO/LOCAL/NAS）',
    status          VARCHAR(20) NOT NULL DEFAULT 'CAPTURING' COMMENT '状态（CAPTURING/COMPLETED/FAILED/EXPIRED）',
    error_message   VARCHAR(1024) DEFAULT NULL COMMENT '错误信息',
    started_at      DATETIME NOT NULL COMMENT '开始时间',
    completed_at    DATETIME DEFAULT NULL COMMENT '完成时间',
    expires_at      DATETIME DEFAULT NULL COMMENT '过期时间（自动清理）',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_atc_round_team_target (contest_id, round_no, team_id, target_id),
    KEY idx_atc_contest (contest_id),
    KEY idx_atc_team (team_id),
    KEY idx_atc_status (status),
    KEY idx_atc_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AWD流量捕获';

-- 流量分析结果表：记录自动化分析结果
CREATE TABLE IF NOT EXISTS awd_traffic_analysis (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    capture_id      BIGINT NOT NULL COMMENT '关联的捕获记录ID',
    analysis_type   VARCHAR(50) NOT NULL COMMENT '分析类型（ATTACK_DETECT,VULN_SCAN,ANOMALY_DETECT）',
    severity        VARCHAR(20) NOT NULL DEFAULT 'INFO' COMMENT '严重程度（INFO,LOW,MEDIUM,HIGH,CRITICAL）',
    findings        JSON NOT NULL COMMENT '发现结果（JSON数组）',
    summary         TEXT DEFAULT NULL COMMENT '分析摘要',
    confidence      DECIMAL(5,2) DEFAULT 0.00 COMMENT '置信度（0-1）',
    analyzer        VARCHAR(100) NOT NULL COMMENT '分析器（规则引擎/ML模型）',
    analyzed_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_ata_capture (capture_id),
    KEY idx_ata_type (analysis_type),
    KEY idx_ata_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AWD流量分析结果';

-- 预置配置：流量捕获参数
INSERT INTO sys_config (config_key, config_value, description)
VALUES 
    ('awd.traffic.capture.enabled', 'true', 'AWD流量捕获开关'),
    ('awd.traffic.capture.duration', '300', '每轮捕获时长（秒）'),
    ('awd.traffic.capture.filter', 'tcp or udp or icmp', 'tcpdump过滤规则'),
    ('awd.traffic.storage.type', 'MINIO', '存储类型（MINIO/LOCAL）'),
    ('awd.traffic.storage.bucket', 'awd-traffic', 'MinIO桶名'),
    ('awd.traffic.retention.days', '30', '流量文件保留天数'),
    ('awd.traffic.analysis.auto', 'true', '自动分析开关'),
    ('awd.traffic.analysis.rules', '["SYN_FLOOD","PORT_SCAN","SQL_INJECTION","XSS_ATTACK"]', '启用的分析规则')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 示例：查询某轮某队的流量捕获
-- SELECT tc.*, ta.analysis_type, ta.severity, ta.summary
-- FROM awd_traffic_capture tc
-- LEFT JOIN awd_traffic_analysis ta ON tc.id = ta.capture_id
-- WHERE tc.contest_id = ? AND tc.round_no = ? AND tc.team_id = ?
-- ORDER BY tc.started_at DESC;

-- 示例：清理过期流量文件
-- DELETE FROM awd_traffic_capture 
-- WHERE expires_at < NOW() AND status IN ('COMPLETED', 'FAILED');
