package com.csp.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 战队成员加入/移除入参。 */
@Data
public class TeamMemberCmd {
    @NotNull(message = "用户 id 不能为空")
    private Long userId;
    /** CAPTAIN/MEMBER */
    private String role = "MEMBER";
}
