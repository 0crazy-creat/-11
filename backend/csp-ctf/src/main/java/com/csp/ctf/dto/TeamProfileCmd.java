package com.csp.ctf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 全局战队资料创建/更新入参。 */
@Data
public class TeamProfileCmd {
    @NotBlank(message = "战队名不能为空")
    @Size(max = 128, message = "战队名过长")
    private String name;
    @Size(max = 512)
    private String logo;
    private String description;
    @Size(max = 255)
    private String slogan;
    private Integer maxMembers;
}
