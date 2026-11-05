package com.csp.platform.notify;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通知渠道配置：SMTP 邮件 + 短信（阿里云 / 腾讯云 / 通用 HTTP 网关）。
 *
 * <pre>
 * csp:
 *   notify:
 *     email-enabled: false
 *     mail-host: smtp.example.com
 *     mail-port: 465
 *     mail-username: ${CSP_MAIL_USER:}
 *     mail-password: ${CSP_MAIL_PASS:}
 *     mail-from: noreply@example.com
 *     mail-ssl: true
 *
 *     sms-enabled: false          # 上线置 true 即启用真实短信
 *     sms-provider: aliyun        # aliyun / tencent / http
 *     sms-gateway: ""             # provider=http 时的网关地址
 *     sms-token: ""               # provider=http 时的 Bearer Token
 *
 *     aliyun:
 *       access-key-id: ${CSP_SMS_ALIYUN_AK:}
 *       access-key-secret: ${CSP_SMS_ALIYUN_SK:}
 *       sign-name: 网络安全竞赛平台
 *       template-code: SMS_123456789
 *       template-param: code
 *       notice-template-code: ""
 *       notice-template-param: content
 *       endpoint: dysmsapi.aliyuncs.com
 *
 *     tencent:
 *       secret-id: ${CSP_SMS_TENCENT_ID:}
 *       secret-key: ${CSP_SMS_TENCENT_KEY:}
 *       sdk-app-id: ""
 *       sign-name: 网络安全竞赛平台
 *       template-id: ""
 *       template-param-count: 1
 *       notice-template-id: ""
 *       region: ap-guangzhou
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "csp.notify")
public class NotifyProperties {

    /* ---------- 邮件 ---------- */
    private boolean emailEnabled = false;
    private String mailHost = "127.0.0.1";
    private int mailPort = 25;
    private String mailUsername = "";
    private String mailPassword = "";
    private String mailFrom = "noreply@csp.local";
    private boolean mailSsl = false;

    /* ---------- 短信 ---------- */
    /** 是否启用真实短信通道。false 时走本地演示模式（验证码直接返回给前端便于自测）。 */
    private boolean smsEnabled = false;
    /** 短信通道：aliyun / tencent / http。 */
    private String smsProvider = "http";
    /** provider=http 时的网关地址。 */
    private String smsGateway = "";
    /** provider=http 时的 Bearer Token。 */
    private String smsToken = "";

    private Aliyun aliyun = new Aliyun();
    private Tencent tencent = new Tencent();

    @Data
    public static class Aliyun {
        private String accessKeyId = "";
        private String accessKeySecret = "";
        private String signName = "";
        /** 验证码模板 Code，例如 SMS_123456789。 */
        private String templateCode = "";
        /** 验证码模板中的变量名，例如 code。 */
        private String templateParam = "code";
        /** 自由文本通知模板 Code（可选）。 */
        private String noticeTemplateCode = "";
        private String noticeTemplateParam = "content";
        private String endpoint = "dysmsapi.aliyuncs.com";
    }

    @Data
    public static class Tencent {
        private String secretId = "";
        private String secretKey = "";
        private String sdkAppId = "";
        private String signName = "";
        /** 验证码模板 ID。 */
        private String templateId = "";
        /** 验证码模板参数个数，默认 1。 */
        private int templateParamCount = 1;
        /** 自由文本通知模板 ID（可选）。 */
        private String noticeTemplateId = "";
        private String region = "ap-guangzhou";
    }
}