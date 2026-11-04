package com.csp.ctf.scenario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 场景实操赛提交证据 Flag。 */
@Data
public class ScenarioSubmitCmd {
    @NotNull(message = "赛事ID不能为空")
    private Long contestId;
    @NotNull(message = "任务ID不能为空")
    private Long questionId;
    @NotBlank(message = "flag 不能为空")
    private String flag;
}
