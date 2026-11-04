package com.csp.platform.notify;

/** 通知渠道枚举。 */
public enum NotifyChannel {
    /** 站内信（默认，落库） */
    INBOX,
    /** 邮件（SMTP） */
    EMAIL,
    /** 短信（HTTP 网关） */
    SMS;

    public static NotifyChannel of(String v) {
        if (v == null || v.isBlank()) return INBOX;
        try {
            return valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return INBOX;
        }
    }
}
