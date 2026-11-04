-- ============================================================
-- ABAC 属性访问控制策略表
-- ============================================================

CREATE TABLE IF NOT EXISTS abac_policy (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    policy_name     VARCHAR(100) NOT NULL COMMENT '策略名称',
    description     VARCHAR(500) DEFAULT NULL COMMENT '策略描述',
    resource_type   VARCHAR(50)  NOT NULL COMMENT '资源类型: contest/question/team/target',
    action          VARCHAR(20)  NOT NULL COMMENT '操作: view/edit/delete/submit',
    condition_expr  TEXT         NOT NULL COMMENT '条件表达式(SpEL)',
    effect          VARCHAR(10)  NOT NULL DEFAULT 'DENY' COMMENT '效果: ALLOW/DENY',
    priority        INT          NOT NULL DEFAULT 0 COMMENT '优先级(数字越大越优先)',
    enabled         TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用: 0禁用 1启用',
    created_by      BIGINT       DEFAULT NULL COMMENT '创建人',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT       DEFAULT NULL COMMENT '更新人',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_abac_resource (resource_type, action),
    KEY idx_abac_enabled (enabled, priority DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ABAC属性访问控制策略';

-- 插入示例策略
INSERT INTO abac_policy (policy_name, description, resource_type, action, condition_expr, effect, priority, enabled)
VALUES 
    ('院校隔离-查看赛事', '仅允许同院校用户查看赛事', 'contest', 'view', 
     'user.orgCode == resource.orgCode', 'ALLOW', 10, 1),
    
    ('管理员豁免-查看赛事', '管理员角色可查看所有赛事', 'contest', 'view',
     'user.roles.contains("ADMIN")', 'ALLOW', 100, 1),
    
    ('裁判豁免-查看赛事', '裁判角色可查看负责赛事', 'contest', 'view',
     'user.roles.contains("JUDGE")', 'ALLOW', 90, 1),
    
    ('队长管理-编辑战队', '仅队长可编辑本战队信息', 'team', 'edit',
     'resource.captainId == user.id', 'ALLOW', 50, 1),
    
    ('靶机归属-查看靶机', '仅归属战队可查看靶机详情', 'target', 'view',
     'resource.teamId == user.teamId || user.roles.contains("ADMIN")', 'ALLOW', 60, 1);

-- ABAC策略审计日志表
CREATE TABLE IF NOT EXISTS abac_audit_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    policy_id       BIGINT       DEFAULT NULL COMMENT '匹配的策略ID',
    resource_type   VARCHAR(50)  NOT NULL COMMENT '资源类型',
    resource_id     VARCHAR(100) NOT NULL COMMENT '资源ID',
    action          VARCHAR(20)  NOT NULL COMMENT '操作',
    decision        VARCHAR(10)  NOT NULL COMMENT '决策结果: ALLOW/DENY',
    reason          VARCHAR(500) DEFAULT NULL COMMENT '决策原因',
    ip_address      VARCHAR(64)  DEFAULT NULL COMMENT '请求IP',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_abac_audit_user (user_id, created_at),
    KEY idx_abac_audit_resource (resource_type, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ABAC决策审计日志';
