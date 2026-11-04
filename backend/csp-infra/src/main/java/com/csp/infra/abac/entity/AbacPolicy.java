package com.csp.infra.abac.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.csp.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ABAC 属性访问控制策略（abac_policy）。
 * 
 * <p>支持基于用户属性（院校/班级/角色等）和资源属性的动态访问控制。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("abac_policy")
public class AbacPolicy extends BaseEntity {

    /** 策略名称 */
    private String policyName;

    /** 策略描述 */
    private String description;

    /** 资源类型: contest/question/team/target */
    private String resourceType;

    /** 操作: view/edit/delete/submit */
    private String action;

    /** 条件表达式(SpEL) */
    private String conditionExpr;

    /** 效果: ALLOW/DENY */
    private String effect;

    /** 优先级(数字越大越优先) */
    private Integer priority;

    /** 是否启用: 0禁用 1启用 */
    private Integer enabled;
}
