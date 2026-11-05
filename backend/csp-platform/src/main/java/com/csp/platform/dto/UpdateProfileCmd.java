package com.csp.platform.dto;

import lombok.Data;

@Data
public class UpdateProfileCmd {
    private String nickname;
    private String realName;
    private String phone;
    private String email;
}
