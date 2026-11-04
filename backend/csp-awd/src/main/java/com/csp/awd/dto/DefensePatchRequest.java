package com.csp.awd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** AWDP 修补提交：提交修复说明，经裁判或自动检查判定后获得防御加成分。 */
@Data
public class DefensePatchRequest {
    @NotNull(message = "竞赛ID不能为空")
    private Long contestId;

    @NotNull(message = "轮次号不能为空")
    private Integer roundNo;

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @NotBlank(message = "修复说明不能为空")
    private String description;

    /** 修补后的靶机验证地址（可选，供裁判复核） */
    private String verifyUrl;
}