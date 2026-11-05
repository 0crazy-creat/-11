package com.csp.platform.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 按 {@code csp.notify.sms-provider} 选择短信通道实现。
 * 取值：aliyun / tencent / http（默认）。
 */
@Slf4j
@Component
public class SmsSenderFactory {

    private final NotifyProperties props;
    private final AliyunSmsSender aliyun;
    private final TencentSmsSender tencent;
    private final HttpGatewaySmsSender http;

    public SmsSenderFactory(NotifyProperties props,
                            AliyunSmsSender aliyun,
                            TencentSmsSender tencent,
                            HttpGatewaySmsSender http) {
        this.props = props;
        this.aliyun = aliyun;
        this.tencent = tencent;
        this.http = http;
    }

    /** 返回当前激活的通道；未启用短信或未知 provider 时返回 null。 */
    public SmsSender current() {
        if (!props.isSmsEnabled()) return null;
        String p = props.getSmsProvider();
        String key = p == null ? "" : p.trim().toLowerCase();
        SmsSender sender = switch (key) {
            case "aliyun" -> aliyun;
            case "tencent" -> tencent;
            case "http", "" -> http;
            default -> {
                log.warn("未知短信通道 {}，回退到 http", p);
                yield http;
            }
        };
        return sender;
    }

    /** 是否已配置真实短信通道（用于演示模式判定）。 */
    public boolean isRealConfigured() {
        if (!props.isSmsEnabled()) return false;
        String p = props.getSmsProvider();
        String key = p == null ? "" : p.trim().toLowerCase();
        return switch (key) {
            case "aliyun" -> !isBlank(props.getAliyun().getAccessKeyId())
                    && !isBlank(props.getAliyun().getAccessKeySecret())
                    && !isBlank(props.getAliyun().getTemplateCode());
            case "tencent" -> !isBlank(props.getTencent().getSecretId())
                    && !isBlank(props.getTencent().getSecretKey())
                    && !isBlank(props.getTencent().getSdkAppId())
                    && !isBlank(props.getTencent().getTemplateId());
            case "http", "" -> !isBlank(props.getSmsGateway());
            default -> false;
        };
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}