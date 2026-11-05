package com.csp.platform.service;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.notify.SmsSender;
import com.csp.platform.notify.NotifyProperties;
import com.csp.platform.notify.SmsSenderFactory;
import com.csp.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * 短信验证码服务：生成、下发、校验。
 *
 * <ul>
 *   <li><b>本地/演示模式</b>（csp.notify.sms-enabled=false 或未配置网关）：生成验证码写入 Redis，
 *       并原样返回给调用方便于自测；</li>
 *   <li><b>真实网关模式</b>：POST JSON {mobile, content} 到 csp.notify.sms-gateway，返回 null（不回显）；</li>
 *   <li>无论哪种模式，都带 <b>60s 重发限制</b>、<b>单日发送上限</b>、<b>5 次校验失败锁定</b>。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final RedisService redisService;
    private final SmsSenderFactory smsSenderFactory;

    private static final SecureRandom RANDOM = new SecureRandom();
    /** 验证码有效期 */
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    /** 同一手机号重发间隔 */
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);
    /** 同一手机号单日发送上限 */
    private static final int DAILY_LIMIT = 10;
    /** 校验失败次数上限，超过后作废验证码 */
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private static final String KEY_CODE = "csp:sms:code:";
    private static final String KEY_RESEND = "csp:sms:resend:";
    private static final String KEY_DAILY = "csp:sms:daily:";
    private static final String KEY_FAIL = "csp:sms:fail:";

    /**
     * 生成并下发验证码。
     *
     * @return 本地模式返回明文验证码；真实网关模式返回 {@code null}（避免泄露）
     */
    public String send(String phone) {
        String p = normalize(phone);

        // 1) 60s 重发限制：SET NX EX，抢到锁（此前不存在）才允许发送
        if (!redisService.tryLock(KEY_RESEND + p, "1", RESEND_INTERVAL)) {
            throw new BizException(ResultCode.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后再试");
        }
        // 2) 单日上限
        Long daily = redisService.incrementString(KEY_DAILY + p);
        if (daily != null && daily == 1L) {
            redisService.expire(KEY_DAILY + p, Duration.ofDays(1));
        }
        if (daily != null && daily > DAILY_LIMIT) {
            // 已达上限，回滚重发锁，避免用户被无谓锁定 60s
            redisService.delete(KEY_RESEND + p);
            throw new BizException(ResultCode.TOO_MANY_REQUESTS, "今日验证码发送次数已达上限");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        redisService.setString(KEY_CODE + p, code, CODE_TTL);
        redisService.delete(KEY_FAIL + p);
        // 重发锁已在步骤 1 通过 tryLock 写入，无需重复 setString

        SmsSender sender = smsSenderFactory.current();

        if (sender != null) {
            try {
                sender.sendCode(p, code);
                log.info("短信验证码已通过 [{}] 下发 phone={}", sender.provider(), mask(p));
                return null;
            } catch (Exception e) {
                // 下发失败，回滚限流锁，允许用户重试
                redisService.delete(KEY_RESEND + p);
                redisService.delete(KEY_CODE + p);
                log.error("短信通道 [{}] 下发失败 phone={} err={}",
                        sender.provider(), mask(p), e.getMessage());
                throw new BizException(ResultCode.INTERNAL_ERROR, "短信发送失败，请稍后再试");
            }
        }

        log.info("短信验证码（本地模式）phone={} code={}", mask(p), code);
        return code;
    }

    /** 校验验证码，通过后立即作废（一次性）。 */
    public void verify(String phone, String code) {
        String p = normalize(phone);
        if (code == null || code.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "请输入短信验证码");
        }
        String saved = redisService.getString(KEY_CODE + p);
        if (saved == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "验证码已过期，请重新获取");
        }

        Long fails = redisService.incrementString(KEY_FAIL + p);
        if (fails != null && fails == 1L) {
            redisService.expire(KEY_FAIL + p, CODE_TTL);
        }
        if (fails != null && fails > MAX_VERIFY_ATTEMPTS) {
            redisService.delete(KEY_CODE + p);
            throw new BizException(ResultCode.TOO_MANY_REQUESTS, "验证码错误次数过多，请重新获取");
        }

        if (!saved.equals(code.trim())) {
            throw new BizException(ResultCode.BAD_REQUEST, "验证码错误");
        }
        // 一次性：校验通过即作废
        redisService.delete(KEY_CODE + p);
        redisService.delete(KEY_FAIL + p);
    }

    private String normalize(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "手机号不能为空");
        }
        String p = phone.trim();
        if (!p.matches("^1[3-9]\\d{9}$")) {
            throw new BizException(ResultCode.BAD_REQUEST, "手机号格式不正确");
        }
        return p;
    }

    private String mask(String phone) {
        return phone.length() >= 7 ? phone.substring(0, 3) + "****" + phone.substring(7) : "***";
    }

    
}