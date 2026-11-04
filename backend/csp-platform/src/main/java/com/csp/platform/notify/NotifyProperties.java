package com.csp.platform.notify;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 通知渠道配置：SMTP 邮件 + HTTP 短信网关。 */
@Data
@Component
@ConfigurationProperties(prefix = "csp.notify")
public class NotifyProperties {

    /** 邮件是否启用 */
    private boolean emailEnabled = false;
    private String mailHost = "127.0.0.1";
    private int mailPort = 1025;
    private String mailUsername = "";
    private String mailPassword = "";
    private String mailFrom = "noreply@csp.local";
    /** true=SSL(465)，false=明文/STARTTLS 可选 */
    private boolean mailSsl = false;

    /** 短信是否启用 */
    private boolean smsEnabled = false;
    /** 短信网关地址，POST JSON {mobile, content} */
    private String smsGateway = "";
    /** 网关鉴权 token，可为空 */
    private String smsToken = "";
}
