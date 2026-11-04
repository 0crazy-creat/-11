package com.csp.security.annotation;

import java.lang.annotation.*;

/** 标记无需登录即可访问的接口。 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicApi {
}
