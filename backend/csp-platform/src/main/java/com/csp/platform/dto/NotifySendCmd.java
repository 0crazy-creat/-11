package com.csp.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 发送通知入参：userId 为空表示广播。 */
@Data
public class NotifySendCmd {
    private Long contestId;
    private Long userId;

    @Pattern(regexp = "INBOX|WS|EMAIL|SMS", message = "channel 非法")
    private String channel = "INBOX";

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题过长")
    private String title;

    @Size(max = 10000, message = "内容过长")
    private String content;
}
