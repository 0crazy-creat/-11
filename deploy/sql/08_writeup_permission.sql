SET NAMES utf8mb4;
USE csp;
-- 题解审核权限（管理员走通配 1001；裁判可审核题解）
INSERT INTO sys_permission (id, code, name, module, type) VALUES
 (1901,'writeup:review','题解审核','writeup','API')
ON DUPLICATE KEY UPDATE name=VALUES(name), module=VALUES(module);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (2,1901),(3,1901);
SELECT code, name FROM sys_permission WHERE code LIKE 'writeup%';
