package com.csp.ctf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 战队报名请求。 */
@Data
public class TeamCreateRequest {
    @NotNull(message = "赛事ID不能为空")
    private Long contestId;

    @NotBlank(message = "战队名不能为空")
    @Size(max = 128, message = "战队名过长")
    private String name;

    @Size(max = 64, message = "战队编码过长")
    private String code;

    private String logo;
    private String orgCode;
}
