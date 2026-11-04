package com.csp.security.annotation;

import java.lang.annotation.*;

/**
 * ABAC 属性级访问控制注解。
 * 
 * <p>支持两种模式：</p>
 * <ul>
 *   <li>简单模式：仅校验用户属性非空或等于指定值（兼容旧逻辑）</li>
 * <li>策略模式：通过 ABAC 决策引擎评估 SpEL 表达式（需指定 resourceType 和 action）</li>
 * </ul>
 * 
 * <p>示例：</p>
 * <pre>
 * // 简单模式：要求用户具有 orgCode 属性
 * {@code @RequireAttribute(name = "orgCode")}
 * 
 * // 简单模式：要求 orgCode 等于参数值
 * {@code @RequireAttribute(name = "orgCode", value = "${orgCode}")}
 * 
 * // 策略模式：通过 ABAC 策略评估
 * {@code @RequireAttribute(resourceType = "contest", action = "view")}
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAttribute {

    /** 
     * 用户属性名（简单模式）：orgCode / userId / username 等。
     * 留空表示使用策略模式。
     */
    String name() default "";

    /** 
     * 期望值（简单模式）；支持 ${} 占位（从方法参数取值），留空表示"非空即可"。
     */
    String value() default "";

    /** 
     * 资源类型（策略模式）：contest / question / team / target 等。
     * 指定后启用 ABAC 决策引擎评估。
     */
    String resourceType() default "";

    /** 
     * 操作类型（策略模式）：view / edit / delete / submit 等。
     */
    String action() default "";

    /** 
     * 资源ID表达式（策略模式）：支持 SpEL 从方法参数提取，如 "#contestId"。
     * 留空时从请求路径或参数自动推断。
     */
    String resourceIdExpr() default "";

    /** 
     * 管理员角色豁免（两种模式均生效）。
     */
    String[] bypassRoles() default {"ADMIN"};

    /** 
     * 拒绝访问时的错误消息。
     */
    String message() default "无权访问该资源（属性不匹配）";
}
