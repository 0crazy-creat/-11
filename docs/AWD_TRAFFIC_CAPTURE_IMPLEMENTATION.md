# AWD 流量取证实现文档

## 概述

AWD 流量取证系统提供网络流量捕获、存储和分析功能，用于：
- **攻击溯源**：记录每轮比赛中各战队靶机的网络流量，支持事后分析攻击行为
- **作弊检测**：通过流量分析识别异常通信模式（如场外辅助、恶意流量）
- **争议仲裁**：为比赛争议提供客观的网络证据

## 核心功能

### 1. 流量捕获

**技术实现**：
- 使用 `tcpdump` 命令行工具捕获网络包
- 每轮比赛为每个战队-靶机对启动独立捕获进程
- 捕获时长与轮次时长一致（默认 300 秒）
- 输出格式为 pcap（标准网络分析格式）

**捕获参数**：
```bash
tcpdump -i any -w output.pcap -c 100000 -G 300 -W 1 "tcp or udp or icmp"
```

- `-i any`：监听所有网卡
- `-w`：写入文件
- `-c 100000`：最多捕获 10 万个包（防止文件过大）
- `-G 300`：捕获 300 秒后自动停止
- `-W 1`：只生成一个文件
- `"tcp or udp or icmp"`：过滤规则（可配置）

### 2. 存储管理

**文件命名**：
```
/tmp/awd-traffic/{contestId}/contest_{contestId}_round_{roundNo}_team_{teamId}_target_{targetId}_{timestamp}.pcap
```

**数据库记录**：
```sql
CREATE TABLE awd_traffic_capture (
    id              BIGINT PRIMARY KEY,
    contest_id      BIGINT,
    round_no        INT,
    team_id         BIGINT,
    target_id       BIGINT,
    capture_file    VARCHAR(512),
    file_size       BIGINT,
    duration        INT,
    packet_count    INT,
    protocol_stats  JSON,
    storage_type    VARCHAR(20),
    status          VARCHAR(20),
    started_at      DATETIME,
    completed_at    DATETIME,
    expires_at      DATETIME
);
```

**过期清理**：
- 默认保留 30 天（可配置）
- 定时任务自动删除过期文件
- 支持手动清理

### 3. 流量分析

**分析类型**：
- `ATTACK_DETECT`：攻击行为检测（SQL注入、XSS、端口扫描等）
- `VULN_SCAN`：漏洞利用扫描
- `ANOMALY_DETECT`：异常流量检测

**分析工具集成**（待实现）：
- **tshark**：Wireshark 命令行版，解析 pcap 文件
- **Zeek**：网络分析框架，生成连接日志
- **自定义规则引擎**：基于正则/机器学习的检测

**分析结果存储**：
```sql
CREATE TABLE awd_traffic_analysis (
    id              BIGINT PRIMARY KEY,
    capture_id      BIGINT,
    analysis_type   VARCHAR(50),
    severity        VARCHAR(20),
    findings        JSON,
    summary         TEXT,
    confidence      DECIMAL(5,2),
    analyzer        VARCHAR(100),
    analyzed_at     DATETIME
);
```

## 代码架构

### 核心类

**TrafficCaptureService**：
- `startCapture()` - 启动流量捕获（异步）
- `stopCapture()` - 停止捕获
- `executeTcpdumpAsync()` - 执行 tcpdump 命令
- `triggerAnalysis()` - 触发流量分析（异步）
- `cleanupExpired()` - 清理过期文件

**AwdTrafficController**：
- `POST /capture/start` - 启动捕获
- `POST /capture/{id}/stop` - 停止捕获
- `GET /captures` - 查询捕获列表
- `GET /capture/{id}` - 查询捕获详情
- `GET /capture/{id}/analysis` - 查询分析结果
- `POST /capture/{id}/analyze` - 手动触发分析
- `DELETE /cleanup` - 清理过期文件

### 数据流

#### 流量捕获流程

```
1. 裁判/管理员启动捕获
   POST /api/awd/contests/{contestId}/traffic/capture/start
   ?roundNo=1&teamId=100&targetId=200

2. TrafficCaptureService.startCapture() 执行：
   - 查询靶机信息（IP地址）
   - 检查是否已有捕获记录
   - 创建 awd_traffic_capture 记录（status=CAPTURING）
   - 生成文件路径
   - 调用 executeTcpdumpAsync()

3. executeTcpdumpAsync() 异步执行：
   - 创建目录
   - 构建 tcpdump 命令
   - 启动 Process
   - 读取输出日志
   - 等待完成（captureDuration + 30 秒）
   - 检查退出码
   - 更新捕获记录（status=COMPLETED）
   - 调用 triggerAnalysis()

4. triggerAnalysis() 异步执行：
   - 查询捕获记录
   - 执行分析（待实现）
   - 创建 awd_traffic_analysis 记录
```

#### 查询分析结果流程

```
1. 用户查询分析结果
   GET /api/awd/contests/{contestId}/traffic/capture/{captureId}/analysis

2. AwdTrafficController.getAnalysis() 执行：
   - 调用 captureService.getAnalysisResults(captureId)
   - SELECT * FROM awd_traffic_analysis WHERE capture_id = ?
   - 返回分析结果列表

3. 前端展示：
   - 分析类型
   - 严重程度
   - 发现结果（JSON展开）
   - 置信度
   - 分析器
   - 分析时间
```

## 配置参数

**application.yml**：
```yaml
awd:
  traffic:
    capture:
      enabled: true          # 流量捕获开关
      duration: 300          # 每轮捕获时长（秒）
      filter: "tcp or udp or icmp"  # tcpdump 过滤规则
    storage:
      type: LOCAL            # 存储类型（LOCAL/MINIO/NAS）
      path: /tmp/awd-traffic # 本地存储路径
      bucket: awd-traffic    # MinIO 桶名（待实现）
    retention:
      days: 30               # 流量文件保留天数
    analysis:
      auto: true             # 自动分析开关
      rules:                 # 启用的分析规则
        - SYN_FLOOD
        - PORT_SCAN
        - SQL_INJECTION
        - XSS_ATTACK
```

## API 接口

### 1. 启动流量捕获

**请求**：
```
POST /api/awd/contests/{contestId}/traffic/capture/start
?roundNo=1&teamId=100&targetId=200
```

**响应**：
```json
{
  "code": 200,
  "data": 12345  // 捕获记录ID
}
```

**权限**：`contest:control`（裁判/管理员）

### 2. 停止流量捕获

**请求**：
```
POST /api/awd/contests/{contestId}/traffic/capture/{captureId}/stop
```

**响应**：
```json
{
  "code": 200,
  "message": "success"
}
```

**权限**：`contest:control`

### 3. 查询捕获列表

**请求**：
```
GET /api/awd/contests/{contestId}/traffic/captures
?roundNo=1&teamId=100
```

**响应**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 12345,
      "contestId": 100,
      "roundNo": 1,
      "teamId": 100,
      "targetId": 200,
      "captureFile": "/tmp/awd-traffic/100/contest_100_round_1_team_100_target_200_1699999999.pcap",
      "fileSize": 1048576,
      "duration": 300,
      "packetCount": 10000,
      "status": "COMPLETED",
      "startedAt": "2026-11-04T10:00:00",
      "completedAt": "2026-11-04T10:05:00"
    }
  ]
}
```

### 4. 查询分析结果

**请求**：
```
GET /api/awd/contests/{contestId}/traffic/capture/{captureId}/analysis
```

**响应**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "captureId": 12345,
      "analysisType": "ATTACK_DETECT",
      "severity": "HIGH",
      "findings": {
        "attacks": [
          {
            "type": "SQL_INJECTION",
            "sourceIp": "192.168.1.100",
            "targetPort": 80,
            "timestamp": "2026-11-04T10:02:30",
            "payload": "' OR 1=1 --"
          }
        ]
      },
      "summary": "检测到 3 次 SQL 注入攻击",
      "confidence": 0.95,
      "analyzer": "RULE_ENGINE",
      "analyzedAt": "2026-11-04T10:06:00"
    }
  ]
}
```

### 5. 清理过期文件

**请求**：
```
DELETE /api/awd/contests/{contestId}/traffic/cleanup
```

**响应**：
```json
{
  "code": 200,
  "data": 15  // 删除的记录数
}
```

**权限**：`system:admin`

## 集成指南

### 1. 与轮次控制集成

**AwdRoundScheduler.startRound()**：
```java
// 轮次开始时自动启动流量捕获
@Autowired
private TrafficCaptureService trafficCaptureService;

public void startRound(Long contestId, Integer roundNo) {
    // 1. 更新轮次状态
    round.setStatus("RUNNING");
    roundMapper.updateById(round);
    
    // 2. 启动所有战队的流量捕获
    List<AwdTarget> targets = targetMapper.selectList(
        new LambdaQueryWrapper<AwdTarget>()
            .eq(AwdTarget::getContestId, contestId));
    
    for (AwdTarget target : targets) {
        // 查询该靶机对应的战队
        Long teamId = resolveTeamId(target);
        trafficCaptureService.startCapture(
            contestId, roundNo, teamId, target.getId());
    }
    
    // 3. 启动容器
    containerManager.startTargets(contestId, roundNo);
}
```

**AwdRoundScheduler.endRound()**：
```java
public void endRound(Long contestId, Integer roundNo) {
    // 1. 停止所有流量捕获
    List<AwdTrafficCapture> captures = trafficCaptureService.listCaptures(
        contestId, roundNo, null);
    for (AwdTrafficCapture capture : captures) {
        trafficCaptureService.stopCapture(capture.getId());
    }
    
    // 2. 停止容器
    containerManager.stopTargets(contestId, roundNo);
    
    // 3. 更新轮次状态
    round.setStatus("FINISHED");
    roundMapper.updateById(round);
}
```

### 2. 前端集成

**Vue 组件示例**：
```vue
<template>
  <div class="traffic-capture-panel">
    <el-button @click="startCapture" type="primary">
      启动流量捕获
    </el-button>
    
    <el-table :data="captures">
      <el-table-column prop="roundNo" label="轮次" />
      <el-table-column prop="teamId" label="战队" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" label="文件大小">
        <template #default="{ row }">
          {{ formatSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click="viewAnalysis(row.id)">
            查看分析
          </el-button>
          <el-button @click="downloadPcap(row.id)">
            下载 pcap
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { startCapture, listCaptures, getAnalysis } from '@/api/awd';

const captures = ref([]);

onMounted(async () => {
  const res = await listCaptures(contestId, roundNo, teamId);
  captures.value = res.data;
});

async function startCapture() {
  await startCapture(contestId, roundNo, teamId, targetId);
  ElMessage.success('流量捕获已启动');
}

async function viewAnalysis(captureId) {
  const res = await getAnalysis(contestId, captureId);
  // 展示分析结果
}

function getStatusType(status) {
  const map = {
    CAPTURING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger'
  };
  return map[status] || 'info';
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
  return (bytes / 1024 / 1024).toFixed(2) + ' MB';
}
</script>
```

## 测试验证

### 1. 单元测试

```java
@Test
void startCapture_ShouldCreateRecord() {
    // 准备
    Long contestId = 100L;
    Integer roundNo = 1;
    Long teamId = 200L;
    Long targetId = 300L;
    
    // 执行
    Long captureId = trafficCaptureService.startCapture(
        contestId, roundNo, teamId, targetId);
    
    // 验证
    assertThat(captureId).isNotNull();
    AwdTrafficCapture capture = captureMapper.selectById(captureId);
    assertThat(capture.getStatus()).isEqualTo("CAPTURING");
    assertThat(capture.getContestId()).isEqualTo(contestId);
    assertThat(capture.getRoundNo()).isEqualTo(roundNo);
}

@Test
void stopCapture_ShouldUpdateStatus() {
    // 准备：启动捕获
    Long captureId = trafficCaptureService.startCapture(...);
    Thread.sleep(5000);  // 等待 5 秒
    
    // 执行：停止捕获
    trafficCaptureService.stopCapture(captureId);
    
    // 验证
    AwdTrafficCapture capture = captureMapper.selectById(captureId);
    assertThat(capture.getStatus()).isEqualTo("COMPLETED");
    assertThat(capture.getFileSize()).isGreaterThan(0);
}
```

### 2. 集成测试

```bash
# 1. 创建 AWD 赛事
POST /api/admin/contests
{
  "name": "AWD 流量测试",
  "type": "AWD",
  "status": "RUNNING"
}

# 2. 创建靶机
POST /api/awd/targets
{
  "contestId": 100,
  "targetIp": "192.168.1.100",
  "targetPort": 80
}

# 3. 启动轮次
POST /api/awd/contests/100/rounds/start
{
  "roundNo": 1
}

# 4. 启动流量捕获
POST /api/awd/contests/100/traffic/capture/start
?roundNo=1&teamId=200&targetId=300

# 5. 等待 10 秒
sleep 10

# 6. 停止捕获
POST /api/awd/contests/100/traffic/capture/12345/stop

# 7. 查询捕获记录
GET /api/awd/contests/100/traffic/capture/12345
# 应返回 status=COMPLETED, fileSize>0

# 8. 查询分析结果
GET /api/awd/contests/100/traffic/capture/12345/analysis
# 应返回分析记录（当前为占位结果）

# 9. 检查文件
ls -lh /tmp/awd-traffic/100/
# 应看到 pcap 文件
```

## 性能优化

### 1. 异步处理

- 流量捕获使用 `@Async` 异步执行，不阻塞主线程
- 分析任务异步触发，捕获完成后自动启动

### 2. 进程管理

- 使用 `ConcurrentHashMap` 管理捕获进程
- 支持强制终止超时进程
- 避免僵尸进程

### 3. 文件清理

- 定时任务清理过期文件
- 使用 `expires_at` 索引加速查询
- 批量删除减少数据库压力

### 4. 存储优化

- 按赛事ID分目录存储
- 文件名包含时间戳避免冲突
- 支持 MinIO 对象存储（待实现）

## 注意事项

1. **权限控制**：
   - 启动/停止捕获需要 `contest:control` 权限
   - 清理文件需要 `system:admin` 权限
   - 普通用户只能查询自己战队的捕获记录

2. **资源限制**：
   - 每个捕获进程最多 10 万个包
   - 捕获时长默认 300 秒（可配置）
   - 文件大小限制（建议 < 100MB）

3. **网络接口**：
   - tcpdump 需要 root 权限或 CAP_NET_RAW 能力
   - 生产环境建议使用专用网卡或 VLAN

4. **隐私保护**：
   - 流量文件包含敏感数据，需严格访问控制
   - 过期文件自动删除
   - 审计日志记录所有访问

## 未来优化

1. **MinIO 集成**：
   - 实现 MinioService
   - 上传 pcap 文件到对象存储
   - 生成预签名下载链接

2. **深度分析**：
   - 集成 tshark 解析 pcap
   - 集成 Zeek 生成连接日志
   - 实现自定义规则引擎

3. **实时监控**：
   - WebSocket 推送捕获状态
   - 实时流量统计（包/秒、字节/秒）
   - 异常流量告警

4. **可视化增强**：
   - 流量时序图
   - 协议分布饼图
   - 攻击热力图

5. **分布式支持**：
   - 多台捕获节点协同
   - 流量文件分片存储
   - 跨地域流量分析
