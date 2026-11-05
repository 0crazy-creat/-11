# 网络安全竞赛平台 · 验收说明

## 一、访问入口与凭据

### 运行中的部署（Docker，验收走这里）

| 项目 | 地址 |
|---|---|
| 平台首页（前端 SPA） | http://localhost/ |
| 登录页 | http://localhost/login |
| 控制台 | http://localhost/console |
| 3D 态势大屏 | http://localhost/situation |
| 排行榜 | http://localhost/rank |
| 战队主页 | http://localhost/team-home/{teamId} |
| 后端 API（直连） | http://localhost:8080/api |
| 接口文档 | http://localhost:8080/swagger-ui/index.html |

容器：`csp-frontend`（nginx :80，`/api`、`/ws` 反代 :8080，bind mount `frontend/dist`，构建即上线）、
`csp-backend`（:8080，jar 在容器内 `/app/app.jar`，热替换需 `docker cp`）、
`csp-mysql`（:3306，库名 `csp`）、`csp-redis`（:6379）、`csp-mongo`（:27017）。

### 单端口验收模式

`./deploy/scripts/run-11001.sh` 以本地 jar 在 11001 同时提供 SPA 与 API
（需先把前端产物拷进 `backend/csp-platform/src/main/resources/static/`）。
各类验收脚本默认指向 Docker 入口，切该模式时显式传 `BASE=http://127.0.0.1:11001`。

### 账号（唯一口径）

| 账号 | 口令 | 角色 | 说明 |
|---|---|---|---|
| `admin` | `Admin@123` | ADMIN | 交接管理员，口令固定，`mustChangePassword=false`，不强制首次修改 |

- 哈希为 BCrypt cost 10，固化在 `deploy/sql/02_seed.sql`；`deploy/sql/fix_admin_password.sql` 用同一哈希做复位，
  两处必须一起改，否则新环境与线上会分叉。
- **任何测试/验收脚本都不得修改 admin 口令**，改密类接口用一次性注册账号验证。
  历史故障正是 `api-full-test.sh` 把 admin 改成 `Admin@999`，而兜底复位依赖 `python3 + bcrypt`，
  缺库时静默跳过，环境就此失配。
- 登录不上时先复位再验证：

  ```bash
  docker exec -i csp-mysql mysql -uroot -pcsp_root_2026 csp < deploy/sql/fix_admin_password.sql
  curl -s -X POST http://localhost/api/auth/login \
       -H 'Content-Type: application/json' \
       -d '{"username":"admin","password":"Admin@123"}'
  ```

  返回 `code=0` 且带 `accessToken` 即凭据可用（`permissions` 至少含 `*`）。
- 演练中临时注册的 `team1`、`222`、`verif…` 等账号不属于交接凭据，按需清理。

## 二、视觉规范落地

来源：《竞赛平台视觉规范.docx》，已作为前端唯一视觉权威。

| 维度 | 规范要求 | 落地情况 |
|---|---|---|
| 主色 | #295FFF / 深 #1b48e8 / 浅 #e8eeff | 已定义 CSS 变量令牌 |
| 状态色 | 成功 #00b42a、警告 #ff7d00、错误 #f53f3f、禁用 #c9cdd4 | 仅用于状态反馈 |
| 中性色 | 标题 #1d2129、正文 #4e5969、次要 #86909c、边框 #e5e6eb、卡片底 #f7f8fa | 全站统一 |
| 字体 | PingFang SC / Microsoft YaHei / Helvetica Neue / Arial | 已配置 |
| 字重 | 标题 600，正文 400，禁止正文大量加粗 | 已遵守 |
| 底色 | **白底浅色为主，主色仅用于关键操作** | 深色主题已全量改造 |

合规检测结果：**深色背景违规数 0**（原 22 处 → 0），累计替换 155 处硬编码色值。

## 三、功能验收结果

### 后端（8 模块，全部编译通过）

| 模块 | 类数 | 职责 |
|---|---|---|
| csp-common | 11 | 统一返回体、分页、异常、BaseEntity、FlagUtils |
| csp-security | 10 | JWT、LoginUser、UserContext、@PublicApi、@RequirePermission |
| csp-infra | 14 | Redis、Mongo、WebSocket、Docker 靶场调度 |
| csp-ctf | 31 | 报名、答题判分、排行榜、理论赛、反作弊 |
| csp-awd | 28 | 轮次状态机、靶场编排、攻防计分 |
| csp-situation | 5 | 排行榜、战队主页、3D 大屏、靶机监控 |
| csp-platform | 72+ | 用户/RBAC、题库、赛事、审计、通知、归档、SPA 托管 |

接口端点共 117 个。

### 端到端实测（11001）

- 静态资源：index.html 200、JS bundle 200（1.2MB）
- 认证：JWT 登录成功，RBAC 角色 5 个（ADMIN/JUDGE/TEACHER/PLAYER/…）
- 底座：用户 1、题库 2、赛事 2，分页正常
- 赛事状态机：`DRAFT → PENDING → SIGNUP → RUNNING` 合法流转，非法流转被拦截
- 战队：创建 → 审核 APPROVED → 可参赛
- CTF 判分：错误 flag 判 false；正确 flag 判 true 且**首杀 +1 分**（100→101）
- 重复提交：已解出题目返回"本次不重复计分"，总分不变
- 排行榜：实时反映 101 分、第 1 名
- 态势大屏：队伍数、提交数、最高分、领先战队聚合正确
- 前端路由：`/`、`/login`、`/console`、`/situation`、`/rank` 全部 200

## 四、修复的关键缺陷

| 缺陷 | 根因 | 修复 |
|---|---|---|
| 应用无法启动 | platform 与 ctf 存在 3 个同名 Mapper | 统一到 ctf，删除冗余副本 |
| Bean 缺失 | SubmissionStatMapper 不在 @MapperScan 路径 | 移入 mapper 包 |
| Docker 客户端失败 | `withDockerHost(null)` 导致 "uri was not specified" | 仅在显式配置时覆盖 |
| 循环依赖 | AwdRoundService ⇄ AwdScoreService | ScoreService 改用 Mapper 直查 |
| 全表审计列缺失 | BaseEntity 自动填充与表结构不一致 | 补齐 38 列（03_audit_columns.sql） |
| situation 接口 500 | `rank` 是 MySQL 8.0 保留字，未加反引号 | `@TableField("`rank`")` |
| 提交接口 NPE | calcElapsed 返回 null 被拆箱 | 0 兜底 |
| 前端构建失败 | AwdScreen.vue 重复 style 标签 | 合并 style 块 |
| admin 登录失败 | 验收脚本把 admin 口令改成 `Admin@999`，兜底复位依赖 `python3+bcrypt`，缺库静默跳过 | 改密测试改用一次性注册账号；复位改为仓库内固定哈希脚本（见第一节） |
| 战队主页时间差 8 小时 | 容器 JVM 为 UTC，`rs.getTimestamp` 按 JVM 时区换算 | 改 `rs.getObject(col, LocalDateTime.class)` |

## 五、运维脚本

```bash
# 一键热部署（对齐当前 Docker 拓扑：前端 dist bind mount，后端 docker cp + 重启）
# Git Bash 下必须带 MSYS_NO_PATHCONV=1，否则容器挂载参数会被转换成 Windows 路径
MSYS_NO_PATHCONV=1 ./deploy/scripts/hotdeploy.sh fe   # 只重建前端（csp-frontend 直接读 dist，构建即上线）
MSYS_NO_PATHCONV=1 ./deploy/scripts/hotdeploy.sh be   # 只重建后端 jar 并注入 csp-backend
MSYS_NO_PATHCONV=1 ./deploy/scripts/hotdeploy.sh      # 默认 all
# 脚本会把源码复制到纯 ASCII 目录（默认 /tmp/csp-build）再编译：源码路径含中文时 Docker 挂载会失败
# 可覆盖变量：DOCKER（docker 可执行文件路径）、CSP_BUILD_DIR、PROBE、BE_CONTAINER

# 单端口验收服务（11001，本地 jar，不走 Docker）
cd backend && mvn clean package -DskipTests
cd frontend && npx vite build && cp -r dist/* ../backend/csp-platform/src/main/resources/static/
./deploy/scripts/run-11001.sh
```

数据库迁移脚本：`deploy/sql/01_schema.sql`、`02_seed.sql`、`03_audit_columns.sql`
执行方式：`docker exec -i csp-mysql mysql -uroot -pcsp_root_2026 csp < deploy/sql/02_seed.sql`

验收脚本（默认打 Docker 入口 `http://localhost`，11001 模式用 `BASE` / 位置参数覆盖）：

| 脚本 | 用途 |
|---|---|
| `api-full-test.sh` | 全量接口发包，认证/RBAC/赛事/题库/归档 12 阶段；改密阶段用一次性注册账号，不触碰 admin |
| `e2e-test.sh` | 静态资源、鉴权、注册、CTF 全链路、登出 8 阶段 |
| `full-drill.sh` | 建赛到完赛的真实业务演练 |
| `req_acceptance.py` | 逐条对照需求说明验收 |
| `e2e_verify.py` / `e2e_real.py` | 数据驱动的 CTF/理论/场景三套流程与靶机生命周期 |
| `ui-login-click.cjs` / `ui-click-test.cjs` / `ui-portal-test.cjs` | Playwright 真实点击回归 |
| `../sql/fix_admin_password.sql` | admin 口令复位（见第一节） |

Windows Git Bash 下跑 shell 类验收脚本的三个坑（已在上述脚本内修掉，改动这些脚本时请保持）：

1. `python3` 解析到微软商店空壳，执行后无输出且不报错——JSON 断言会静默拿到空值，
   「接口全部失败」或「兜底复位没执行」都是它。脚本统一用 `PY="$(command -v python || command -v python3)"`。
   （历史故障：`api-full-test.sh` 末尾复位 admin 口令的那步就是这么被静默跳过的。）
2. Windows 版 Python 读不到 MSYS 的 `/tmp/xxx`——`open('$TMP/a.json')` 这类写在 python 源码里的路径必然失败。
   `api-full-test.sh` 改为 argv 传路径，`e2e-test.sh` 用 `TW()`（`cygpath -w` 转正斜杠，非 Windows 原样返回）包一层。
3. `curl -d '{…中文…}'` 经 mingw curl 的命令行参数按 GBK 往返，服务端只会回 `400 请求体格式错误`。
   请求体一律落文件再 `--data-binary "@file"`；`full-drill.sh` 原本就用 BODYFILE，天生安全。

> 这类失败都是**环境**问题，不是平台缺陷：同一脚本在 WSL/Linux 下直接可用。


