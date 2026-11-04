package com.csp.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/** 登录结果：双 token + 用户概览。 */
@Data
@Builder
public class LoginVO {
    private String accessToken;
    private String refreshToken;
    /** access token 有效期（秒） */
    private long expiresIn;
    /** 是否需强制修改初始密码 */
    private boolean mustChangePassword;
    private UserProfileVO user;
    private Set<String> roles;
    private Set<String> permissions;
}