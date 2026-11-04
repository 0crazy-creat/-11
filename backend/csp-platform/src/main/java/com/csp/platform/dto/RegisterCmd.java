package com.csp.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 自助注册请求。 */
@Data
public class RegisterCmd {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度需为 3-64 位")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "用户名仅支持字母、数字、下划线、点和短横线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需为 8-64 位")
    private String password;

    @Size(max = 64, message = "昵称过长")
    private String nickname;

    @Size(max = 64, message = "真实姓名过长")
    private String realName;

    @Size(max = 32, message = "手机号过长")
    private String phone;

    @Size(max = 128, message = "邮箱过长")
    private String email;
}
