# CSP Arena — 独立靶场服务

网络安全竞赛平台的**独立靶场子系统**，与主平台（11001）解耦，独立库 `csp_arena`、独立端口 **11002**，可单独部署运维。

## 一、快速开始

```bash
# 1. 初始化数据库
docker exec -i csp-mysql mysql -uroot -pcsp_root_2026 < deploy/sql/01_arena_schema.sql
docker exec -i csp-mysql mysql -uroot -pcsp_root_2026 < deploy/sql/02_arena_seed.sql

# 2. 构建基础靶场镜像
cd deploy/targets/basic-web && docker build -t csp/arena-basic-web:1.0 .

# 3. 构建并启动服务
cd backend && mvn clean package -DskipTests
nohup java -jar target/csp-arena.jar > /tmp/csp-arena.log 2>&1 &

# 4. 验证
bash deploy/scripts/arena-verify.sh
```

## 二、功能清单

| 模块 | 能力 |
|---|---|
| 模板管理 | 增删改查、按编码查询、启用/停用、分类筛选、关键字搜索 |
| 靶机生命周期 | 创建（自动签发 flag）、重置（换 flag）、销毁、批量销毁 |
| 端口池 | 21000-22000 自动分配，冲突检测 |
| 网络隔离 | 按赛事创建独立 bridge 网络 `arena-net-{contestId}` |
| 资源限制 | 模板级 CPU/内存限制，实例 TTL 到期自动回收 |
| 健康巡检 | 单台/全量巡检，定时任务 60s 一轮 |
| 资源监控 | CPU/内存占用采集，监控列表供大屏使用 |
| 统计总览 | 模板数、实例数、运行/停止/异常数、分类与状态分布 |
| 审计日志 | 创建/重置/销毁/巡检全记录，含耗时与操作人 |
| 降级能力 | Docker 不可用时自动模拟模式，业务链路仍可验证 |

## 三、接口一览（前缀 `http://localhost:11002/api`）

### 健康
- `GET /arena/health` — 服务与 Docker 可用性

### 模板
- `GET /templates?page&size&category&keyword&status`
- `GET /templates/enabled` — 启用模板下拉
- `GET /templates/{id}` / `GET /templates/code/{code}`
- `POST /templates` / `PUT /templates/{id}` / `DELETE /templates/{id}`
- `PUT /templates/{id}/status?status=1|0`

### 实例
- `GET /instances?page&size&templateId&status&ownerId&contestId&teamId`
- `GET /instances/{id}?showFlag=true`
- `GET /instances/mine?ownerId=`
- `POST /instances?ownerId=&ownerName=` body: `{templateId, contestId?, teamId?, flag?, ttlMinutes?}`
- `POST /instances/{id}/reset?actorId=`
- `POST /instances/{id}/destroy?actorId=`
- `POST /instances/{id}/health-check?actorId=`
- `POST /instances/health-check-all`
- `POST /instances/destroy-all?ownerId=&contestId=`
- `POST /instances/reclaim-expired`
- `GET /instances/stats` — 总览统计
- `GET /instances/monitor?contestId=&ownerId=` — 监控列表
- `GET /instances/logs?instanceId=&limit=` — 审计日志

## 四、内置模板（种子数据）

| 编码 | 名称 | 镜像 | 难度 | TTL |
|---|---|---|---|---|
| basic-web | 基础 Web 靶场 | csp/arena-basic-web:1.0 | EASY | 120m |
| basic-web-2 | 基础 Web 靶场（副本） | csp/arena-basic-web:1.0 | EASY | 120m |
| nginx-demo | Nginx 静态靶场 | nginx:alpine | EASY | 60m |
| alpine-shell | Alpine 容器靶场 | alpine:latest | EASY | 60m |

## 五、基础靶场镜像说明

`csp/arena-basic-web:1.0` 提供：
- `/` — 靶场首页
- `/healthz` — 健康探针（返回 `ok`）
- `/flag.txt` — flag 回显
- 响应头 `X-Target-Flag` — flag 注入验证

flag 由编排层通过环境变量 `FLAG` 注入，容器启动脚本写入 nginx 配置与静态文件。

## 六、数据表

- `arena_template` — 靶场模板
- `arena_instance` — 靶机实例
- `arena_action_log` — 操作审计
