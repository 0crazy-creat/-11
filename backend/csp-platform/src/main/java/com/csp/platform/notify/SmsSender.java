package com.csp.platform.notify;

/**
 * 短信发送通道抽象。
 *
 * <p>由 {@code csp.notify.sms-provider} 决定激活哪种实现：
 * {@code aliyun} / {@code tencent} / {@code http}。未启用短信时不会实例化。</p>
 */
public interface SmsSender {

    /** 发送验证码短信。code 用于模板变量；实现方按需自行拼接文案。 */
    void sendCode(String phone, String code) throws Exception;

    /** 发送任意通知文本（站内信转短信等）。不支持自由文本的通道应抛出异常。 */
    void sendText(String phone, String text) throws Exception;

    /** 通道标识，便于日志定位。 */
    String provider();
}