package com.csp.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 战队创建/更新入参。 */
@Data
public class TeamSaveCmd {
    @NotBlank(message = "战队名不能为空")
    @Size(max = 128)
    private String name;
    private String code;
    private String logo;
    private String orgCode;
    /** 队长用户 id，为空则为当前登录用户 */
    private Long captainId;
}
