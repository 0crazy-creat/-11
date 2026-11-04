SET NAMES utf8mb4;
USE csp;

-- ============ 1. 清理测试赛事（保留 3 场规范演示赛）============
DELETE FROM contest_question WHERE contest_id IN (
  SELECT id FROM contest WHERE code LIKE 'V%' OR code LIKE 'FX%' OR code LIKE 'E2E%'
     OR code LIKE 'FULL%' OR code LIKE 'FCX%' OR code LIKE 'CTF-TEST%' OR code='V221710');
DELETE FROM contest_team WHERE contest_id IN (
  SELECT id FROM contest WHERE code LIKE 'V%' OR code LIKE 'FX%' OR code LIKE 'E2E%'
     OR code LIKE 'FULL%' OR code LIKE 'FCX%' OR code LIKE 'CTF-TEST%' OR code='V221710');
DELETE FROM submission WHERE contest_id IN (
  SELECT id FROM contest WHERE code LIKE 'V%' OR code LIKE 'FX%' OR code LIKE 'E2E%'
     OR code LIKE 'FULL%' OR code LIKE 'FCX%' OR code LIKE 'CTF-TEST%' OR code='V221710');
DELETE FROM contest_archive WHERE contest_id IN (
  SELECT id FROM contest WHERE code LIKE 'V%' OR code LIKE 'FX%' OR code LIKE 'E2E%'
     OR code LIKE 'FULL%' OR code LIKE 'FCX%' OR code LIKE 'CTF-TEST%' OR code='V221710');
DELETE FROM contest WHERE code LIKE 'V%' OR code LIKE 'FX%' OR code LIKE 'E2E%'
     OR code LIKE 'FULL%' OR code LIKE 'FCX%' OR code LIKE 'CTF-TEST%' OR code='V221710';

-- ============ 2. 清理测试角色 ============
DELETE FROM sys_role_permission WHERE role_id IN (
  SELECT id FROM sys_role WHERE code LIKE 'T%' OR code LIKE 'DEL%' OR code='TESTER');
DELETE FROM sys_user_role WHERE role_id IN (
  SELECT id FROM sys_role WHERE code LIKE 'T%' OR code LIKE 'DEL%' OR code='TESTER');
DELETE FROM sys_role WHERE code LIKE 'T%' OR code LIKE 'DEL%' OR code='TESTER';

-- ============ 3. 清理测试题目（乱码/临时）============
DELETE FROM question WHERE title IS NULL OR title='' OR title LIKE '%测试%' OR title LIKE '%???%' OR title REGEXP '^[?]+$';

SELECT '=== 清理后赛事 ===' AS '';
SELECT c.id, c.code, c.name, c.type, c.status, COUNT(cq.question_id) qs
FROM contest c LEFT JOIN contest_question cq ON cq.contest_id=c.id
WHERE c.deleted=0 GROUP BY c.id;
SELECT '=== 角色 ===' AS '';
SELECT id,code,name FROM sys_role;
SELECT '=== 题目 ===' AS '';
SELECT bank_type, COUNT(*) FROM question WHERE deleted=0 GROUP BY bank_type;
