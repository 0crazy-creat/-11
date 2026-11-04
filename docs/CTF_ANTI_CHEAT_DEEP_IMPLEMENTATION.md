# CTF 反作弊深化实现文档

## 概述

在现有反作弊机制（限流、暴力猜解检测、机器速度检测、同IP多账号检测）基础上，新增**设备指纹追踪**和**作弊审计日志**功能，实现更深账号检测、设备切换检测、风险评估与可视化。

## 核心功能

### 1. 设备指纹采集与存储

**前端采集**：
- 浏览器指纹（Canvas、WebGL、AudioContext）
- 屏幕分辨率与色深
- 系统字体列表
- 时区与语言
- 硬件并发数与内存大小
- 生成唯一 deviceId（基于上述特征的哈希）

**后端存储**：
```sql
CREATE TABLE device_fingerprint (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id       VARCHAR(128) NOT NULL,
    user_id         BIGINT NOT NULL,
    contest_id      BIGINT NOT NULL,
    fingerprint     JSON NOT NULL,
    ip_address      VARCHAR(64),
    user_agent      VARCHAR(512),
    first_seen_at   DATETIME,
    last_seen_at    DATETIME,
    trust_score     INT DEFAULT 100,
    risk_flags      VARCHAR(255)
);
```

### 2. 多账号检测

**检测逻辑**：
- 同一设备（deviceId 相同）关联多个用户
- 阈值：`DEVICE_MULTI_USERS_THRESHOLD = 3`
- 触发后：
  - 记录高风险审计日志（severity=HIGH, riskScore=80）
  - 降低该设备所有关联用户的信任分（-20）
  - 标记 risk_flags 为 `MULTI_ACCOUNT`

**代码实现**：
```java
// AntiCheatAnalysisService.detectMultiAccountRisk()
Integer userCount = fingerprintMapper.countUsersByDevice(deviceId, contestId);
if (userCount >= DEVICE_MULTI_USERS_THRESHOLD) {
    logAudit(contestId, userId, null, "MULTI_DEVICE", "HIGH", 80, ...);
    // 降低信任分
    for (DeviceFingerprint fp : relatedFingerprints) {
        fp.setTrustScore(Math.max(0, fp.getTrustScore() - 20));
        fp.setRiskFlags(appendRiskFlag(fp.getRiskFlags(), "MULTI_ACCOUNT"));
    }
}
```

### 3. 作弊审计日志

**记录内容**：
```sql
CREATE TABLE cheat_audit_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    contest_id      BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    audit_type      VARCHAR(50) NOT NULL,
    severity        VARCHAR(20) DEFAULT 'LOW',
    risk_score      INT DEFAULT 0,
    detail          JSON,
    action_taken    VARCHAR(50)
);
```

**审计类型**：
- `RATE_LIMIT` - 触发限流
- `BRUTE_FORCE` - 暴力猜解 flag
- `MACHINE_SPEED` - 机器速度（< 800ms）
- `MULTI_IP` - 同IP多账号
- `MULTI_DEVICE` - 同设备多账号
- `SAME_ANSWER` - 答案高度相似

**严重程度**：
- `LOW` - 风险提示（riskScore < 40）
- `MEDIUM` - 可疑行为（40 ≤ riskScore < 60）
- `HIGH` - 高风险（60 ≤ riskScore < 80）
- `CRITICAL` - 确认作弊（riskScore ≥ 80）

### 4. 风险评估 API

**查询用户风险摘要**：
```
GET /api/ctf/contests/{contestId}/anti-cheat/users/{userId}/risk
```

**返回示例**：
```json
{
  "deviceCount": 2,
  "devices": ["device_abc123", "device_def456"],
  "auditLogs": {
    "MULTI_DEVICE": 3,
    "RATE_LIMIT": 1
  },
  "maxRiskScore": 80,
  "totalAuditCount": 4,
  "fingerprints": [
    {
      "deviceId": "device_abc123",
      "trustScore": 60,
      "riskFlags": "MULTI_ACCOUNT",
      "lastSeenAt": "2026-11-04T10:30:00"
    }
  ]
}
```

**查询赛事高风险用户**：
```
GET /api/ctf/contests/{contestId}/anti-cheat/high-risk?minRiskScore=60
```

**返回示例**：
```json
[
  {
    "userId": 123,
    "auditType": "MULTI_DEVICE",
    "severity": "HIGH",
    "riskScore": 80,
    "detail": {"deviceId": "xxx", "userCount": 3},
    "actionTaken": "WARNING",
    "createdAt": "2026-11-04T10:30:00"
  }
]
```

## 集成点

### 1. 提交答案时记录指纹

**文件**：`SubmissionService.submit()`

```java
// 记录设备指纹（用于多账号检测）
String userAgent = httpRequest.getHeader("User-Agent");
antiCheatAnalysis.recordFingerprint(
    contest.getId(), userId, req.getDeviceId(), 
    null, ip, userAgent
);
```

**时机**：每次提交 flag/答案时自动记录，无需前端额外操作。

### 2. 现有反作弊机制保留

- `AntiCheatService.preCheck()` - 限流 + 暴力猜解检测
- `AntiCheatService.postCheckOnCorrect()` - 机器速度 + 同IP多账号检测
- `AntiCheatService.tryAcquireSubmitLock()` - 提交去重锁

**新增**：`AntiCheatAnalysisService` 专注于设备指纹和审计日志。

## 数据流

### 设备指纹记录流程

```
1. 用户提交答案
   POST /api/ctf/submissions
   {
     "contestId": 100,
     "questionId": 200,
     "content": "flag{...}",
     "deviceId": "abc123..."  // 前端生成
   }

2. SubmissionService.submit() 执行：
   - 解析 deviceId 和 User-Agent
   - 调用 antiCheatAnalysis.recordFingerprint()
   
3. AntiCheatAnalysisService.recordFingerprint() 执行：
   - 查询 device_fingerprint 表（是否已存在）
   - 不存在：INSERT 新记录（trustScore=100）
   - 已存在：UPDATE last_seen_at 和 ip_address
   - 调用 detectMultiAccountRisk()
   
4. detectMultiAccountRisk() 执行：
   - 统计该设备关联的用户数
   - 如果 >= 3：
     - INSERT cheat_audit_log（MULTI_DEVICE, HIGH, 80）
     - UPDATE 所有关联指纹的 trustScore（-20）
     - UPDATE risk_flags = "MULTI_ACCOUNT"
     
5. 继续正常判题流程
```

### 风险查询流程

```
1. 管理员/裁判查看赛事反作弊面板
   GET /api/ctf/contests/100/anti-cheat/high-risk?minRiskScore=60

2. CtfAntiCheatController.getHighRiskUsers() 执行：
   - 权限校验（contest:control）
   - 调用 analysisService.getHighRiskUsers(100, 60)
   
3. AntiCheatAnalysisService.getHighRiskUsers() 执行：
   - SELECT * FROM cheat_audit_log
     WHERE contest_id=100 AND risk_score>=60
     ORDER BY risk_score DESC, created_at DESC
   - 返回审计日志列表
   
4. 前端展示高风险用户列表
   - 用户ID
   - 检测类型
   - 风险分
   - 详细信息（JSON展开）
   - 采取措施
```

## 前端集成指南

### 1. 设备指纹采集（FingerprintJS）

**推荐库**：[FingerprintJS](https://github.com/fingerprintjs/fingerprintjs)

```javascript
// src/utils/fingerprint.js
import FingerprintJS from '@fingerprintjs/fingerprintjs';

let fpPromise = null;

export async function getDeviceId() {
  if (!fpPromise) {
    fpPromise = FingerprintJS.load();
  }
  const fp = await fpPromise;
  const result = await fp.get();
  return result.visitorId;  // 32位哈希
}

export async function getFingerprint() {
  if (!fpPromise) {
    fpPromise = FingerprintJS.load();
  }
  const fp = await fpPromise;
  const result = await fp.get();
  return {
    visitorId: result.visitorId,
    components: result.components  // 详细特征
  };
}
```

### 2. 提交答案时携带 deviceId

```javascript
// src/api/ctf.js
import { getDeviceId } from '@/utils/fingerprint';

export async function submitAnswer(contestId, questionId, content, startTs) {
  const deviceId = await getDeviceId();
  return request.post('/api/ctf/submissions', {
    contestId,
    questionId,
    content,
    startTs,
    deviceId  // 新增字段
  });
}
```

### 3. 反作弊面板（管理员）

```vue
<!-- src/views/contest/anti-cheat.vue -->
<template>
  <div class="anti-cheat-panel">
    <el-table :data="highRiskUsers">
      <el-table-column prop="userId" label="用户ID" />
      <el-table-column prop="auditType" label="检测类型" />
      <el-table-column prop="severity" label="严重程度">
        <template #default="{ row }">
          <el-tag :type="getSeverityType(row.severity)">
            {{ row.severity }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="riskScore" label="风险分" />
      <el-table-column label="详情">
        <template #default="{ row }">
          <pre>{{ JSON.stringify(row.detail, null, 2) }}</pre>
        </template>
      </el-table-column>
      <el-table-column prop="actionTaken" label="已采取措施" />
      <el-table-column prop="createdAt" label="时间" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getHighRiskUsers } from '@/api/ctf';

const highRiskUsers = ref([]);

onMounted(async () => {
  const res = await getHighRiskUsers(contestId, 60);
  highRiskUsers.value = res.data;
});

function getSeverityType(severity) {
  const map = { LOW: 'info', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger' };
  return map[severity] || 'info';
}
</script>
```

## 测试验证

### 1. 单元测试

```java
@Test
void recordFingerprint_MultiAccounts_ShouldLogHighRisk() {
    // 准备：同一设备关联3个用户
    String deviceId = "test_device_123";
    Long contestId = 100L;
    
    // 用户1
    analysisService.recordFingerprint(contestId, 1L, deviceId, null, "1.1.1.1", "UA1");
    // 用户2
    analysisService.recordFingerprint(contestId, 2L, deviceId, null, "1.1.1.1", "UA1");
    // 用户3（触发检测）
    analysisService.recordFingerprint(contestId, 3L, deviceId, null, "1.1.1.1", "UA1");
    
    // 验证：审计日志已记录
    List<CheatAuditLog> logs = auditLogMapper.selectList(
        new LambdaQueryWrapper<CheatAuditLog>()
            .eq(CheatAuditLog::getContestId, contestId)
            .eq(CheatAuditLog::getAuditType, "MULTI_DEVICE")
    );
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getRiskScore()).isEqualTo(80);
    assertThat(logs.get(0).getSeverity()).isEqualTo("HIGH");
    
    // 验证：信任分已降低
    DeviceFingerprint fp = fingerprintMapper.selectOne(
        new LambdaQueryWrapper<DeviceFingerprint>()
            .eq(DeviceFingerprint::getDeviceId, deviceId)
            .eq(DeviceFingerprint::getUserId, 1L)
    );
    assertThat(fp.getTrustScore()).isLessThan(100);
    assertThat(fp.getRiskFlags()).contains("MULTI_ACCOUNT");
}
```

### 2. 集成测试

```bash
# 1. 创建赛事
POST /api/admin/contests
{
  "name": "反作弊测试赛",
  "type": "CTF",
  "status": "RUNNING"
}

# 2. 用户A提交答案（携带 deviceId）
POST /api/ctf/submissions
{
  "contestId": 100,
  "questionId": 200,
  "content": "flag{test}",
  "deviceId": "device_abc"
}

# 3. 用户B使用相同设备提交
POST /api/ctf/submissions
{
  "contestId": 100,
  "questionId": 201,
  "content": "flag{test2}",
  "deviceId": "device_abc"
}

# 4. 用户C使用相同设备提交（触发多账号检测）
POST /api/ctf/submissions
{
  "contestId": 100,
  "questionId": 202,
  "content": "flag{test3}",
  "deviceId": "device_abc"
}

# 5. 查询高风险用户
GET /api/ctf/contests/100/anti-cheat/high-risk?minRiskScore=60
# 应返回 deviceId=device_abc 的审计日志

# 6. 查询用户风险摘要
GET /api/ctf/contests/100/anti-cheat/users/1/risk
# 应返回 deviceCount=1, maxRiskScore=80
```

## 性能优化

### 1. 索引设计

```sql
-- device_fingerprint 表
UNIQUE KEY uk_df_device_user (device_id, user_id),
KEY idx_df_contest (contest_id),
KEY idx_df_user (user_id),
KEY idx_df_trust (trust_score)

-- cheat_audit_log 表
KEY idx_cal_contest (contest_id),
KEY idx_cal_user (user_id),
KEY idx_cal_type (audit_type),
KEY idx_cal_severity (severity),
KEY idx_cal_created (created_at)
```

### 2. 查询优化

- **设备指纹查询**：使用 `device_id + contest_id` 联合索引，O(log N)
- **多账号统计**：`COUNT(DISTINCT user_id)` 使用覆盖索引
- **审计日志查询**：按 `created_at DESC` 排序，使用索引避免 filesort

### 3. 缓存策略

```java
// 设备指纹缓存（Redis）
String cacheKey = "df:" + contestId + ":" + deviceId;
List<Long> userIds = redisService.get(cacheKey);
if (userIds == null) {
    userIds = fingerprintMapper.selectUsersByDeviceAndContest(deviceId, contestId);
    redisService.set(cacheKey, userIds, Duration.ofMinutes(10));
}
```

## 注意事项

1. **隐私保护**：
   - 设备指纹仅存储哈希值，不存储原始特征
   - 审计日志保留 90 天后自动归档
   - 仅管理员/裁判可查询风险数据

2. **误判处理**：
   - 公共设备（如学校机房）可能多人共用，trustScore 降低不直接判负
   - 人工审核：管理员可查看详细信息并手动标记（SUSPENDED/CLEARED）

3. **扩展性**：
   - 审计类型（audit_type）可扩展（如 SAME_ANSWER、VPN_DETECT）
   - 风险分计算规则可配置（JSON 格式存储在 cheat_audit_log）

4. **前端兼容**：
   - deviceId 为可选字段，前端未采集时后端跳过指纹记录
   - 使用 FingerprintJS 开源版（免费）或 Pro 版（更精准）

## 未来优化

1. **机器学习检测**：
   - 基于历史数据训练异常检测模型
   - 识别复杂作弊模式（如协作答题、时间序列异常）

2. **实时告警**：
   - WebSocket 推送高风险事件给裁判
   - 自动触发人工审核流程

3. **跨赛事追踪**：
   - 全局设备指纹库（跨所有赛事）
   - 识别职业作弊者

4. **可视化增强**：
   - 设备关系图谱（Graph Visualization）
   - 风险时间线（Timeline）
   - 热力图（Heatmap）
