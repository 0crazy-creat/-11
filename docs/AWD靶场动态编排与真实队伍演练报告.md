# AWD 靶场动态编排与真实队伍演练报告

## 一、问题定位

用户反馈三个问题：

| 问题 | 根因 |
|---|---|
| 看不到靶场地址 | ① `TargetVO` 缺 `accessUrl` 字段；② 选手端无"我的靶场"页面 |
| 没有测试队伍 | 演示赛事只有 1 支 admin 队伍，无多队对抗 |
| 靶场非动态生成 | `defaultImage()` 硬编码 `csp/awd-target:latest`（镜像不存在）→ 拉起必失败；`provision-all` 只做"自愈"不创建新靶机 |

## 二、修复内容

### 1. 镜像来源改为题库配置
`AwdTargetService.defaultImage()`：优先读取 `question.image_name`，回退 `csp/arena-basic-web:1.0`。

### 2. 靶机访问地址
`TargetVO` 新增 `accessUrl`，由 `publicHost + port` 拼装（仅 RUNNING 状态返回）。

### 3. 战队归属解析
`AwdTeamResolver` 重写：直接 `contest_team_member JOIN contest_team` 按 `contest_id` 查，不再依赖 `LoginUser.orgCode` 注入。

### 4. 选手端"我的靶场"
新增 `frontend/src/views/portal/MyTargets.vue` + 路由 `/portal/targets` + 侧边菜单，展示状态/地址/IP/端口，一键访问。

### 5. AWD 题库
新增 4 道 AWD 靶场题（`bank_type='AWD'`），关联 `image_name`。

## 三、真实队伍演练

### 队伍与账号

| 队伍 | 账号 | 密码 | 靶机地址 | 独立 flag |
|---|---|---|---|---|
| AWD战队1 | `awdteam1` | `Team@123` | http://127.0.0.1:20000 | `flag{awd_<team1>_1_...}` |
| AWD战队2 | `awdteam2` | `Team@123` | http://127.0.0.1:20001 | `flag{awd_<team2>_1_...}` |
| AWD战队3 | `awdteam3` | `Team@123` | http://127.0.0.1:20002 | `flag{awd_<team3>_1_...}` |

### 动态编排结果

```
POST /api/awd/targets/provision?contestId=...&teamId=...&questionId=...&roundNo=1
→ team1: 192.168.128.2:20000  RUNNING  health=1
→ team2: 192.168.128.3:20001  RUNNING  health=1
→ team3: 192.168.128.4:20002  RUNNING  health=1
```

每队容器独立、端口独立、**flag 独立**（含 teamId + roundNo + 随机串）。

### 攻防对抗实测

| 攻击 | 结果 |
|---|---|
| 队1 打队2 | ✅ 首杀 +150，受害队 -30 |
| 队2 打队3 | ✅ 首杀 +150 |
| 队3 打队1 | ✅ 首杀 +150 |
| 重复提交同一 flag | ✅ 拒绝重复计分（+0） |

排行榜：三队各 150 分，排名正确。

### 选手端浏览器验证

| 检查 | 结果 |
|---|---|
| `awdteam1` 看到自己的靶场 | ✅ 卡片=1 地址=http://127.0.0.1:20000 |
| `awdteam2` 看到自己的靶场 | ✅ 卡片=1 地址=http://127.0.0.1:20001 |
| `awdteam3` 看到自己的靶场 | ✅ 卡片=1 地址=http://127.0.0.1:20002 |
| 公开排行榜页 | ✅ 可打开 |
| 三靶场直连 | ✅ HTTP=200，flag.txt 各队不同 |

## 四、多赛制并发演练

三支队伍同时报名 CTF / 理论 / 场景三场赛事并真实答题：

| 队伍 | 赛制 | 提交结果 |
|---|---|---|
| 队1 | CTF | ✅ 首杀 +1，总分 **101** |
| 队2 | CTF | ✅ +100，总分 **100** |
| 队3 | CTF | ✅ +100，总分 **100** |

CTF 排行榜：`rank1 战队1号CTF 101` → `rank2 战队3号CTF 100` → `rank3 战队2号CTF 100`。

## 五、回归结果

| 套件 | 结果 |
|---|---|
| 业务全流程 `e2e_verify.py` | **33 / 33** |
| 平台 API `api-full-test.sh` | **78 / 78** |
| 独立靶场 `arena-verify.sh` | **17 / 17** |

## 六、数据看板

- 态势总览：`teamCount=3, targetCount=3, healthyCount=3`
- 靶机监控：3 台 RUNNING，带 `accessUrl`，health=1
- 飞线：真实攻破记录（team→target）
- 排行榜：AWD 攻击分 / CTF 总分均正常展示

## 七、复现命令

```bash
# 1. 建 AWD 赛并下发靶场题
docker exec -i csp-mysql mysql -uroot -pcsp_root_2026 < deploy/sql/11_awd_questions.sql

# 2. 三队报名（脚本见 /tmp 或按上文 curl 复现）

# 3. 逐队动态拉靶机
curl -X POST "$B/api/awd/targets/provision?contestId=$CID&teamId=$TID&questionId=$QID&roundNo=1" -H "Authorization: Bearer $AT"

# 4. 选手端查看
open http://127.0.0.1:11001/portal/targets
```
