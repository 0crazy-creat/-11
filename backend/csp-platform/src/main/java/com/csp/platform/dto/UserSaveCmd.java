package com.csp.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** 新增/编辑用户。 */
@Data
public class UserSaveCmd {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度需为 3-64 位")
    private String username;

    /** 仅新增时生效；为空则使用默认初始密码 */
    @Size(min = 8, max = 64, message = "密码长度需为 8-64 位")
    private String password;

    private String nickname;
    private String realName;
    private String studentNo;

    @Size(max = 32, message = "手机号过长")
    private String phone;

    @Size(max = 128, message = "邮箱过长")
    private String email;

    private String avatar;
    private String orgCode;
    private String orgName;
    private String className;

    /** 1正常 0禁用 2锁定 */
    private Integer status;

    /** 角色码列表 */
    private List<String> roles;
}