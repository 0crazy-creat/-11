-- ============================================================
-- 角色协作矩阵（五角色 × 六赛制）
-- ADMIN(全权) / JUDGE(判题、申诉、调分) / TEACHER(建赛组卷、看成绩)
-- / PLAYER(报名、答题、起靶机) / OBSERVER(只读看榜)
-- 说明：以 02_seed.sql 为基线做增量补齐，不改动基线；幂等可重复执行。
-- ============================================================
SET NAMES utf8mb4;

-- 1) 补齐 OBSERVER 角色（原 VIEWER 保留兼容），语义与「只读看榜」一致
INSERT INTO sys_role (id, code, name, description, builtin) VALUES
 (6,'OBSERVER','观察者','只读查看赛事与实时榜单，不可参赛/管理',1)
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- 2) TEACHER：建赛组卷 + 看成绩（成绩走 report:export / contest:view）
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
 (3,1101),(3,1102),(3,1105),(3,1106),(3,1201),(3,1202),(3,1204),(3,1301),(3,1601);

-- 3) JUDGE：判题、申诉、调分（调分走 contest:control）
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
 (2,1101),(2,1102),(2,1104),(2,1105),(2,1106),(2,1204),(2,1301),(2,1501),(2,1502),(2,1503),(2,1601),(2,1901);

-- 4) PLAYER：报名(contest:view)、答题(question:view)、起靶机(contest:view)、申诉(awd:dispute)
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
 (4,1105),(4,1204),(4,1502);

-- 5) OBSERVER：只读看榜（contest:view）
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
 (6,1105);
