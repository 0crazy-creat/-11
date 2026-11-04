package com.csp.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 赛事备案归档入参。 */
@Data
public class ArchiveCmd {
    /** 备案编号（主办方/监管方备案号） */
    @NotBlank(message = "备案编号不能为空")
    @Size(max = 128, message = "备案编号过长")
    private String filingNo;

    /** 备案单位 */
    @Size(max = 255)
    private String filingOrg;

    /** 归档说明 */
    @Size(max = 2000)
    private String remark;
}
