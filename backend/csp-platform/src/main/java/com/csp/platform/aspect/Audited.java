package com.csp.platform.aspect;

import java.lang.annotation.*;

/** 标记需要写审计日志的方法；value 为操作类型，resource 为资源模板。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {
    /** 操作类型，如 QUESTION_CREATE */
    String action();

    /** 资源标识，支持简单占位（如 question:{id}） */
    String resource() default "";
}
