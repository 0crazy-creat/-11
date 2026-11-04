package com.csp.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 登录请求。 */
@Data
public class LoginCmd {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名过长")
    /** 用户名（与 phone / thirdToken 三选一） */
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 128, message = "密码过长")
    private String password;

    /** 可选：登录时指定进入的赛事命名空间 */
    private Long contestId;

    /** 双因素验证码（mfa_enabled=1 时必填） */
    private String mfaCode;

    /** 手机号登录（免密走短信验证码时配合 smsCode） */
    private String phone;
    /** 短信验证码 */
    private String smsCode;
    /** 第三方登录：provider(wechat/dingtalk/github) + token */
    private String provider;
    private String thirdToken;
}