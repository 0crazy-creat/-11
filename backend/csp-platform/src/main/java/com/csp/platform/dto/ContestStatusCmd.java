package com.csp.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 赛事状态流转入参。 */
@Data
public class ContestStatusCmd {
    @NotBlank(message = "目标状态不能为空")
    @Pattern(regexp = "DRAFT|PENDING|SIGNUP|RUNNING|FINISHED|ARCHIVED", message = "目标状态非法")
    private String status;
    private String reason;
}
