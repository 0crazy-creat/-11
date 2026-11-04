package com.csp.ctf.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 加入战队请求。 */
@Data
public class TeamJoinRequest {
    @NotNull(message = "战队ID不能为空")
    private Long teamId;
}
