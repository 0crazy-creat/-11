package com.csp.arena.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 创建靶机实例请求。 */
@Data
public class CreateInstanceCmd {
    @NotNull(message = "模板ID不能为空")
    private Long templateId;
    private Long contestId;
    private Long teamId;
    /** 自定义 flag，不传则自动生成 */
    private String flag;
    /** 存活分钟数，不传用模板默认 */
    private Integer ttlMinutes;
}
