# Phase 1 数据库迁移执行指南

## 前置条件

确保 MySQL 服务正在运行：
```bash
docker ps | grep mysql
```

如果未运行，启动 MySQL：
```bash
docker start mysql
```

## 执行迁移

### 方法 1: 使用 Docker MySQL 客户端（推荐）

逐个执行迁移脚本：

```bash
# 1. ABAC 策略表
docker exec -i mysql mysql -uroot -p csp_platform < deploy/sql/18_abac_policy.sql

# 2. 队伍邀请码表
docker exec -i mysql mysql -uroot -p csp_platform < deploy/sql/19_team_invite_code.sql

# 3. 反作弊深度分析表
docker exec -i mysql mysql -uroot -p csp_platform < deploy/sql/20_anti_cheat_deep.sql

# 4. AWD 流量取证表
docker exec -i mysql mysql -uroot -p csp_platform < deploy/sql/21_awd_traffic_capture.sql
```

执行时会提示输入密码，输入 MySQL root 密码即可。

### 方法 2: 使用 MySQL Workbench

1. 打开 MySQL Workbench 并连接到本地 MySQL
2. 选择 `csp_platform` 数据库
3. 依次打开并执行以下 SQL 文件：
   - `deploy/sql/18_abac_policy.sql`
   - `deploy/sql/19_team_invite_code.sql`
   - `deploy/sql/20_anti_cheat_deep.sql`
   - `deploy/sql/21_awd_traffic_capture.sql`

### 方法 3: 使用命令行 MySQL 客户端

如果本地安装了 MySQL 客户端：

```bash
mysql -hlocalhost -uroot -p csp_platform < deploy/sql/18_abac_policy.sql
mysql -hlocalhost -uroot -p csp_platform < deploy/sql/19_team_invite_code.sql
mysql -hlocalhost -uroot -p csp_platform < deploy/sql/20_anti_cheat_deep.sql
mysql -hlocalhost -uroot -p csp_platform < deploy/sql/21_awd_traffic_capture.sql
```

## 验证迁移

执行以下 SQL 查询验证表是否创建成功：

```sql
-- 查看所有新表
SHOW TABLES LIKE 'abac_%';
SHOW TABLES LIKE 'contest_team_%';
SHOW TABLES LIKE 'device_%';
SHOW TABLES LIKE 'cheat_%';
SHOW TABLES LIKE 'awd_traffic_%';

-- 查看表结构
DESCRIBE abac_policy;
DESCRIBE abac_audit_log;
DESCRIBE device_fingerprint;
DESCRIBE cheat_audit_log;
DESCRIBE awd_traffic_capture;
DESCRIBE awd_traffic_analysis;

-- 查看预置数据
SELECT * FROM abac_policy;
SELECT * FROM sys_config WHERE config_key LIKE 'awd.traffic%';
```

## 预期结果

### 新增表（8 张）

1. `abac_policy` - ABAC 策略表（5 条预置数据）
2. `abac_audit_log` - ABAC 审计日志表
3. `contest_team_captain_transfer` - 队长转让记录表
4. `contest_team_invite` - 邀请记录表
5. `device_fingerprint` - 设备指纹表
6. `cheat_audit_log` - 作弊审计日志表
7. `awd_traffic_capture` - 流量捕获表
8. `awd_traffic_analysis` - 流量分析结果表

### 扩展表（1 张）

1. `contest_team` - 新增字段：
   - `invite_code` VARCHAR(8)
   - `invite_expire_at` DATETIME
   - `max_members` INT
   - `description` VARCHAR(500)
   - `slogan` VARCHAR(200)

### 预置配置（8 条）

`sys_config` 表新增 AWD 流量捕获配置：
- `awd.traffic.capture.enabled` = true
- `awd.traffic.capture.duration` = 300
- `awd.traffic.capture.filter` = "tcp or udp or icmp"
- `awd.traffic.storage.type` = "MINIO"
- `awd.traffic.storage.bucket` = "awd-traffic"
- `awd.traffic.retention.days` = 30
- `awd.traffic.analysis.auto` = true
- `awd.traffic.analysis.rules` = ["SYN_FLOOD","PORT_SCAN","SQL_INJECTION","XSS_ATTACK"]

## 回滚方案

如果迁移失败，可以删除新增表：

```sql
DROP TABLE IF EXISTS abac_audit_log;
DROP TABLE IF EXISTS abac_policy;
DROP TABLE IF EXISTS contest_team_captain_transfer;
DROP TABLE IF EXISTS contest_team_invite;
DROP TABLE IF EXISTS device_fingerprint;
DROP TABLE IF EXISTS cheat_audit_log;
DROP TABLE IF EXISTS awd_traffic_analysis;
DROP TABLE IF EXISTS awd_traffic_capture;

-- 删除 contest_team 新增字段
ALTER TABLE contest_team DROP COLUMN invite_code;
ALTER TABLE contest_team DROP COLUMN invite_expire_at;
ALTER TABLE contest_team DROP COLUMN max_members;
ALTER TABLE contest_team DROP COLUMN description;
ALTER TABLE contest_team DROP COLUMN slogan;

-- 删除预置配置
DELETE FROM sys_config WHERE config_key LIKE 'awd.traffic%';
```

## 注意事项

1. **备份数据库**：执行迁移前建议备份
2. **字符集**：确保数据库使用 utf8mb4 字符集
3. **权限**：需要数据库管理员权限
4. **顺序**：按编号顺序执行（18 → 19 → 20 → 21）

## 故障排查

### 问题：表已存在

**解决**：SQL 脚本使用 `CREATE TABLE IF NOT EXISTS`，可重复执行

### 问题：字段已存在

**解决**：迁移脚本包含 `IF NOT EXISTS` 和 `ON DUPLICATE KEY UPDATE`，可重复执行

### 问题：权限不足

**解决**：使用 root 用户或有 CREATE/ALTER/INSERT 权限的用户

### 问题：字符集错误

**解决**：
```sql
ALTER DATABASE csp_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
