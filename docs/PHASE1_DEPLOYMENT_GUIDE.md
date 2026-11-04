# Phase 1 完整部署指南

## 概述

本文档提供 Phase 1 所有功能的完整部署步骤，包括数据库迁移、后端编译、前端构建和部署。

## 前置条件

### 系统要求
- Docker 和 Docker Compose
- Node.js 18+ 和 npm
- Java 21 和 Maven 3.9+
- MySQL 8.0+
- Redis 7.0+

### 网络要求
- 能够访问 Maven 中央仓库（或使用国内镜像）
- 能够访问 npm  registry

## 部署步骤

### 步骤 1: 数据库迁移

#### 1.1 确认 MySQL 服务运行
```bash
docker ps | grep mysql
```

如果未运行，启动 MySQL：
```bash
docker start mysql
```

#### 1.2 执行迁移脚本

**方法 A: 使用 Docker MySQL 客户端（推荐）**

逐个执行迁移脚本：
```bash
cd D:\tmp\网安平台项目\旧项目源码\网安平台项目\deploy\sql

# 1. ABAC 策略表
docker exec -i mysql mysql -uroot -p csp_platform < 18_abac_policy.sql

# 2. 队伍邀请码表
docker exec -i mysql mysql -uroot -p csp_platform < 19_team_invite_code.sql

# 3. 反作弊深度分析表
docker exec -i mysql mysql -uroot -p csp_platform < 20_anti_cheat_deep.sql

# 4. AWD 流量取证表
docker exec -i mysql mysql -uroot -p csp_platform < 21_awd_traffic_capture.sql
```

执行时会提示输入密码，输入 MySQL root 密码。

**方法 B: 使用 MySQL Workbench**

1. 打开 MySQL Workbench 并连接到本地 MySQL
2. 选择 `csp_platform` 数据库
3. 依次打开并执行以下 SQL 文件：
   - `deploy/sql/18_abac_policy.sql`
   - `deploy/sql/19_team_invite_code.sql`
   - `deploy/sql/20_anti_cheat_deep.sql`
   - `deploy/sql/21_awd_traffic_capture.sql`

#### 1.3 验证迁移

```sql
-- 查看所有新表
SHOW TABLES LIKE 'abac_%';
SHOW TABLES LIKE 'contest_team_%';
SHOW TABLES LIKE 'device_%';
SHOW TABLES LIKE 'cheat_%';
SHOW TABLES LIKE 'awd_traffic_%';

-- 查看预置数据
SELECT COUNT(*) FROM abac_policy;  -- 应为 5
SELECT COUNT(*) FROM sys_config WHERE config_key LIKE 'awd.traffic%';  -- 应为 8
```

### 步骤 2: 后端编译

#### 2.1 配置 Maven 镜像（如果网络有问题）

已创建 `backend/settings.xml`，使用阿里云镜像：
```xml
<mirrors>
    <mirror>
        <id>aliyunmaven</id>
        <url>https://maven.aliyun.com/repository/public</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
</mirrors>
```

#### 2.2 编译后端

**方法 A: 使用 Docker Maven（推荐）**
```bash
cd D:\tmp\网安平台项目\旧项目源码\网安平台项目\backend

# 使用阿里云镜像编译
docker run --rm \
  -v ${PWD}:/app \
  -v ${PWD}/settings.xml:/root/.m2/settings.xml \
  -w /app \
  maven:3.9-eclipse-temurin-21 \
  mvn clean package -DskipTests
```

**方法 B: 使用本地 Maven**
```bash
cd D:\tmp\网安平台项目\旧项目源码\网安平台项目\backend

# 使用阿里云镜像
mvn clean package -DskipTests -s settings.xml
```

#### 2.3 验证编译

编译成功后，会在各模块的 `target` 目录生成 JAR 文件：
```bash
ls csp-platform/target/*.jar
# 应看到 csp-platform-1.0.0.jar
```

### 步骤 3: 启动后端服务

#### 3.1 配置环境变量

创建 `.env` 文件（或在 `application.yml` 中配置）：
```env
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=csp_platform
DB_USER=root
DB_PASSWORD=root123

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# MinIO 配置（可选）
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# AWD 流量捕获配置
AWD_TRAFFIC_STORAGE_PATH=/tmp/awd-traffic
```

#### 3.2 启动服务

**方法 A: 使用 Docker Compose**
```bash
cd D:\tmp\网安平台项目\旧项目源码\网安平台项目\deploy
docker-compose up -d backend
```

**方法 B: 直接运行 JAR**
```bash
cd D:\tmp\网安平台项目\旧项目源码\网安平台项目\backend\csp-platform\target
java -jar csp-platform-1.0.0.jar
```

#### 3.3 验证启动

```bash
# 检查服务状态
curl http://localhost:8080/actuator/health

# 应返回：{"status":"UP"}
```

### 步骤 4: API 测试

#### 4.1 使用 Postman 测试

1. 导入 Postman 集合：
   - 打开 Postman
   - Import → File → 选择 `deploy/postman_collection_phase1.json`

2. 登录获取 Token：
   - 执行 "0. 登录获取Token" → "管理员登录"
   - Token 会自动保存到集合变量

3. 测试各个 API：
   - ABAC 策略管理
   - 队伍邀请码
   - 反作弊分析
   - 流量取证

#### 4.2 使用 Bash 脚本测试

```bash
cd D:\tmp\网安平台项目\旧项目源码\网安平台项目\deploy
chmod +x test_phase1_api.sh
./test_phase1_api.sh
```

### 步骤 5: 前端构建

#### 5.1 安装依赖

```bash
cd D:\tmp\网安平台项目\旧项目源码\网安平台项目\frontend
npm install
```

#### 5.2 构建生产版本

```bash
npm run build
```

构建完成后，会在 `dist` 目录生成静态文件。

#### 5.3 部署到 Nginx

**方法 A: 使用 Docker Nginx**
```bash
cd D:\tmp\网安平台项目\旧项目源码\网安平台项目\deploy

# 复制构建产物
cp -r ../frontend/dist ./nginx/html/

# 启动 Nginx
docker-compose up -d nginx
```

**方法 B: 使用本地 Nginx**

1. 复制构建产物到 Nginx 目录：
```bash
sudo cp -r dist/* /usr/share/nginx/html/
```

2. 配置 Nginx（`/etc/nginx/conf.d/csp.conf`）：
```nginx
server {
    listen 80;
    server_name localhost;
    
    root /usr/share/nginx/html;
    index index.html;
    
    # SPA 路由
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

3. 重启 Nginx：
```bash
sudo nginx -t
sudo systemctl reload nginx
```

#### 5.4 验证前端

打开浏览器访问：`http://localhost`

### 步骤 6: 功能验证

#### 6.1 ABAC 策略管理

1. 登录管理后台
2. 访问 `/console/abac`
3. 测试功能：
   - 创建策略
   - 编辑策略
   - 启用/禁用策略
   - 测试策略评估

#### 6.2 队伍邀请码

1. 登录选手端
2. 进入赛事报名页面
3. 测试功能：
   - 创建战队（自动生成邀请码）
   - 复制邀请码
   - 刷新邀请码
   - 通过邀请码加入

#### 6.3 反作弊监控

1. 登录管理后台
2. 访问 `/console/anti-cheat/{contestId}`
3. 测试功能：
   - 查看风险统计
   - 查看高风险用户列表
   - 查看用户风险详情
   - 查看风险分布图

#### 6.4 AWD 流量取证

1. 登录 AWD 赛事页面
2. 访问 `/portal/awd/{contestId}/traffic`
3. 测试功能：
   - 启动流量捕获
   - 查看捕获列表
   - 停止捕获
   - 查看分析结果
   - 下载 pcap 文件

## 故障排查

### 问题 1: Maven 编译失败（SSL 错误）

**原因**：网络问题导致无法访问 Maven Central

**解决**：
1. 使用阿里云镜像（已配置在 `settings.xml`）
2. 检查网络连接
3. 检查防火墙设置

### 问题 2: 数据库迁移失败

**原因**：表已存在或字段已存在

**解决**：
- SQL 脚本使用 `IF NOT EXISTS`，可重复执行
- 检查数据库字符集是否为 `utf8mb4`

### 问题 3: 后端启动失败

**原因**：数据库连接失败或 Redis 连接失败

**解决**：
1. 检查 MySQL 和 Redis 服务是否运行
2. 检查数据库配置是否正确
3. 查看日志：`logs/csp-platform.log`

### 问题 4: 前端页面空白

**原因**：API 请求失败或路由配置错误

**解决**：
1. 打开浏览器开发者工具查看 Console 错误
2. 检查 Network 面板的 API 请求
3. 确认后端服务已启动

### 问题 5: 流量捕获失败

**原因**：tcpdump 未安装或权限不足

**解决**：
1. 安装 tcpdump：
   ```bash
   # Ubuntu/Debian
   sudo apt-get install tcpdump
   
   # CentOS/RHEL
   sudo yum install tcpdump
   ```

2. 确保运行用户有 CAP_NET_RAW 能力：
   ```bash
   sudo setcap cap_net_raw+ep /usr/bin/tcpdump
   ```

## 性能优化

### 数据库优化

1. 添加索引（已在 SQL 脚本中包含）
2. 定期清理过期数据：
   ```sql
   -- 清理过期的流量捕获记录
   DELETE FROM awd_traffic_capture WHERE expires_at < NOW();
   ```

### Redis 优化

1. 配置内存限制：
   ```conf
   maxmemory 2gb
   maxmemory-policy allkeys-lru
   ```

2. 启用持久化：
   ```conf
   save 900 1
   save 300 10
   save 60 10000
   ```

### 前端优化

1. 启用 Gzip 压缩：
   ```nginx
   gzip on;
   gzip_types text/plain text/css application/json application/javascript;
   ```

2. 配置缓存：
   ```nginx
   location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
       expires 1y;
       add_header Cache-Control "public, immutable";
   }
   ```

## 监控和日志

### 后端监控

1. Actuator 端点：
   ```
   http://localhost:8080/actuator/health
   http://localhost:8080/actuator/metrics
   http://localhost:8080/actuator/loggers
   ```

2. 日志配置（`application.yml`）：
   ```yaml
   logging:
     level:
       com.csp: INFO
       org.springframework: WARN
     file:
       name: logs/csp-platform.log
   ```

### 前端监控

1. 错误追踪：集成 Sentry
2. 性能监控：使用 Lighthouse

## 安全加固

### 1. 修改默认密码

```sql
-- 修改 MySQL root 密码
ALTER USER 'root'@'localhost' IDENTIFIED BY '新密码';

-- 修改 Redis 密码
CONFIG SET requirepass 新密码
```

### 2. 配置 HTTPS

```nginx
server {
    listen 443 ssl;
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    # ... 其他配置
}
```

### 3. 限制 API 访问

```nginx
location /api/admin/ {
    allow 127.0.0.1;
    deny all;
    # ... 其他配置
}
```

## 备份和恢复

### 数据库备份

```bash
# 备份所有数据库
mysqldump -uroot -p --all-databases > backup_$(date +%Y%m%d).sql

# 备份指定数据库
mysqldump -uroot -p csp_platform > csp_backup_$(date +%Y%m%d).sql
```

### 数据库恢复

```bash
mysql -uroot -p csp_platform < csp_backup_20261104.sql
```

### 文件备份

```bash
# 备份流量捕获文件
tar -czf traffic_backup_$(date +%Y%m%d).tar.gz /tmp/awd-traffic/
```

## 总结

完成以上步骤后，Phase 1 的所有功能应该已经成功部署并可以正常使用。

**检查清单**：
- [ ] 数据库迁移执行成功
- [ ] 后端编译成功
- [ ] 后端服务启动成功
- [ ] API 测试通过
- [ ] 前端构建成功
- [ ] 前端部署成功
- [ ] 所有功能验证通过

如有问题，请参考"故障排查"章节。
