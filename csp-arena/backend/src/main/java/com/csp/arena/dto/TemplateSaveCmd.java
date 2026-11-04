package com.csp.arena.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 靶场模板保存请求。 */
@Data
public class TemplateSaveCmd {
    @NotBlank(message = "编码不能为空")
    private String code;
    @NotBlank(message = "名称不能为空")
    private String name;
    private String category;
    @NotBlank(message = "镜像不能为空")
    private String image;
    private Integer containerPort;
    private String difficulty;
    private String description;
    private String defaultFlag;
    private String cpuLimit;
    private String memoryLimit;
    private Integer ttlMinutes;
    private Integer status;
}
