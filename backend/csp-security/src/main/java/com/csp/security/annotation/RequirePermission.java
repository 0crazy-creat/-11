package com.csp.security.annotation;

import java.lang.annotation.*;

/** 要求当前用户具备指定权限点，支持多个（默认 AND 语义）。 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    String[] value();
    /** 多个权限之间的关系：AND（默认）或 OR */
    Logical logical() default Logical.AND;

    enum Logical { AND, OR }
}
