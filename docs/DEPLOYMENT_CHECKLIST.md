# Phase 1 部署检查清单

## 部署前检查

### 环境准备
- [ ] Docker 和 Docker Compose 已安装
- [ ] Node.js 18+ 和 npm 已安装
- [ ] Java 21 已安装（如果使用本地 Maven）
- [ ] Maven 3.9+ 已安装（如果使用本地 Maven）
- [ ] MySQL 8.0+ 已安装并运行
- [ ] Redis 7.0+ 已安装并运行
- [ ] Nginx 已安装（用于前端部署）

### 网络检查
- [ ] 能够访问 Maven 仓库（或使用阿里云镜像）
- [ ] 能够访问 npm registry
- [ ] 端口 8080（后端）可用
- [ ] 端口 80（前端）可用
- [ ] 端口 3306（MySQL）可用
- [ ] 端口 6379（Redis）可用

### 代码检查
- [ ] 所有代码文件已就位
- [ ] 数据库迁移脚本已准备（4 个 SQL 文件）
- [ ] Maven settings.xml 已配置（阿里云镜像）
- [ ] 前端路由已配置
- [ ] API 测试脚本已准备

## 部署步骤

### 步骤 1: 数据库迁移
- [ ] 确认 MySQL 服务运行
- [ ] 执行 `18_abac_policy.sql`
- [ ] 执行 `19_team_invite_code.sql`
- [ ] 执行 `20_anti_cheat_deep.sql`
- [ ] 执行 `21_awd_traffic_capture.sql`
- [ ] 验证表创建成功
- [ ] 验证预置数据插入成功

**验证命令**：
```sql
SHOW TABLES LIKE 'abac_%';
SHOW TABLES LIKE 'device_%';
SHOW TABLES LIKE 'cheat_%';
SHOW TABLES LIKE 'awd_traffic_%';
SELECT COUNT(*) FROM abac_policy;  -- 应为 5
```

### 步骤 2: 后端编译
- [ ] 进入 backend 目录
- [ ] 使用 Docker Maven 编译（或本地 Maven）
- [ ] 等待编译完成
- [ ] 检查是否有编译错误
- [ ] 验证 JAR 文件生成

**验证命令**：
```bash
ls csp-platform/target/*.jar
# 应看到 csp-platform-1.0.0.jar
```

### 步骤 3: 启动后端服务
- [ ] 配置环境变量（数据库、Redis 等）
- [ ] 启动后端服务（Docker Compose 或直接运行）
- [ ] 等待服务启动（约 10-30 秒）
- [ ] 检查服务健康状态

**验证命令**：
```bash
curl http://localhost:8080/actuator/health
# 应返回：{"status":"UP"}
```

### 步骤 4: API 测试
- [ ] 导入 Postman 集合
- [ ] 登录获取 Token
- [ ] 测试 ABAC 策略 API
- [ ] 测试队伍邀请码 API
- [ ] 测试反作弊分析 API
- [ ] 测试流量取证 API
- [ ] 记录测试结果

**验证方法**：
- 使用 Postman 或 `test_phase1_api.sh`
- 检查所有 API 返回 200 状态码
- 验证数据正确性

### 步骤 5: 前端构建
- [ ] 进入 frontend 目录
- [ ] 安装依赖（npm install）
- [ ] 构建生产版本（npm run build）
- [ ] 检查构建是否成功
- [ ] 验证 dist 目录生成

**验证命令**：
```bash
ls dist/
# 应看到 index.html, assets/ 等
```

### 步骤 6: 前端部署
- [ ] 复制 dist 文件到 Nginx 目录
- [ ] 配置 Nginx（SPA 路由、API 代理）
- [ ] 重启 Nginx
- [ ] 检查前端是否可访问

**验证方法**：
- 打开浏览器访问 http://localhost
- 检查页面是否正常加载
- 检查 Console 是否有错误

## 功能验证

### ABAC 策略管理
- [ ] 访问 `/console/abac`
- [ ] 创建新策略
- [ ] 编辑策略
- [ ] 启用/禁用策略
- [ ] 测试策略评估
- [ ] 删除策略

### 队伍邀请码
- [ ] 进入赛事报名页面
- [ ] 创建战队（自动生成邀请码）
- [ ] 查看邀请码
- [ ] 复制邀请码
- [ ] 刷新邀请码
- [ ] 通过邀请码加入战队

### 反作弊监控
- [ ] 访问 `/console/anti-cheat/{contestId}`
- [ ] 查看风险统计
- [ ] 查看高风险用户列表
- [ ] 查看用户风险详情
- [ ] 查看风险分布图
- [ ] 暂停/标记用户

### AWD 流量取证
- [ ] 访问 `/portal/awd/{contestId}/traffic`
- [ ] 启动流量捕获
- [ ] 查看捕获列表
- [ ] 查看捕获详情
- [ ] 停止捕获
- [ ] 查看分析结果
- [ ] 下载 pcap 文件

## 性能检查

### 数据库性能
- [ ] 检查慢查询日志
- [ ] 验证索引是否生效
- [ ] 测试大数据量查询

### Redis 性能
- [ ] 检查内存使用
- [ ] 验证 Key 过期策略
- [ ] 测试高并发场景

### 前端性能
- [ ] 检查页面加载时间
- [ ] 验证代码分割效果
- [ ] 测试移动端适配

## 安全检查

### 权限控制
- [ ] 验证 ABAC 策略管理需要 `abac:view` 权限
- [ ] 验证反作弊查询需要 `contest:control` 权限
- [ ] 验证流量捕获管理需要 `contest:control` 权限

### 数据脱敏
- [ ] 验证提交记录中的 flag 已哈希
- [ ] 验证设备指纹仅存储哈希值
- [ ] 验证审计日志中的敏感信息已加密

### 访问控制
- [ ] 验证 API 接口使用 JWT 认证
- [ ] 验证管理接口有 IP 白名单（如配置）
- [ ] 验证敏感操作记录审计日志

## 监控告警

### 后端监控
- [ ] 配置 Actuator 端点
- [ ] 设置日志级别
- [ ] 配置告警规则

### 前端监控
- [ ] 集成错误追踪（如 Sentry）
- [ ] 配置性能监控（如 Lighthouse）

## 备份恢复

### 数据库备份
- [ ] 执行首次备份
- [ ] 验证备份文件完整性
- [ ] 测试恢复流程

### 文件备份
- [ ] 备份流量捕获文件
- [ ] 备份配置文件

## 文档检查

### 实现文档
- [ ] ABAC_IMPLEMENTATION.md
- [ ] TEAM_INVITE_CODE_IMPLEMENTATION.md
- [ ] AWD_AUTO_REGISTRATION_IMPLEMENTATION.md
- [ ] CTF_ANTI_CHEAT_DEEP_IMPLEMENTATION.md
- [ ] AWD_TRAFFIC_CAPTURE_IMPLEMENTATION.md

### 部署文档
- [ ] DATABASE_MIGRATION_GUIDE.md
- [ ] PHASE1_DEPLOYMENT_GUIDE.md
- [ ] PHASE1_FINAL_REPORT.md

### API 文档
- [ ] Swagger UI 可访问
- [ ] API 端点文档完整

## 问题记录

### 已知问题
1. _（记录部署过程中遇到的问题）_

### 待解决问题
1. _（记录需要后续解决的问题）_

## 签署确认

- [ ] 部署完成
- [ ] 所有功能验证通过
- [ ] 性能检查通过
- [ ] 安全检查通过
- [ ] 文档齐全

**部署人员**：_______________

**部署日期**：_______________

**审核人员**：_______________

**审核日期**：_______________

---

## 快速部署命令

### 一键部署（Linux/Mac）
```bash
chmod +x quickstart.sh
./quickstart.sh
```

### 一键部署（Windows）
```cmd
quickstart.bat
```

### 手动部署步骤

#### 1. 数据库迁移
```bash
cd deploy/sql
docker exec -i mysql mysql -uroot -p csp_platform < 18_abac_policy.sql
docker exec -i mysql mysql -uroot -p csp_platform < 19_team_invite_code.sql
docker exec -i mysql mysql -uroot -p csp_platform < 20_anti_cheat_deep.sql
docker exec -i mysql mysql -uroot -p csp_platform < 21_awd_traffic_capture.sql
```

#### 2. 后端编译
```bash
cd backend
docker run --rm -v ${PWD}:/app -v ${PWD}/settings.xml:/root/.m2/settings.xml -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests
```

#### 3. 启动后端
```bash
cd deploy
docker-compose up -d backend
```

#### 4. 前端构建
```bash
cd frontend
npm install
npm run build
```

#### 5. 部署前端
```bash
sudo cp -r dist/* /usr/share/nginx/html/
sudo systemctl reload nginx
```

#### 6. 验证部署
```bash
# 检查后端
curl http://localhost:8080/actuator/health

# 检查前端
curl http://localhost

# 运行 API 测试
./deploy/test_phase1_api.sh
```
