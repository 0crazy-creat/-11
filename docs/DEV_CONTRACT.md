# 开发契约（所有团队成员必读）

## 环境事实
- 工作目录：`/home/ai/网安平台项目`
- JDK：`/home/ai/网安平台项目/.toolchain/jdk-17.0.21+9`
- Maven：`/home/ai/网安平台项目/.toolchain/apache-maven-3.9.9/bin/mvn`
- Maven settings：`/home/ai/网安平台项目/.m2/settings.xml`（本地仓库 `.m2repo`）
- **HOME 不可写**，不要往 `~/.m2` 写任何东西
- 构建命令模板：
```bash
export JAVA_HOME=/home/ai/网安平台项目/.toolchain/jdk-17.0.21+9
export PATH=$JAVA_HOME/bin:/home/ai/网安平台项目/.toolchain/apache-maven-3.9.9/bin:$PATH
cd /home/ai/网安平台项目/backend
mvn -B -q -s /home/ai/网安平台项目/.m2/settings.xml -pl <模块> -am compile
```
- 基础设施（已启动，勿删）：
  - MySQL `127.0.0.1:3306`，库 `csp`，root/`csp_root_2026`
  - Redis `127.0.0.1:6379`
  - MongoDB `127.0.0.1:27017`，库 `csp`
- MySQL 容器内执行 SQL：`docker exec -i csp-mysql mysql -uroot -pcsp_root_2026 csp < 文件`

## 已有模块（已编译通过，不要改其公共 API）
| 模块 | 职责 | 关键类 |
|---|---|---|
| `csp-common` | 统一返回/异常/分页/基类/工具 | `Result` `ResultCode` `PageResult` `PageQuery` `BizException` `BaseEntity` `FlagUtils` `IpUtils` |
| `csp-security` | JWT + RBAC 鉴权 | `LoginUser` `UserContext` `JwtService` `@RequirePermission` `@RequireRole` `@PublicApi` |
| `csp-infra` | Redis/Mongo/WS/Docker | `RedisService` `RedisKeys` `SubmissionLogRepository` `WsPublisher` `TargetScheduler` |
| `csp-platform` | 启动入口 | `CspApplication`（`scanBasePackages=com.csp`，`@MapperScan("com.csp.**.mapper")`）|

## 硬性规范
1. **包名**：`com.csp.<模块>.<层次>`，层次为 `controller/service/mapper/entity/dto/vo/enums`。
2. **统一返回**：Controller 一律返回 `Result<T>`；分页返回 `Result<PageResult<T>>`。
3. **异常**：业务错误抛 `BizException`；不要自己 try-catch 后返回 Result。
4. **鉴权**：类/方法上标注 `@RequirePermission("xxx:yyy")` 或 `@RequireRole("ADMIN")`；公开接口标 `@PublicApi`。
5. **实体**：继承 `BaseEntity`（已含 id/createdBy/createdAt/updatedBy/updatedAt/deleted）。
6. **Mapper**：接口 `extends BaseMapper<T>`，放在 `com.csp.<模块>.mapper` 包，启动类已开启 `@MapperScan("com.csp.**.mapper")`。
7. **表名**：`@TableName("表名")`，字段驼峰自动映射下划线。
8. **写库前先确认表结构**：`deploy/sql/01_schema.sql` 是唯一事实来源；如确需新增表/字段，写**增量 SQL**到 `deploy/sql/03_xxx.sql` 并说明，不要改 `01_schema.sql`。
9. **注释**：中文 Javadoc + 关键逻辑行内注释，说明"为什么"。
10. **不要动别人的文件**；`csp-platform` 的 `application.yml` 只有队长可改。
11. **提交前必须自测编译**：`mvn -pl <模块> -am compile` 通过。
12. **禁止**：写 `~/.m2`、改 `pom.xml` 里的 dependencyManagement、修改已存在的 common/security/infra 公共类签名（要加东西就新增类）。

## 数据库核心表（16 张）
`sys_user` `sys_role` `sys_permission` `sys_user_role` `sys_role_permission`
`contest` `contest_team` `contest_team_member` `question` `contest_question`
`submission` `scoreboard` `awd_round` `awd_target` `audit_log` `notify_message`

## 默认账号
`admin` / `Admin@123`（BCrypt 已入库）

## 端口约定
- 后端主服务：`8080`
- 前端 dev server：`5173`
- 靶机端口池：`20000-30000`
