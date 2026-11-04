# 依赖修复说明

## 问题描述

在 Phase 1 实施过程中，遇到了两个循环依赖问题，需要通过重构模块结构来解决。

---

## 问题 1: ABAC 引擎位置导致的循环依赖

### 原始架构

```
csp-security (PermissionInterceptor)
    ↓ 需要调用
csp-platform (AbacDecisionEngine)
    ↓ 但 csp-security 不能依赖 csp-platform
```

### 问题原因

- `PermissionInterceptor` 在 `csp-security` 模块
- `AbacDecisionEngine` 最初放在 `csp-platform` 模块
- `csp-security` 是底层模块，`csp-platform` 是上层模块
- 底层模块不能依赖上层模块（会造成循环依赖）

### 解决方案

将 ABAC 相关类从 `csp-platform` 移到 `csp-infra`：

**移动的文件**：
- `AbacPolicy.java` → `csp-infra/src/main/java/com/csp/infra/abac/entity/`
- `AbacAuditLog.java` → `csp-infra/src/main/java/com/csp/infra/abac/entity/`
- `AbacPolicyMapper.java` → `csp-infra/src/main/java/com/csp/infra/abac/mapper/`
- `AbacAuditLogMapper.java` → `csp-infra/src/main/java/com/csp/infra/abac/mapper/`
- `AbacDecisionEngine.java` → `csp-infra/src/main/java/com/csp/infra/abac/service/`

**修改的配置**：
- `csp-security/pom.xml` - 添加 `csp-infra` 依赖
- 所有相关文件的 `package` 声明
- 所有相关文件的 `import` 语句

### 修复后架构

```
csp-security (PermissionInterceptor)
    ↓ 依赖
csp-infra (AbacDecisionEngine)
    ↓ 依赖
csp-common
```

---

## 问题 2: LoginUser 位置导致的循环依赖

### 原始架构

```
csp-infra (AbacDecisionEngine)
    ↓ 需要使用
csp-security (LoginUser)
    ↓ 但 csp-infra 不能依赖 csp-security
```

### 问题原因

- `AbacDecisionEngine` 在 `csp-infra` 模块
- `LoginUser` 在 `csp-security` 模块
- `csp-infra` 和 `csp-security` 是同级模块
- 如果 `csp-infra` 依赖 `csp-security`，会造成：
  - `csp-security` → `csp-infra` → `csp-security`（循环）

### 解决方案

将 `LoginUser` 从 `csp-security` 移到 `csp-common`：

**移动的文件**：
- `LoginUser.java` → `csp-common/src/main/java/com/csp/common/security/`

**修改的配置**：
- `LoginUser.java` 的 `package` 声明
- 所有使用 `LoginUser` 的文件的 `import` 语句（约 20 个文件）

### 修复后架构

```
csp-security (PermissionInterceptor, JwtService)
    ↓ 依赖
csp-infra (AbacDecisionEngine)
    ↓ 依赖
csp-common (LoginUser)
```

---

## 最终模块依赖图

```
Layer 0 (foundation):   csp-common
                        ├── LoginUser
                        ├── ResultCode
                        ├── BizException
                        └── 其他基础类

Layer 1 (infra):        csp-infra → csp-common
                        ├── AbacDecisionEngine
                        ├── AbacPolicy/AbacAuditLog
                        ├── RedisService
                        ├── WsPublisher
                        └── 其他基础设施服务

Layer 1 (security):     csp-security → csp-common, csp-infra
                        ├── PermissionInterceptor
                        ├── JwtService
                        ├── RequireAttribute/RequirePermission
                        └── 安全相关注解和拦截器

Layer 2 (business):     csp-ctf → csp-security, csp-infra
                        ├── 邀请码机制
                        ├── 反作弊分析
                        ├── AWD 自动注册
                        └── CTF 业务逻辑

Layer 2 (business):     csp-awd → csp-security, csp-infra
                        ├── 流量取证
                        └── AWD 业务逻辑

Layer 3 (aggregation):  csp-platform → csp-security, csp-infra, csp-ctf, csp-awd, csp-situation, csp-cfs
                        ├── AbacPolicyController
                        ├── 其他管理 API
                        └── 应用入口
```

---

## 修改文件清单

### 移动的文件（6 个）

1. `AbacPolicy.java`
   - 从：`csp-platform/src/main/java/com/csp/platform/entity/`
   - 到：`csp-infra/src/main/java/com/csp/infra/abac/entity/`
   - 包名：`com.csp.platform.entity` → `com.csp.infra.abac.entity`

2. `AbacAuditLog.java`
   - 从：`csp-platform/src/main/java/com/csp/platform/entity/`
   - 到：`csp-infra/src/main/java/com/csp/infra/abac/entity/`
   - 包名：`com.csp.platform.entity` → `com.csp.infra.abac.entity`

3. `AbacPolicyMapper.java`
   - 从：`csp-platform/src/main/java/com/csp/platform/mapper/`
   - 到：`csp-infra/src/main/java/com/csp/infra/abac/mapper/`
   - 包名：`com.csp.platform.mapper` → `com.csp.infra.abac.mapper`

4. `AbacAuditLogMapper.java`
   - 从：`csp-platform/src/main/java/com/csp/platform/mapper/`
   - 到：`csp-infra/src/main/java/com/csp/infra/abac/mapper/`
   - 包名：`com.csp.platform.mapper` → `com.csp.infra.abac.mapper`

5. `AbacDecisionEngine.java`
   - 从：`csp-platform/src/main/java/com/csp/platform/service/`
   - 到：`csp-infra/src/main/java/com/csp/infra/abac/service/`
   - 包名：`com.csp.platform.service` → `com.csp.infra.abac.service`

6. `LoginUser.java`
   - 从：`csp-security/src/main/java/com/csp/security/core/`
   - 到：`csp-common/src/main/java/com/csp/common/security/`
   - 包名：`com.csp.security.core` → `com.csp.common.security`

### 修改的配置文件（2 个）

1. `csp-security/pom.xml`
   - 添加依赖：`csp-infra`

2. 约 20 个 Java 文件
   - 更新 `import` 语句：
     - `import com.csp.security.core.LoginUser;` → `import com.csp.common.security.LoginUser;`
     - `import com.csp.platform.entity.AbacPolicy;` → `import com.csp.infra.abac.entity.AbacPolicy;`
     - `import com.csp.platform.mapper.AbacPolicyMapper;` → `import com.csp.infra.abac.mapper.AbacPolicyMapper;`
     - `import com.csp.platform.service.AbacDecisionEngine;` → `import com.csp.infra.abac.service.AbacDecisionEngine;`

---

## 设计原则

### 1. 单向依赖
- 上层模块可以依赖下层模块
- 下层模块不能依赖上层模块
- 同级模块之间不能有依赖

### 2. 基础类下沉
- 被多个模块使用的类应该放在更底层的模块
- `LoginUser` 被 security 和 infra 都使用，所以放在 common
- `AbacDecisionEngine` 被 security 和 platform 都使用，所以放在 infra

### 3. 横切关注点独立
- ABAC 是横切关注点，不属于任何特定业务模块
- 放在 infra 层，作为基础设施服务
- 所有需要访问控制的模块都可以使用

---

## 验证方法

### 编译验证
```bash
cd backend
mvn clean compile -DskipTests
```

### 依赖检查
```bash
mvn dependency:tree
```

### 循环依赖检测
```bash
mvn enforcer:enforce
```

---

## 经验总结

### 1. 模块设计要提前规划
- 在开始编码前确定模块结构
- 明确每个模块的职责和依赖关系
- 避免后期大规模重构

### 2. 基础类要放在底层
- 被多个模块使用的类应该放在 common 或 infra
- 避免放在业务模块中
- 减少循环依赖风险

### 3. 使用依赖注入
- 通过接口和依赖注入解耦
- 避免直接依赖具体实现
- 便于测试和维护

### 4. 定期编译检查
- 每次修改后都要编译
- 及时发现依赖问题
- 避免问题积累

---

## 参考资料

- [Maven 多模块项目最佳实践](https://maven.apache.org/guides/mini/guide-multiple-modules.html)
- [Spring Boot 模块化设计](https://spring.io/guides/tutorials/modular-multi-project/)
- [循环依赖解决方案](https://www.baeldung.com/circular-dependencies-in-spring)

---

**文档版本**：v1.0  
**更新日期**：2026-11-04  
**作者**：AI Assistant
