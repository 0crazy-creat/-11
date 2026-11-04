package com.csp.awd.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 创建/编排 AWD 轮次请求。 */
@Data
public class CreateRoundRequest {
    @NotNull(message = "竞赛ID不能为空")
    private Long contestId;

    @NotNull(message = "轮次号不能为空")
    @Min(value = 1, message = "轮次号从1开始")
    private Integer roundNo;

    /** 单轮时长（秒），默认 600 */
    @Min(value = 30, message = "单轮时长至少 30 秒")
    @Max(value = 86400, message = "单轮时长不能超过 24 小时")
    private Integer durationS = 600;

    /** 竞赛模式：AWD / AWDP，默认 AWD */
    private String mode = "AWD";

    /** 是否为所有参赛战队批量拉起靶机，默认 true */
    private Boolean provisionTargets = true;
}