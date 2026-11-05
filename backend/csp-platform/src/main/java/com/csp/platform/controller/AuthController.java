package com.csp.platform.controller;

import com.csp.common.api.Result;
import com.csp.common.util.IpUtils;
import com.csp.platform.dto.ChangePasswordCmd;
import com.csp.platform.dto.LoginCmd;
import com.csp.platform.dto.RegisterCmd;
import com.csp.platform.dto.UpdateProfileCmd;
import com.csp.platform.dto.UserSaveCmd;
import com.csp.platform.dto.LoginVO;
import com.csp.platform.dto.RefreshCmd;
import com.csp.platform.service.AuthService;
import com.csp.platform.service.UserService;
import com.csp.security.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 认证接口：登录 / 刷新 / 登出 / 当前用户 / 改密。 */
@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PublicApi
    @Operation(summary = "账号密码登录，返回双 token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginCmd cmd, HttpServletRequest request) {
        return Result.ok(authService.login(cmd, IpUtils.getClientIp(request)));
    }

    @PublicApi
    @Operation(summary = "自助注册（默认 PLAYER 角色，注册后直接登录）")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterCmd cmd, HttpServletRequest request) {
        UserSaveCmd save = new UserSaveCmd();
        save.setUsername(cmd.getUsername());
        save.setPassword(cmd.getPassword());
        save.setNickname(StringUtils.hasText(cmd.getNickname()) ? cmd.getNickname() : cmd.getUsername());
        save.setRealName(cmd.getRealName());
        save.setPhone(cmd.getPhone());
        save.setEmail(cmd.getEmail());
        save.setStatus(1);
        userService.createWithDefaultRole(save, "PLAYER");

        LoginCmd login = new LoginCmd();
        login.setUsername(cmd.getUsername());
        login.setPassword(cmd.getPassword());
        return Result.ok(authService.login(login, IpUtils.getClientIp(request)), "注册成功");
    }

    @PublicApi
    @Operation(summary = "使用 refresh token 换取新令牌")
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@Valid @RequestBody RefreshCmd cmd) {
        return Result.ok(authService.refresh(cmd.getRefreshToken()));
    }

    @Operation(summary = "登出（无状态 JWT，客户端丢弃令牌）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    @Operation(summary = "当前登录用户资料与权限")
    @GetMapping({"/me", "/profile"})
    public Result<LoginVO> me() {
        return Result.ok(authService.currentUser());
    }

    @Operation(summary = "更新本人资料（昵称、姓名、手机、邮箱）")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UpdateProfileCmd cmd) {
        authService.updateProfile(cmd);
        return Result.ok();
    }

    @Operation(summary = "修改本人密码")
    @PostMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordCmd cmd) {
        authService.changePassword(cmd.getOldPassword(), cmd.getNewPassword());
        return Result.ok();
    }

    @Operation(summary = "下发短信验证码（本地模式返回 code 便于自测，真实网关模式不返回）")
    @PublicApi
    @PostMapping("/sms-code")
    public Result<java.util.Map<String, String>> smsCode(@RequestParam String phone) {
        String code = authService.issueSmsCode(phone);
        java.util.Map<String, String> body = new java.util.LinkedHashMap<>();
        body.put("phone", phone);
        body.put("ttlSeconds", "300");
        // 仅本地/演示模式会返回明文 code；真实短信网关返回 null，此处不下发，避免泄露
        if (code != null && !code.isBlank()) {
            body.put("code", code);
        }
        return Result.ok(body);
    }

    @Operation(summary = "手机号 + 短信验证码登录")
    @PublicApi
    @PostMapping("/login-phone")
    public Result<LoginVO> loginPhone(@Valid @RequestBody com.csp.platform.dto.PhoneLoginCmd cmd, HttpServletRequest request) {
        return Result.ok(authService.loginByPhone(cmd.getPhone(), cmd.getSmsCode(), IpUtils.getClientIp(request)));
    }

    @Operation(summary = "第三方登录（wechat/dingtalk/github）")
    @PublicApi
    @PostMapping("/login-third")
    public Result<LoginVO> loginThird(@Valid @RequestBody com.csp.platform.dto.ThirdLoginCmd cmd, HttpServletRequest request) {
        return Result.ok(authService.loginByThird(cmd.getProvider(), cmd.getThirdToken(), IpUtils.getClientIp(request)));
    }

    // ---------------- 微信扫码登录（需求 41 段） ----------------
    @Operation(summary = "生成微信扫码登录二维码（state 会话）")
    @PublicApi
    @PostMapping("/wechat/qrcode")
    public Result<java.util.Map<String, Object>> wechatQrcode(HttpServletRequest request) {
        return Result.ok(authService.createWechatQrSession());
    }

    @Operation(summary = "查询扫码状态（前端轮询）")
    @PublicApi
    @GetMapping("/wechat/status")
    public Result<java.util.Map<String, Object>> wechatStatus(@RequestParam String state) {
        return Result.ok(authService.queryWechatState(state));
    }

    @Operation(summary = "模拟微信回调（本地验证/网关回调）")
    @PublicApi
    @PostMapping("/wechat/callback")
    public Result<java.util.Map<String, Object>> wechatCallback(@RequestBody java.util.Map<String, String> body,
                                                               HttpServletRequest request) {
        return Result.ok(authService.wechatCallback(body.get("state"), body.get("code"),
                IpUtils.getClientIp(request)));
    }

    @Operation(summary = "微信扫码确认后换取登录令牌")
    @PublicApi
    @PostMapping("/wechat/exchange")
    public Result<LoginVO> wechatExchange(@RequestBody java.util.Map<String, String> body) {
        return Result.ok(authService.wechatExchange(body.get("state")));
    }

    @Operation(summary = "手机端扫码确认页数据（演示模式）")
    @PublicApi
    @GetMapping("/qr-confirm/info")
    public Result<java.util.Map<String, Object>> qrConfirmInfo(@RequestParam String state) {
        return Result.ok(authService.qrConfirmInfo(state));
    }

    @Operation(summary = "手机端点击确认（演示模式，完成扫码登录）")
    @PublicApi
    @PostMapping("/qr-confirm/confirm")
    public Result<java.util.Map<String, Object>> qrConfirm(@RequestBody java.util.Map<String, String> body,
                                                           HttpServletRequest request) {
        return Result.ok(authService.qrConfirm(body.get("state"), IpUtils.getClientIp(request)));
    }
}
