# AWD 自动注册计分实现文档

## 概述

AWD/AWDP 赛事中，战队创建时自动注册到 Redis 计分排行榜，初始化分数为 0。战队解散时自动清理计分数据。

## 实现原理

### 跨模块依赖处理

由于 `csp-ctf` 和 `csp-awd` 是同级模块，无直接依赖关系，采用共享基础设施层（`csp-infra`）的 `RedisService` 实现计分注册，避免引入循环依赖。

**关键决策**：
- 不添加 `csp-ctf → csp-awd` 的 Maven 依赖（会破坏分层架构）
- 直接复用 `RedisService`（已在 `csp-infra` 中，两个模块都依赖）
- 遵循 `AwdKeys` 的 Redis key 命名规范：`awd:total:{contestId}`

### Redis Key 设计

```
Key:    awd:total:{contestId}
Type:   ZSet
Value:  teamId (Long)
Score:  当前总分 (Double, 初始 0)
```

**示例**：
```
ZADD awd:total:123 0 456789  # 赛事 123，战队 456789，初始 0 分
```

## 代码变更

### 1. RedisService 新增方法

**文件**：`csp-infra/src/main/java/com/csp/infra/redis/RedisService.java`

```java
public Long zRemove(String key, Object... values) { 
    return redisTemplate.opsForZSet().remove(key, values); 
}
```

**用途**：战队解散时从排行榜移除。

### 2. ContestEnrollService 自动注册

**文件**：`csp-ctf/src/main/java/com/csp/ctf/service/ContestEnrollService.java`

**依赖注入**：
```java
private final RedisService redisService;
```

**创建战队时自动注册**（`createTeam()` 方法）：
```java
// AWD/AWDP 赛事自动注册计分（初始化 Redis ZSet 分数为 0）
if ("AWD".equals(contest.getType()) || "AWDP".equals(contest.getType())) {
    String redisKey = "awd:total:" + contest.getId();
    redisService.zAdd(redisKey, team.getId(), 0);
    log.info("AWD 赛事 {} 自动注册战队 {} 计分", contest.getId(), team.getId());
}
```

**退出/解散时清理**（`quit()` 方法）：
```java
if (left == 0) {
    team.setStatus("QUIT");
    // AWD/AWDP 赛事清理计分
    if ("AWD".equals(contest.getType()) || "AWDP".equals(contest.getType())) {
        String redisKey = "awd:total:" + contest.getId();
        redisService.zRemove(redisKey, team.getId());
        log.info("AWD 赛事 {} 清理战队 {} 计分", contest.getId(), team.getId());
    }
}
```

## 数据流

### 战队创建流程

```
1. 用户调用 POST /api/ctf/contests/{contestId}/teams
2. ContestEnrollService.createTeam() 执行：
   - 校验赛事状态（报名中）
   - 校验未重复报名
   - 校验人数未超限
   - 插入 contest_team 表
   - 生成邀请码
   - 插入 contest_team_member 表（队长）
   - **判断赛事类型（AWD/AWDP）**
   - **Redis ZADD awd:total:{contestId} 0 {teamId}**
   - WebSocket 推送 TEAM_CREATED 事件
3. 前端收到推送，刷新战队列表
4. 排行榜服务可立即查询到该战队（0 分）
```

### 战队解散流程

```
1. 队长调用 DELETE /api/ctf/contests/{contestId}/teams/quit
2. ContestEnrollService.quit() 执行：
   - 查询战队成员
   - 如果是队长且还有其他成员，抛出异常（需先转让）
   - 删除 contest_team_member 记录
   - 更新 contest_team.members = 0
   - **判断赛事类型（AWD/AWDP）**
   - **Redis ZREM awd:total:{contestId} {teamId}**
   - 设置 contest_team.status = "QUIT"
3. 排行榜服务自动刷新（战队已移除）
```

## 与 AwdScoreService 的关系

### 原有逻辑（csp-awd）

`AwdScoreService.registerTeam()` 方法：
```java
public void registerTeam(Long contestId, Long teamId) {
    redisService.zAdd(AwdKeys.total(contestId), teamId, 0);
}
```

**调用时机**：由 AWD 模块内部在开赛时批量注册（如 `AwdMatchService.startMatch()`）。

### 新增逻辑（csp-ctf）

`ContestEnrollService.createTeam()` 方法：
```java
if ("AWD".equals(contest.getType()) || "AWDP".equals(contest.getType())) {
    String redisKey = "awd:total:" + contest.getId();
    redisService.zAdd(redisKey, team.getId(), 0);
}
```

**调用时机**：战队创建时立即注册（报名阶段）。

### 幂等性保证

两次调用都使用 `ZADD`，如果 teamId 已存在，Redis 会更新分数为 0（而非累加），因此：
- **报名阶段**：`ContestEnrollService` 注册（0 分）
- **开赛阶段**：`AwdScoreService` 可能再次注册（仍为 0 分，幂等）
- **比赛进行**：`AwdScoreService.zIncrement()` 更新分数

**结论**：两个注册点不冲突，保证战队在创建时即可见排行榜。

## 测试验证

### 单元测试

```java
@Test
void createTeam_AWD_ShouldRegisterToRedis() {
    // 准备 AWD 赛事
    Contest awdContest = new Contest();
    awdContest.setId(100L);
    awdContest.setType("AWD");
    awdContest.setStatus("SIGNUP");
    
    // Mock RedisService
    when(redisService.zAdd("awd:total:100", 200L, 0)).thenReturn(true);
    
    // 执行
    TeamCreateRequest req = new TeamCreateRequest();
    req.setContestId(100L);
    req.setName("TestTeam");
    ContestTeam team = enrollService.createTeam(1L, req);
    
    // 验证
    verify(redisService).zAdd("awd:total:100", team.getId(), 0);
}

@Test
void createTeam_CTF_ShouldNotRegisterToRedis() {
    // 准备 CTF 赛事
    Contest ctfContest = new Contest();
    ctfContest.setId(101L);
    ctfContest.setType("CTF");
    ctfContest.setStatus("SIGNUP");
    
    // 执行
    TeamCreateRequest req = new TeamCreateRequest();
    req.setContestId(101L);
    req.setName("TestTeam");
    enrollService.createTeam(1L, req);
    
    // 验证（不应调用 Redis）
    verify(redisService, never()).zAdd(anyString(), any(), anyDouble());
}
```

### 集成测试

1. **创建 AWD 赛事**
   ```bash
   POST /api/admin/contests
   {
     "name": "AWD 测试赛",
     "type": "AWD",
     "mode": "TEAM",
     "status": "SIGNUP"
   }
   ```

2. **创建战队**
   ```bash
   POST /api/ctf/contests/{contestId}/teams
   {
     "name": "战队A",
     "code": "TA"
   }
   ```

3. **验证 Redis**
   ```bash
   docker exec -it redis redis-cli
   > ZRANGE awd:total:{contestId} 0 -1 WITHSCORES
   1) "{teamId}"
   2) "0"
   ```

4. **查看排行榜**
   ```bash
   GET /api/awd/scoreboard/{contestId}
   # 应包含新创建的战队，分数为 0
   ```

5. **退出战队**
   ```bash
   DELETE /api/ctf/contests/{contestId}/teams/quit
   ```

6. **再次验证 Redis**
   ```bash
   > ZRANGE awd:total:{contestId} 0 -1 WITHSCORES
   (empty array)
   ```

## 注意事项

1. **赛事类型判断**：仅 `AWD` 和 `AWDP` 触发自动注册，`CTF`/`THEORY`/`SCENARIO`/`CFS` 不注册
2. **Redis key 一致性**：必须与 `AwdKeys.total()` 生成的 key 完全一致
3. **事务边界**：Redis 操作在 `@Transactional` 内，如果 DB 回滚，Redis 不会自动回滚（需手动补偿或接受最终一致性）
4. **并发安全**：`ZADD` 是原子操作，多个战队同时创建不会冲突
5. **性能影响**：每次创建战队增加一次 Redis 调用（< 1ms），可忽略

## 未来优化

1. **批量注册**：如果赛事开赛时已有 N 个战队，可批量 `ZADD` 而非逐个注册
2. **Redis 事务**：使用 `MULTI/EXEC` 保证 DB 和 Redis 的原子性
3. **补偿机制**：定时任务扫描 DB 和 Redis 不一致的数据并修复
4. **事件驱动**：引入 Spring ApplicationEvent，`ContestEnrollService` 发布 `TeamCreatedEvent`，`AwdScoreService` 监听并注册（解耦）
