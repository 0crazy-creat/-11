package com.csp.platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger / OpenAPI 文档配置。 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI cspOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("网络安全竞赛平台 API")
                        .description("统一底座 + CTF + AWD/AWDP + 态势感知，支持 RESTful 与 WebSocket")
                        .version("1.0.0")
                        .contact(new Contact().name("CSP Platform"))
                        .license(new License().name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("在此填入登录接口返回的 accessToken")));
    }
}
