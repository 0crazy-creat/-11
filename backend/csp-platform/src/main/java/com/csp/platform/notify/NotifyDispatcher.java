package com.csp.platform.notify;

import com.csp.platform.entity.SysUser;
import com.csp.platform.mapper.SysUserMapper;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** 真实通知投递：SMTP 发邮件、短信走 SmsSenderFactory；失败不影响站内信落库。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyDispatcher {

    private final NotifyProperties props;
    private final SysUserMapper userMapper;
    private final SmsSenderFactory smsSenderFactory;

    /** 投递结果：是否真正发出 + 说明 */
    public record Result(boolean delivered, String detail) {}

    /** 按渠道分发；userId 为空表示广播，邮件/短信需要具体用户地址，广播时不投递。 */
    public Result dispatch(NotifyChannel channel, Long userId, String title, String content) {
        return switch (channel) {
            case INBOX -> new Result(true, "站内信已入库");
            case EMAIL -> sendEmail(userId, title, content);
            case SMS -> sendSms(userId, title + "：" + content);
        };
    }

    private Result sendEmail(Long userId, String title, String content) {
        if (!props.isEmailEnabled()) return new Result(false, "邮件渠道未启用（csp.notify.email-enabled=false）");
        if (userId == null) return new Result(false, "广播通知缺少收件人邮箱");
        SysUser u = userMapper.selectById(userId);
        if (u == null || u.getEmail() == null || u.getEmail().isBlank()) {
            return new Result(false, "用户无邮箱，跳过邮件投递");
        }
        try {
            Properties p = new Properties();
            p.put("mail.smtp.host", props.getMailHost());
            p.put("mail.smtp.port", String.valueOf(props.getMailPort()));
            p.put("mail.smtp.auth", String.valueOf(!props.getMailUsername().isBlank()));
            p.put("mail.smtp.starttls.enable", "false");
            p.put("mail.smtp.ssl.enable", String.valueOf(props.isMailSsl()));
            p.put("mail.smtp.connectiontimeout", "5000");
            p.put("mail.smtp.timeout", "8000");

            Session s = Session.getInstance(p, null);
            MimeMessage m = new MimeMessage(s);
            m.setFrom(new InternetAddress(props.getMailFrom()));
            m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(u.getEmail()));
            m.setSubject(title, StandardCharsets.UTF_8.name());
            m.setText(content == null ? "" : content, StandardCharsets.UTF_8.name());
            Transport.send(m);
            log.info("[EMAIL] 已投递 -> {} ({})", u.getEmail(), title);
            return new Result(true, "邮件已发送至 " + u.getEmail());
        } catch (Exception e) {
            log.warn("[EMAIL] 投递失败 userId={} err={}", userId, e.getMessage());
            return new Result(false, "邮件投递失败：" + e.getMessage());
        }
    }

    private Result sendSms(Long userId, String text) {
        SmsSender sender = smsSenderFactory.current();
        if (sender == null) {
            return new Result(false, "短信渠道未启用（csp.notify.sms-enabled）");
        }
        if (userId == null) return new Result(false, "广播通知缺少接收手机号");
        SysUser u = userMapper.selectById(userId);
        if (u == null || u.getPhone() == null || u.getPhone().isBlank()) {
            return new Result(false, "用户无手机号，跳过短信投递");
        }
        try {
            sender.sendText(u.getPhone(), text);
            log.info("[SMS] -> {} provider={}", u.getPhone(), sender.provider());
            return new Result(true, "短信已提交（" + sender.provider() + "）");
        } catch (Exception e) {
            log.warn("[SMS] 投递失败 userId={} provider={} err={}", userId, sender.provider(), e.getMessage());
            return new Result(false, "短信投递失败：" + e.getMessage());
        }
    }
}