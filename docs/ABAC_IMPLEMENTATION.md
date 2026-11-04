# ABAC 属性决策引擎实施文档

**实施时间**: 2026-11-04  
**状态**: ✅ 代码完成，待编译验证

---

## 1. 数据库设计

### 1.1 新增表

**`abac_policy` - ABAC策略表**
```sql
CREATE TABLE abac_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_name VARCHAR(100) NOT NULL COMMENT '策略名称',
    description VARCHAR(500) COMMENT '策略描述',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型: contest/question/team/target',
    action VARCHAR(20) NOT NULL COMMENT '操作: view/edit/delete/submit',
    condition_expr TEXT NOT NULL COMMENT '条件表达式(SpEL)',
    effect VARCHAR(10) NOT NULL DEFAULT 'DENY' COMMENT '效果: ALLOW/DENY',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级(数字越大越优先)',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);
```

**`abac_audit_log` - ABAC审计日志表**
```sql
CREATE TABLE abac_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    policy_id BIGINT COMMENT '匹配的策略ID',
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100) NOT NULL,
    action VARCHAR(20) NOT NULL,
    decision VARCHAR(10) NOT NULL COMMENT 'ALLOW/DENY',
    reason VARCHAR(500),
    ip_address VARCHAR(64),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 1.2 预置策略示例

```sql
INSERT INTO abac_policy (policy_name, resource_type, action, condition_expr, effect, priority) VALUES
('院校隔离-查看赛事', 'contest', 'view', 'user.orgCode == resource.orgCode', 'ALLOW', 10),
('管理员豁免-查看赛事', 'contest', 'view', 'user.roles.contains("ADMIN")', 'ALLOW', 100),
('裁判豁免-查看赛事', 'contest', 'view', 'user.roles.contains("JUDGE")', 'ALLOW', 90),
('队长管理-编辑战队', 'team', 'edit', 'resource.captainId == user.id', 'ALLOW', 50);
```

---

## 2. 后端实现

### 2.1 实体类

**`AbacPolicy.java`** - ABAC策略实体
- 位置: `csp-platform/src/main/java/com/csp/platform/entity/AbacPolicy.java`
- 继承: `BaseEntity`
- 字段: policyName, description, resourceType, action, conditionExpr, effect, priority, enabled

**`AbacAuditLog.java`** - 审计日志实体
- 位置: `csp-platform/src/main/java/com/csp/platform/entity/AbacAuditLog.java`
- 字段: userId, policyId, resourceType, resourceId, action, decision, reason, ipAddress, createdAt

### 2.2 Mapper接口

**`AbacPolicyMapper.java`**
- 位置: `csp-platform/src/main/java/com/csp/platform/mapper/AbacPolicyMapper.java`
- 继承: `BaseMapper<AbacPolicy>`

**`AbacAuditLogMapper.java`**
- 位置: `csp-platform/src/main/java/com/csp/platform/mapper/AbacAuditLogMapper.java`
- 继承: `BaseMapper<AbacAuditLog>`

### 2.3 核心服务

**`AbacDecisionEngine.java`** - ABAC决策引擎
- 位置: `csp-platform/src/main/java/com/csp/platform/service/AbacDecisionEngine.java`
- 核心方法:
  - `check(LoginUser user, Object resource, String resourceType, String resourceId, String action)` - 执行决策
  - `buildUserContext(LoginUser user)` - 构建用户上下文（SpEL变量）
  - `buildResourceContext(Object resource)` - 构建资源上下文（SpEL变量）
  - `recordAudit(...)` - 记录审计日志

**决策流程**:
1. 加载匹配的策略（按resourceType + action过滤，按priority降序）
2. 构建SpEL评估上下文（user变量 + resource变量 + extraVars）
3. 依次评估策略条件表达式
4. 返回首个命中的策略效果（ALLOW/DENY）
5. 记录审计日志

### 2.4 注解增强

**`@RequireAttribute`** - ABAC属性访问控制注解
- 位置: `csp-security/src/main/java/com/csp/security/annotation/RequireAttribute.java`
- 新增属性:
  - `resourceType()` - 资源类型（策略模式）
  - `action()` - 操作类型（策略模式）
  - `resourceIdExpr()` - 资源ID SpEL表达式

**两种模式**:
- **简单模式**: 仅指定`name`和`value`，校验用户属性非空或等于指定值
- **策略模式**: 指定`resourceType`和`action`，调用ABAC决策引擎评估

### 2.5 拦截器增强

**`PermissionInterceptor.java`** - 权限拦截器
- 位置: `csp-security/src/main/java/com/csp/security/filter/PermissionInterceptor.java`
- 增强逻辑:
  - 检测`@RequireAttribute`注解
  - 判断使用简单模式还是策略模式
  - 策略模式调用`AbacDecisionEngine.check()`
  - 自动从路径变量/请求参数提取resourceId

### 2.6 REST API

**`AbacPolicyController.java`** - ABAC策略管理接口
- 位置: `csp-platform/src/main/java/com/csp/platform/controller/AbacPolicyController.java`
- 接口列表:
  - `GET /api/admin/abac/policies` - 分页查询策略
  - `GET /api/admin/abac/policies/enabled` - 查询启用的策略
  - `GET /api/admin/abac/policies/{id}` - 查询策略详情
  - `POST /api/admin/abac/policies` - 创建策略
  - `PUT /api/admin/abac/policies/{id}` - 更新策略
  - `DELETE /api/admin/abac/policies/{id}` - 删除策略
  - `PUT /api/admin/abac/policies/{id}/toggle` - 启用/禁用策略
  - `POST /api/admin/abac/policies/test` - 测试策略评估

---

## 3. 使用示例

### 3.1 简单模式（兼容旧逻辑）

```java
// 要求用户具有orgCode属性
@RequireAttribute(name = "orgCode")
@GetMapping("/need-org")
public Result<Void> needOrg() {
    return Result.ok();
}

// 要求orgCode等于参数值
@RequireAttribute(name = "orgCode", value = "${orgCode}")
@GetMapping("/match-org")
public Result<Void> matchOrg(@RequestParam String orgCode) {
    return Result.ok();
}
```

### 3.2 策略模式（ABAC引擎评估）

```java
// 通过ABAC策略评估访问控制
@RequireAttribute(resourceType = "contest", action = "view")
@GetMapping("/{contestId}")
public Result<ContestVO> getContest(@PathVariable Long contestId) {
    Contest contest = contestService.getById(contestId);
    // ABAC引擎会自动评估策略，无需手动校验
    return Result.ok(convert(contest));
}
```

### 3.3 SpEL表达式示例

```
# 用户属性访问
user.orgCode == 'SECURITY'
user.roles.contains('ADMIN')
user.id == resource.captainId

# 资源属性访问
resource.status == 'RUNNING'
resource.orgCode == user.orgCode

# 组合条件
user.roles.contains('JUDGE') && resource.contestId == user.contestId
user.orgCode == resource.orgCode || user.roles.contains('ADMIN')
```

---

## 4. 待办事项

- [ ] 编译验证（Maven SSL问题待解决）
- [ ] 单元测试
- [ ] 前端策略管理页面
- [ ] 集成测试（与现有权限系统联调）
- [ ] 文档完善（API文档、用户手册）

---

## 5. 技术要点

1. **SpEL表达式引擎**: 使用Spring Expression Language动态评估策略条件
2. **反射提取属性**: 通过getter方法自动提取资源对象属性到SpEL上下文
3. **审计日志**: 每次决策都记录详细日志，便于追溯和调试
4. **优先级机制**: 支持策略优先级，高优先级策略先评估
5. **双模式兼容**: 简单模式兼容旧逻辑，策略模式提供强大功能
6. **自动资源ID提取**: 从路径变量和请求参数自动推断资源ID

---

## 6. 下一步

Phase 1.1 ABAC属性决策引擎代码已完成，接下来：
1. 解决Maven编译问题
2. 执行数据库迁移脚本 `18_abac_policy.sql`
3. 启动后端验证功能
4. 继续 Phase 1.2: 队伍邀请码机制
