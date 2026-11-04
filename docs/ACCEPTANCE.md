# 网络安全竞赛平台 · 验收说明

## 一、访问入口

| 项目 | 地址 |
|---|---|
| 平台首页（前端 SPA） | http://localhost:11001/ |
| 登录页 | http://localhost:11001/login |
| 控制台 | http://localhost:11001/console |
| 3D 态势大屏 | http://localhost:11001/situation |
| 排行榜 | http://localhost:11001/rank |
| 后端 API | http://localhost:11001/api |
| 接口文档 | http://localhost:11001/swagger-ui/index.html |

**默认账号**：`admin` / `Admin@123`

单端口部署：前端构建产物打包进后端 jar 的 `static/`，由 `SpaWebMvcConfig` 做 SPA 路由回退，
`/api/**` 与 `/ws` 不做回退，保证接口 404 语义不被吞。

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

## 五、运维脚本

```bash
# 构建后端（含前端产物）
cd backend && mvn clean package -DskipTests

# 构建前端
cd frontend && npx vite build && cp -r dist/* ../backend/csp-platform/src/main/resources/static/

# 启动验收服务（11001）
./deploy/scripts/run-11001.sh
```

数据库迁移脚本：`deploy/sql/01_schema.sql`、`02_seed.sql`、`03_audit_columns.sql`
