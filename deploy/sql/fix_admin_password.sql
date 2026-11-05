-- ============================================================
-- 管理员口令复位：把 admin 的密码固定回交接凭据 Admin@123
-- 哈希由 Spring Security 的 BCrypt（cost 10）生成并已自校验通过
-- 用法：docker exec -i csp-mysql mysql -uroot -pcsp_root_2026 csp < deploy/sql/fix_admin_password.sql
-- 与 02_seed.sql 中的哈希保持一致，改口令时两处一起更新
-- ============================================================
SET NAMES utf8mb4;
UPDATE sys_user SET password='$2b$10$kygISDtIDLGp6f14nTjbE.SVb5whcgr9G0kOD/znV0AziY1JQFQYG' WHERE username='admin';
