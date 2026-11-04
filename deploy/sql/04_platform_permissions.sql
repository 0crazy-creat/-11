-- ============================================================
-- 平台底座新增权限点：备案归档、通知发送
-- 幂等：可重复执行
-- ============================================================
SET NAMES utf8mb4;

INSERT INTO sys_permission (id, code, name, module, type) VALUES
 (1106,'contest:archive','赛事备案归档','contest','API'),
 (1801,'notify:send','发送通知/公告','notify','API')
ON DUPLICATE KEY UPDATE name=VALUES(name), module=VALUES(module);

-- 管理员走 1001 通配，无需显式授予；裁判/教师可归档但不可群发
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
 (2,1106),(3,1106);
