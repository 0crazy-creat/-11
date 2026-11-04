package com.csp.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** JWT 配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "csp.jwt")
public class JwtProperties {
    /** 签名密钥，生产环境必须由环境变量注入 */
    private String secret = "csp-default-secret-key-please-change-in-production-env-2026";
    /** access token 有效期（秒），默认 2 小时 */
    private long expireSeconds = 7200;
    /** refresh token 有效期（秒），默认 7 天 */
    private long refreshExpireSeconds = 604800;
    private String issuer = "csp";
    private String header = "Authorization";
    private String prefix = "Bearer ";
}
