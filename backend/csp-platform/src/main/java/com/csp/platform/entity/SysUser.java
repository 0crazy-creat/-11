package com.csp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.csp.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 平台用户（sys_user）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;
    /** BCrypt 哈希，任何出参均不得包含 */
    private String password;
    private String nickname;
    private String realName;
    private String studentNo;
    private String phone;
    private String email;
    private String avatar;
    private String orgCode;
    private String orgName;
    private String className;
    /** 1正常 0禁用 2锁定 */
    private Integer status;
    private Integer mfaEnabled;
    private String mfaSecret;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    /** 微信 openid（扫码登录绑定） */
    private String wechatOpenid;
    /** 微信 unionid（同一开放平台账号下唯一） */
    private String wechatUnionid;

    public boolean isEnabled() { return status != null && status == 1; }
    public boolean isLocked() { return status != null && status == 2; }
}