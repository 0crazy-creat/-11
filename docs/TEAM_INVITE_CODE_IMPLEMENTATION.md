# 队伍邀请码机制实施文档

**实施时间**: 2026-11-04  
**状态**: ✅ 代码完成，待编译验证

---

## 1. 数据库设计

### 1.1 扩展 contest_team 表

```sql
ALTER TABLE contest_team 
  ADD COLUMN invite_code VARCHAR(32) DEFAULT NULL COMMENT '邀请码（唯一）',
  ADD COLUMN invite_expire_at DATETIME DEFAULT NULL COMMENT '邀请码过期时间',
  ADD COLUMN max_members INT DEFAULT 8 COMMENT '战队人数上限',
  ADD COLUMN description TEXT DEFAULT NULL COMMENT '战队简介',
  ADD COLUMN slogan VARCHAR(255) DEFAULT NULL COMMENT '战队口号',
  ADD UNIQUE KEY uk_team_invite_code (invite_code);
```

### 1.2 新增表

**`contest_team_captain_transfer` - 队长转让记录**
```sql
CREATE TABLE contest_team_captain_transfer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contest_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    from_captain BIGINT NOT NULL,
    to_captain BIGINT NOT NULL,
    reason VARCHAR(255),
    transferred_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**`contest_team_invite` - 邀请记录**
```sql
CREATE TABLE contest_team_invite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contest_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    inviter_id BIGINT NOT NULL,
    invitee_id BIGINT,
    invite_code VARCHAR(32) NOT NULL,
    status VARCHAR(16) DEFAULT 'PENDING',
    expire_at DATETIME NOT NULL,
    accepted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_invite_code (invite_code)
);
```

---

## 2. 后端实现

### 2.1 实体类增强

**`ContestTeam.java`** - 新增字段
- `inviteCode` - 邀请码（唯一）
- `inviteExpireAt` - 过期时间
- `maxMembers` - 人数上限（默认8）
- `description` - 战队简介
- `slogan` - 战队口号

**`ContestTeamCaptainTransfer.java`** - 新建实体
- 位置: `csp-ctf/src/main/java/com/csp/ctf/entity/ContestTeamCaptainTransfer.java`
- 字段: id, contestId, teamId, fromCaptain, toCaptain, reason, transferredAt

**`ContestTeamInvite.java`** - 新建实体
- 位置: `csp-ctf/src/main/java/com/csp/ctf/entity/ContestTeamInvite.java`
- 字段: id, contestId, teamId, inviterId, inviteeId, inviteCode, status, expireAt, acceptedAt, createdAt

### 2.2 Mapper接口

**`ContestTeamCaptainTransferMapper.java`**
- 位置: `csp-ctf/src/main/java/com/csp/ctf/mapper/ContestTeamCaptainTransferMapper.java`

**`ContestTeamInviteMapper.java`**
- 位置: `csp-ctf/src/main/java/com/csp/ctf/mapper/ContestTeamInviteMapper.java`

### 2.3 服务层增强

**`ContestEnrollService.java`** - 新增方法

#### generateInviteCode(Long teamId)
- 生成8位随机邀请码（排除易混淆字符0/O, 1/I/L）
- 设置24小时有效期
- 确保唯一性（冲突时重新生成）

#### joinByInviteCode(Long userId, Long contestId, String inviteCode)
- 验证邀请码有效性
- 检查是否过期
- 检查人数上限
- 调用joinTeam完成加入

#### refreshInviteCode(Long teamId, Long userId)
- 验证队长权限
- 重新生成邀请码

#### 自动生成长
- 在`createTeam()`方法中自动调用`generateInviteCode()`

### 2.4 REST API

**`CtfEnrollController.java`** - 新增接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/ctf/contests/{contestId}/teams/{teamId}/invite-code` | 生成/获取邀请码 |
| POST | `/api/ctf/contests/{contestId}/teams/join-by-code` | 通过邀请码加入 |
| PUT | `/api/ctf/contests/{contestId}/teams/{teamId}/invite-code` | 刷新邀请码 |

---

## 3. 邀请码算法

### 3.1 字符集
```
ABCDEFGHJKMNPQRSTUVWXYZ23456789
```
- 排除: 0, O, 1, I, L（易混淆字符）
- 长度: 8位
- 总组合数: 31^8 ≈ 852亿种

### 3.2 生成流程
1. 使用`SecureRandom`生成随机字符串
2. 查询数据库验证唯一性
3. 冲突时重新生成（最多重试10次）
4. 设置过期时间为当前时间 + 24小时
5. 更新到`contest_team`表

### 3.3 验证流程
1. 根据邀请码查询战队
2. 检查是否过期（`invite_expire_at < now()`）
3. 检查人数上限（`members >= max_members`）
4. 检查用户是否已报名（`uk_ctm`约束）
5. 通过则调用`joinTeam()`

---

## 4. 使用示例

### 4.1 创建战队（自动生成邀请码）
```javascript
POST /api/ctf/contests/1001/teams
{
  "name": "闪电队",
  "code": "SD",
  "orgCode": "XX大学"
}

// 响应
{
  "code": 200,
  "data": {
    "id": 5001,
    "name": "闪电队",
    "inviteCode": "ABC123XY",
    "inviteExpireAt": "2026-11-05T10:00:00"
  }
}
```

### 4.2 通过邀请码加入
```javascript
POST /api/ctf/contests/1001/teams/join-by-code?inviteCode=ABC123XY

// 响应
{
  "code": 200,
  "data": {
    "id": 5001,
    "name": "闪电队",
    "members": 2
  }
}
```

### 4.3 刷新邀请码
```javascript
PUT /api/ctf/contests/1001/teams/5001/invite-code

// 响应
{
  "code": 200,
  "data": "XYZ789AB"
}
```

---

## 5. 前端集成（待实现）

### 5.1 战队管理页
- 显示邀请码卡片（码值、过期时间、刷新按钮）
- 生成二维码（使用`qrcode.js`）
- 复制邀请链接

### 5.2 加入战队弹窗
- 输入邀请码
- 验证并加入
- 错误提示（无效/过期/已满）

### 5.3 二维码分享
- 生成邀请二维码
- 扫码直接跳转到加入确认页

---

## 6. 安全考虑

1. **邀请码唯一性**: 数据库唯一索引保证
2. **过期机制**: 24小时自动失效
3. **权限校验**: 仅队长可生成/刷新邀请码
4. **防暴力破解**: 8位码+24小时过期，破解成本极高
5. **审计日志**: 可记录邀请码使用情况（待实现）

---

## 7. 下一步

Phase 1.2 队伍邀请码机制代码已完成，接下来：
1. 执行数据库迁移脚本 `19_team_invite_code.sql`
2. 编译验证后端代码
3. 实现前端邀请码UI
4. 继续 Phase 1.3: AWD自动注册计分
