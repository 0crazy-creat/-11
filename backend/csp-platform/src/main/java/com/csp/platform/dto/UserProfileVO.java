package com.csp.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户资料视图（脱敏，绝不含 password / mfaSecret）。 */
@Data
@Builder
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String realName;
    private String studentNo;
    private String phone;
    private String email;
    private String avatar;
    private String orgCode;
    private String orgName;
    private String className;
    private Integer status;
    private Boolean mfaEnabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}