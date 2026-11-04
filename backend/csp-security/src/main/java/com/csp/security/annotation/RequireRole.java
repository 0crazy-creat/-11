package com.csp.security.annotation;

import java.lang.annotation.*;

/** 要求当前用户具备指定角色。 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();
    Logical logical() default Logical.OR;

    enum Logical { AND, OR }
}
