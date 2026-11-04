package com.csp.platform.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/** 批量标记已读。 */
@Data
public class NotifyReadCmd {
    @NotEmpty(message = "ids 不能为空")
    private List<Long> ids;
}
