package com.csp.platform.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员重置密码；不传则回退到默认初始密码。 */
@Data
public class ResetPasswordCmd {

    @Size(min = 8, max = 64, message = "密码长度需为 8-64 位")
    private String newPassword;
}