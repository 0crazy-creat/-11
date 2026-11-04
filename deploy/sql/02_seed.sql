-- ============================================================
-- 基础种子数据：内置角色、权限点、管理员账号
-- 管理员初始密码：Admin@123（首次登录强制修改）
-- ============================================================
SET NAMES utf8mb4;

INSERT INTO sys_role (id, code, name, description, builtin) VALUES
 (1,'ADMIN','管理员','后台全部管理权限',1),
 (2,'JUDGE','裁判','赛事控制权：暂停、封号、加分、强制结束、争议裁决',1),
 (3,'TEACHER','教师','课程组卷、练习场运营管理',1),
 (4,'PLAYER','参赛队员','参赛答题、writeup 提交',1),
 (5,'VIEWER','观摩用户','大屏观赛、赛后数据查询',1)
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

INSERT INTO sys_permission (id, code, name, module, type) VALUES
 (1001,'*','超级权限','system','API'),
 (1101,'contest:create','创建赛事','contest','API'),
 (1102,'contest:update','编辑赛事','contest','API'),
 (1103,'contest:delete','删除赛事','contest','API'),
 (1104,'contest:control','赛事控制(暂停/强制结束/封号/加分)','contest','API'),
 (1105,'contest:view','查看赛事','contest','API'),
 (1201,'question:create','新建题目','question','API'),
 (1202,'question:update','编辑题目','question','API'),
 (1203,'question:delete','删除题目','question','API'),
 (1204,'question:view','查看题目','question','API'),
 (1301,'team:manage','战队管理','team','API'),
 (1401,'user:manage','用户管理','user','API'),
 (1501,'awd:control','AWD轮次控制','awd','API'),
 (1601,'report:export','报表导出','report','API'),
 (1701,'audit:view','审计查询','audit','API')
ON DUPLICATE KEY UPDATE name=VALUES(name), module=VALUES(module);

-- 管理员拥有全部
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (1, 1001);

-- 裁判权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
 (2,1101),(2,1102),(2,1104),(2,1105),(2,1204),(2,1301),(2,1501),(2,1601);

-- 教师权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
 (3,1101),(3,1105),(3,1201),(3,1202),(3,1204),(3,1301),(3,1601);

-- 参赛队员
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (4,1105),(4,1204);

-- 观摩用户
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (5,1105);

-- 默认管理员 admin / Admin@123
INSERT INTO sys_user (id, username, password, nickname, real_name, status) VALUES
 (1000000000000000001,'admin','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','超级管理员','系统管理员',1)
ON DUPLICATE KEY UPDATE nickname=VALUES(nickname);

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1000000000000000001, 1);
