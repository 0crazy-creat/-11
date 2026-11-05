package com.csp.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 第三方登录请求（provider + thirdToken）。 */
@Data
public class ThirdLoginCmd {

    @NotBlank(message = "provider 不能为空")
    private String provider;

    @NotBlank(message = "thirdToken 不能为空")
    private String thirdToken;
}