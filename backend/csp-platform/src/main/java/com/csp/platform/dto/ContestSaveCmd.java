package com.csp.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 新建/编辑赛事入参。 */
@Data
public class ContestSaveCmd {
    @NotBlank(message = "赛事编码不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,64}$", message = "赛事编码仅允许字母数字下划线短横线，3-64 位")
    private String code;

    @NotBlank(message = "赛事名称不能为空")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "赛事类型不能为空")
    @Pattern(regexp = "CTF|AWD|AWDP|THEORY|SCENARIO|CFS", message = "赛事类型非法")
    private String type;

    @Pattern(regexp = "INDIVIDUAL|TEAM", message = "赛制模式非法")
    private String mode = "TEAM";

    private LocalDateTime signupStart;
    private LocalDateTime signupEnd;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Min(value = 1, message = "参赛上限至少为1")
    private Integer maxPlayers = 1000;

    private Integer allowLate = 0;

    @Pattern(regexp = "INTERNAL|EXTERNAL", message = "网络模式非法")
    private String networkMode = "INTERNAL";

    private Integer blackout = 0;
    private Integer dynamicFlag = 1;
    private Integer firstBlood = 1;
    private String description;
    /** JSON 字符串 */
    private String config;
    private String schemaName;
    private String organizer;

    /** 时间窗一致性：开始须早于结束。 */
    @AssertTrue(message = "赛事开始时间必须早于结束时间")
    public boolean isTimeWindowValid() {
        if (startAt == null || endAt == null) return true;
        return startAt.isBefore(endAt);
    }

    @AssertTrue(message = "报名开始时间必须早于报名结束时间")
    public boolean isSignupWindowValid() {
        if (signupStart == null || signupEnd == null) return true;
        return signupStart.isBefore(signupEnd);
    }
}
