package com.csp.platform.controller;

import com.csp.common.api.Result;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.infra.redis.RedisService;
import com.csp.platform.dto.RegisterCmd;
import com.csp.platform.service.AuthService;
import com.csp.security.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 账号找回与二次验证（需求 41 段）：
 * 邮箱注册 / 邮箱找回密码 / 验证码校验 / 2FA 开关与校验。
 */
@Slf4j
@Tag(name = "账号找回与二次验证")
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountRecoveryController {

    private final RedisService redisService;
    private final AuthService authService;

    private static final String RESET_PREFIX = "csp:pwd-reset:";
    private static final String EMAIL_CODE_PREFIX = "csp:email-code:";
    private static final String MFA_PREFIX = "csp:mfa:";

    @Operation(summary = "发送邮箱验证码（本地验证直接返回）")
    @PublicApi
    @PostMapping("/email-code")
    public Result<Map<String, String>> emailCode(@RequestParam String email) {
        if (email == null || !email.contains("@")) {
            throw new BizException(ResultCode.BAD_REQUEST, "邮箱格式不正确");
        }
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        redisService.set(EMAIL_CODE_PREFIX + email, code, Duration.ofMinutes(10));
        log.info("[EMAIL] 验证码已下发 email={} code={}", email, code);
        return Result.ok(Map.of("email", email, "code", code, "ttlSeconds", "600"));
    }

    @Operation(summary = "邮箱注册")
    @PublicApi
    @PostMapping("/register-email")
    public Result<Void> registerEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String username = body.get("username");
        String password = body.get("password");
        if (email == null || code == null || username == null || password == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "email/code/username/password 均不能为空");
        }
        String expect = redisService.get(EMAIL_CODE_PREFIX + email);
        if (expect == null || !expect.equals(code.trim())) {
            throw new BizException(ResultCode.UNAUTHORIZED, "邮箱验证码错误或已过期");
        }
        RegisterCmd cmd = new RegisterCmd();
        cmd.setUsername(username);
        cmd.setPassword(password);
        cmd.setEmail(email);
        authService.register(cmd);
        redisService.delete(EMAIL_CODE_PREFIX + email);
        return Result.ok();
    }

    @Operation(summary = "申请找回密码（返回一次性重置令牌）")
    @PublicApi
    @PostMapping("/forgot-password")
    public Result<Map<String, String>> forgotPassword(@RequestParam String email) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisService.set(RESET_PREFIX + token, email, Duration.ofMinutes(15));
        log.info("[PWD-RESET] 已生成重置令牌 email={}", email);
        return Result.ok(Map.of("email", email, "resetToken", token, "ttlSeconds", "900"));
    }

    @Operation(summary = "用重置令牌设置新密码")
    @PublicApi
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("resetToken");
        String newPassword = body.get("newPassword");
        if (token == null || newPassword == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "resetToken/newPassword 不能为空");
        }
        String email = redisService.get(RESET_PREFIX + token);
        if (email == null) throw new BizException(ResultCode.UNAUTHORIZED, "重置令牌无效或已过期");
        AuthService.validatePasswordStrength(newPassword);
        authService.resetPasswordByEmail(email, newPassword);
        redisService.delete(RESET_PREFIX + token);
        return Result.ok();
    }

    @Operation(summary = "开启/关闭双因素认证")
    @PostMapping("/mfa")
    public Result<Map<String, Object>> toggleMfa(@RequestParam Long userId,
                                                 @RequestParam Boolean enabled) {
        authService.setMfaEnabled(userId, Boolean.TRUE.equals(enabled));
        String secret = null;
        if (Boolean.TRUE.equals(enabled)) {
            secret = java.util.Base64.getEncoder().encodeToString(
                    UUID.randomUUID().toString().getBytes());
            redisService.set(MFA_PREFIX + userId, secret, Duration.ofDays(365));
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", userId);
        m.put("mfaEnabled", enabled);
        m.put("secret", secret);
        return Result.ok(m);
    }

    @Operation(summary = "校验 TOTP 验证码（简化：6 位校验码 = secret 前 6 位）")
    @PublicApi
    @PostMapping("/mfa/verify")
    public Result<Map<String, Boolean>> verifyMfa(@RequestParam Long userId,
                                                  @RequestParam String code) {
        String secret = redisService.get(MFA_PREFIX + userId);
        boolean ok = secret != null && code != null
                && secret.startsWith(code.trim());
        return Result.ok(Map.of("valid", ok));
    }
}
