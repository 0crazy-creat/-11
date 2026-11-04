package com.csp.platform.config;

import com.csp.platform.aspect.AuditInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 注册审计拦截器（无 AOP 依赖，基于 MVC HandlerInterceptor）。 */
@Configuration
@RequiredArgsConstructor
public class AuditWebConfig implements WebMvcConfigurer {

    private final AuditInterceptor auditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor).addPathPatterns("/api/**");
    }
}
