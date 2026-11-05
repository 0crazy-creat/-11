package com.csp.platform.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信开放平台扫码登录配置。
 *
 * <p>appId/secret 任一为空则自动降级为演示模式（二维码指向平台内置确认页）。
 * 生产环境通过环境变量或 application-prod.yml 注入即可，无需改代码。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "csp.wechat")
public class WechatProperties {

    /** 微信开放平台网站应用 AppID */
    private String appId = "";
    /** 微信开放平台网站应用 AppSecret */
    private String appSecret = "";
    /** 扫码回调地址（须与开放平台配置一致），默认指向登录页 */
    private String redirectUri = "http://127.0.0.1:11001/login";
    /** 二维码会话有效期（秒） */
    private int qrExpireSeconds = 300;

    public boolean isReal() {
        return appId != null && !appId.isBlank() && appSecret != null && !appSecret.isBlank();
    }
}