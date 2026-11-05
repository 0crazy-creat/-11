# 前端补全清单（面向后端接口）

> 基线：后端 259 个接口 / 前端 API 层已定义约 130 个调用 / `views` 层实际使用约 90 个。
> 结论：**API 层基本齐备，缺口集中在「页面层」与 4 个后端模块的 API 绑定**。

## 一、分类总览

| 类别 | 说明 | 数量 |
|---|---|---|
| A. API 已就绪，仅缺页面 | `api/index.js` 已有完整方法，`views` 零引用 | 3 项 |
| B. API 与页面都缺 | 后端有接口，前端连 API 都没写 | 4 项 |
| C. 有骨架但未打通 | 页面/路由存在，未接真实接口或后端缺失 | 3 项 |

---

## 二、A 类：API 已就绪，只差页面（优先，成本最低）

### A-1 消息通知中心
- **后端**：7 个接口（`/notify/my`、`/notify/unread-count`、`/notify/inbox`、`/notify/{id}`、`/notify/read`、`/notify/read-all`、`/notify/send`）
- **前端现状**：`notifyApi` 已在 `api/index.js:199-207` 完整定义，**零页面引用**
- **待做**：
  1. `layouts/PortalLayout.vue` + `ConsoleLayout.vue` 顶栏加铃铛 + 未读数角标（30s 轮询 `unreadCount`）
  2. 抽屉/下拉面板展示最近通知，点击标记已读
  3. 新增 `/portal/notifications` 页：分页列表 + 全部已读 + 详情
  4. 管理端 `/console/notifications`：发送通知（`notify/send`）
- **工作量**：小

### A-2 赛事归档管理
- **后端**：5 个接口（`/archives/contest/{id}` 创建、`/archives` 分页、`/archives/{id}` 详情、`/archives/{id}/verify` 校验、`/archives/contest/{id}` 按赛事查）
- **前端现状**：`archiveApi` 已定义（`api/index.js:226-232`），零页面
- **待做**：`/console/archives` 页 —— 列表 + 创建归档 + 完整性校验（显示校验结果）
- **工作量**：小

### A-3 统计报表
- **后端**：`/stats/overview`、`/statistics/theory/report`、`/statistics/class/report`、`/sla`
- **前端现状**：`statsApi`、`opsApi` 已定义，仅 Dashboard 用了 `overview`，其余零引用
- **待做**：`/console/reports` 页 —— 理论赛报告 + 班级报告 + SLA 三块，ECharts 出图
- **工作量**：中

---

## 三、B 类：API 与页面都缺

### B-1 LVP 虚拟机管理（8 个接口）
- 后端：`GET/POST /lvp/vms`、`GET /lvp/vms/{id}`、`GET /lvp/vms/summary`、`POST /lvp/vms/{id}/{start|stop|snapshot}`、`DELETE /lvp/vms/{id}`
- 前端：`lvp` 关键字 **零匹配**
- 待做：先在 `api/` 补 `lvpApi`，再写 `/console/lvp` 管理页（列表 + 启停 + 快照）
- 工作量：中

### B-2 集群监控（4 个接口）
- 后端：`GET /cluster/overview`、`GET /cluster/sla`、`POST /cluster/heartbeat`、`POST /cluster/sample`
- 前端：`cluster` **零匹配**
- 待做：补 `clusterApi` + `/console/cluster` 页（节点概览 + SLA 曲线）
- 工作量：中

### B-3 MQTT 管理（3 个接口）
- 后端：`GET /mqtt/status`、`GET /mqtt/messages`、`POST /mqtt/publish`
- 前端：`mqtt` **零匹配**
- 待做：补 `mqttApi` + 管理页（连接状态 + 消息列表 + 手动发布）
- 工作量：小

### B-4 SSO / LDAP（4 个接口）
- 后端：`GET /sso/ldap/status`、`POST /sso/ldap/login`、`POST /sso/ticket/issue`、`POST /sso/ticket/validate`
- 前端：`ldap`、`ticket` **零匹配**
- 待做：登录页加「LDAP 登录」tab + 管理端 SSO 配置页
- 工作量：中

---

## 四、C 类：有骨架但未打通（阻塞业务）

### C-1 理论赛答题（P0）
- **问题**：`router/index.js:24` 的 `/portal/theory/:contestId` 直接复用 `PlayArena.vue`（CTF 答题页），从未调用 `theoryApi.paper` / `theoryApi.submit`
- **后果**：六大正式赛制之一无法正常交卷
- **待做**：新建 `views/portal/TheoryArena.vue`（试卷渲染 + 倒计时 + 交卷确认），切换路由指向
- **工作量**：中

### C-2 CFS 拓扑管理（P0）
- **问题**：`cfsApi` 已有 `createTopology/topologyDef/publish/start`，但**没有任何管理页面**；`cfs_topology` 表为空
- **后果**：CFS 赛制永远显示「未配置分层拓扑」
- **待做**：`/console/cfs-topology` 页（拓扑 CRUD + 分层/节点配置 + 发布）
- **工作量**：中

### C-3 AWDP 补丁提交（P1）
- **问题**：`api/index.js:344` 调用 `POST /awd/defense/submit`，但**后端该端点不存在**（实测 500）
- **后果**：AWDP 核心的「提交补丁包」功能失效
- **待做**：后端补 `submit` 端点（或前端改用 `POST /awd/defense`），并打通 Check 验证
- **工作量**：中（需后端配合）

---

## 五、执行顺序建议

1. **A-1 消息通知中心** ← 立即开始，全局基础能力，见效最快
2. **A-2 赛事归档**
3. **C-1 理论赛答题**（解锁第六大赛制）
4. **B-3 MQTT** / **A-3 统计报表**
5. **C-2 CFS 拓扑管理**
6. **B-1 LVP** / **B-2 集群** / **B-4 SSO**
7. **C-3 AWDP**（等后端接口）