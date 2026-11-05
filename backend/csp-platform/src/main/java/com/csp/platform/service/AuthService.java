package com.csp.platform.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.dto.LoginCmd;
import com.csp.platform.dto.LoginVO;
import com.csp.platform.dto.UpdateProfileCmd;
import com.csp.platform.dto.UserProfileVO;
import com.csp.platform.entity.SysUser;
import com.csp.platform.mapper.SysPermissionMapper;
import com.csp.platform.mapper.SysUserMapper;
import com.csp.platform.mapper.SysUserRoleMapper;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import com.csp.security.jwt.JwtProperties;
import com.csp.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 认证与账号自助服务：登录、刷新、登出、资料、改密。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final com.csp.infra.redis.RedisService redisService;

    private static final String ADMIN_ROLE = "ADMIN";
    /** 首次登录强制改密的标记键（放在 sys_user.ext 过于复杂，这里用内存态 + 由改密接口清除） */

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysPermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final SmsService smsService;
    private final com.csp.platform.mapper.SysRoleMapper roleMapper;
    private final com.csp.platform.wechat.WechatProperties wechatProps;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    // ---------------- 登录 ----------------

    @Transactional
    public LoginVO login(LoginCmd cmd, String clientIp) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, cmd.getUsername().trim()));
        // 统一错误信息，避免用户名枚举
        if (user == null) {
            log.warn("登录失败：用户不存在 username={} ip={}", cmd.getUsername(), clientIp);
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(cmd.getPassword(), user.getPassword())) {
            log.warn("登录失败：密码错误 userId={} ip={}", user.getId(), clientIp);
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.isLocked()) throw new BizException(ResultCode.FORBIDDEN, "账号已锁定，请联系管理员");
        if (!user.isEnabled()) throw new BizException(ResultCode.FORBIDDEN, "账号已禁用，请联系管理员");

        if (Integer.valueOf(1).equals(user.getMfaEnabled())) {
            if (cmd.getMfaCode() == null || cmd.getMfaCode().isBlank()) {
                throw new BizException(ResultCode.UNAUTHORIZED, "请输入双因素验证码");
            }
            String secret = redisService.get("csp:mfa:" + user.getId());
            if (secret == null) {
                log.warn("用户 {} 已开启 MFA 但未绑定密钥", user.getId());
                throw new BizException(ResultCode.UNAUTHORIZED, "双因素密钥未初始化，请联系管理员");
            }
            if (!secret.startsWith(cmd.getMfaCode().trim())) {
                log.warn("用户 {} 双因素校验失败", user.getId());
                throw new BizException(ResultCode.UNAUTHORIZED, "双因素验证码错误");
            }
        }

        Set<String> roles = loadRoles(user.getId());
        Set<String> perms = loadPermissions(user.getId(), roles);
        LoginUser loginUser = buildLoginUser(user, roles, perms, cmd.getContestId());

        // 记录登录痕迹
        SysUser touch = new SysUser();
        touch.setId(user.getId());
        touch.setLastLoginAt(LocalDateTime.now());
        touch.setLastLoginIp(clientIp);
        userMapper.updateById(touch);

        log.info("登录成功 userId={} username={} ip={}", user.getId(), user.getUsername(), clientIp);
        return assemble(loginUser, user);
    }


    // ---------------- 扩展登录方式 ----------------

    /** 手机号 + 短信验证码登录（无账号则自动注册）。 */
    @Transactional
    public LoginVO loginByPhone(String phone, String smsCode, String clientIp) {
        smsService.verify(phone, smsCode);
        String p = phone.trim();

        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getPhone, p).eq(SysUser::getDeleted, 0).last("LIMIT 1"));
        if (user == null) {
            user = createPhoneUser(p);
        }
        if (user.isLocked()) throw new BizException(ResultCode.FORBIDDEN, "账号已锁定");
        if (!user.isEnabled()) throw new BizException(ResultCode.FORBIDDEN, "账号已禁用");

        Set<String> roles = loadRoles(user.getId());
        Set<String> perms = loadPermissions(user.getId(), roles);
        LoginUser lu = buildLoginUser(user, roles, perms, null);
        SysUser touch = new SysUser();
        touch.setId(user.getId());
        touch.setLastLoginAt(LocalDateTime.now());
        touch.setLastLoginIp(clientIp);
        userMapper.updateById(touch);
        log.info("手机号登录成功 userId={} phone={} ip={}", user.getId(), phone, clientIp);
        return assemble(lu, user);
    }

    /** 第三方登录：provider + thirdToken 换取本地账号（首次自动建号）。 */
    @Transactional
    public LoginVO loginByThird(String provider, String thirdToken, String clientIp) {
        if (provider == null || provider.isBlank()) throw new BizException(ResultCode.BAD_REQUEST, "provider 不能为空");
        if (thirdToken == null || thirdToken.isBlank()) throw new BizException(ResultCode.BAD_REQUEST, "thirdToken 不能为空");
        String prov = provider.trim().toLowerCase();

        // 已绑定过则直接登录
        String bindKey = "csp:oauth-bind:" + prov + ":" + thirdToken;
        String uid = redisService.get(bindKey);
        SysUser user = null;
        if (uid != null) {
            user = userMapper.selectById(Long.valueOf(uid));
        }
        if (user == null) {
            // 首次：用 provider+token 派生稳定用户名，避免重复建号
            String uname = prov + "_" + Integer.toHexString(thirdToken.hashCode());
            user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, uname));
            if (user == null) {
                user = new SysUser();
                user.setUsername(uname);
                user.setNickname(prov + "用户");
                user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                user.setStatus(1);
                userMapper.insert(user);
                log.info("第三方首次登录，自动建号 userId={} provider={}", user.getId(), prov);
            }
            redisService.set(bindKey, String.valueOf(user.getId()), java.time.Duration.ofDays(30));
        }
        if (user.isLocked()) throw new BizException(ResultCode.FORBIDDEN, "账号已锁定");
        if (!user.isEnabled()) throw new BizException(ResultCode.FORBIDDEN, "账号已禁用");

        Set<String> roles = loadRoles(user.getId());
        Set<String> perms = loadPermissions(user.getId(), roles);
        LoginUser lu = buildLoginUser(user, roles, perms, null);
        LoginVO vo = assemble(lu, user);
        // 维护第三方 Token ↔ 本地会话映射
        bindThirdSession(prov, thirdToken, user.getId(),
                vo.getAccessToken() == null ? null : vo.getAccessToken().substring(0, 16));
        log.info("第三方登录成功 provider={} userId={}", prov, user.getId());
        return vo;
    }

    /** 下发短信验证码。真实网关模式返回 null（不回显）；本地模式返回验证码便于自测。 */
    public String issueSmsCode(String phone) {
        return smsService.send(phone);
    }

    /**
     * 手机号首次登录自动建号：
     * <ul>
     *   <li>用户名冲突时追加随机后缀重试，避免 500；</li>
     *   <li>并发下按手机号再查一次，命中则复用；</li>
     *   <li>默认分配 PLAYER 角色，保证登录后路由/菜单有权限。</li>
     * </ul>
     */
    private SysUser createPhoneUser(String phone) {
        SysUser user = new SysUser();
        user.setUsername("u" + phone);
        user.setNickname("手机用户" + phone.substring(Math.max(0, phone.length() - 4)));
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        user.setStatus(1);
        try {
            userMapper.insert(user);
        } catch (org.springframework.dao.DuplicateKeyException dup) {
            // 并发：别的请求刚建好同手机号账号
            SysUser exist = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getPhone, phone).eq(SysUser::getDeleted, 0).last("LIMIT 1"));
            if (exist != null) return exist;
            // 用户名被占用（非手机号用户），换一个不冲突的用户名重试
            SysUser retry = new SysUser();
            retry.setUsername("u" + phone + "_" + Integer.toHexString((int) (System.nanoTime() & 0xffff)));
            retry.setNickname(user.getNickname());
            retry.setPhone(phone);
            retry.setPassword(user.getPassword());
            retry.setStatus(1);
            userMapper.insert(retry);
            user = retry;
        }
        assignDefaultRole(user.getId(), "PLAYER");
        log.info("手机号首次登录，自动注册 userId={} phone={}", user.getId(), phone);
        return user;
    }

    /** 分配默认角色；角色码不存在时仅告警不阻断登录。 */
    private void assignDefaultRole(Long userId, String roleCode) {
        try {
            var role = roleMapper.selectOne(Wrappers.<com.csp.platform.entity.SysRole>lambdaQuery()
                    .eq(com.csp.platform.entity.SysRole::getCode, roleCode)
                    .eq(com.csp.platform.entity.SysRole::getDeleted, 0).last("LIMIT 1"));
            if (role == null) {
                log.warn("默认角色 {} 不存在，跳过分配 userId={}", roleCode, userId);
                return;
            }
            userRoleMapper.insert(new com.csp.platform.entity.SysUserRole(userId, role.getId()));
        } catch (Exception e) {
            log.warn("分配默认角色失败 userId={} role={} err={}", userId, roleCode, e.getMessage());
        }
    }

    // ---------------- 刷新 ----------------

    public LoginVO refresh(String refreshToken) {
        LoginUser parsed = jwtService.parseRefresh(refreshToken);
        if (parsed == null) throw new BizException(ResultCode.UNAUTHORIZED, "刷新令牌无效或已过期");

        // 以库为准重算权限，避免令牌中的旧权限继续生效
        SysUser user = userMapper.selectById(parsed.getUserId());
        if (user == null) throw new BizException(ResultCode.UNAUTHORIZED, "用户不存在");
        if (!user.isEnabled()) throw new BizException(ResultCode.FORBIDDEN, "账号已禁用");

        Set<String> roles = loadRoles(user.getId());
        Set<String> perms = loadPermissions(user.getId(), roles);
        LoginUser fresh = buildLoginUser(user, roles, perms, parsed.getContestId());
        return assemble(fresh, user);
    }

    // ---------------- 登出 ----------------

    /**
     * 无状态 JWT 的登出：客户端丢弃令牌即可。
     * 预留接口以便后续接入 Redis 黑名单（jti + TTL）。
     */
    public void logout() {
        LoginUser u = UserContext.get();
        if (u != null) log.info("用户登出 userId={} username={}", u.getUserId(), u.getUsername());
    }

    // ---------------- 当前用户 ----------------

    public LoginVO currentUser() {
        LoginUser u = UserContext.require();
        SysUser user = userMapper.selectById(u.getUserId());
        if (user == null) throw new BizException(ResultCode.UNAUTHORIZED, "用户不存在");
        Set<String> roles = loadRoles(user.getId());
        Set<String> perms = loadPermissions(user.getId(), roles);
        LoginUser fresh = buildLoginUser(user, roles, perms, u.getContestId());
        // 当前用户接口不回发新令牌，仅回资料与权限
        return LoginVO.builder()
                .mustChangePassword(false)
                .user(toProfile(user))
                .roles(fresh.getRoles())
                .permissions(fresh.getPermissions())
                .build();
    }

    // ---------------- 修改密码 ----------------

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        LoginUser u = UserContext.require();
        SysUser user = userMapper.selectById(u.getUserId());
        if (user == null) throw new BizException(ResultCode.UNAUTHORIZED, "用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BizException.of("原密码不正确");
        }
        if (oldPassword.equals(newPassword)) {
            throw BizException.of("新密码不能与原密码相同");
        }
        validatePasswordStrength(newPassword);

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(newPassword));
        update.setUpdatedBy(u.getUserId());
        userMapper.updateById(update);
        log.info("用户修改密码 userId={}", user.getId());
    }

    /** 密码强度：8-64 位，至少含字母与数字。 */
    public static void validatePasswordStrength(String pwd) {
        if (pwd == null || pwd.length() < 8 || pwd.length() > 64) {
            throw BizException.of("密码长度需为 8-64 位");
        }
        boolean hasLetter = pwd.chars().anyMatch(Character::isLetter);
        boolean hasDigit = pwd.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) throw BizException.of("密码需同时包含字母和数字");
    }

    // ---------------- 更新本人资料 ----------------

    @Transactional
    public void updateProfile(UpdateProfileCmd cmd) {
        LoginUser ctx = UserContext.require();
        SysUser update = new SysUser();
        update.setId(ctx.getUserId());
        if (cmd.getNickname() != null) update.setNickname(cmd.getNickname());
        if (cmd.getRealName() != null) update.setRealName(cmd.getRealName());
        if (cmd.getPhone() != null) update.setPhone(cmd.getPhone());
        if (cmd.getEmail() != null) update.setEmail(cmd.getEmail());
        update.setUpdatedBy(ctx.getUserId());
        userMapper.updateById(update);
        log.info("用户更新资料 userId={}", ctx.getUserId());
    }

    // ---------------- 内部工具 ----------------

    public Set<String> loadRoles(Long userId) {
        List<String> codes = userRoleMapper.selectRoleCodesByUserId(userId);
        return codes == null ? Set.of() : new HashSet<>(codes);
    }

    public Set<String> loadPermissions(Long userId, Set<String> roles) {
        // 管理员直接持通配权限，省去逐点查询
        if (roles != null && roles.contains(ADMIN_ROLE)) return Set.of("*");
        List<String> codes = permissionMapper.selectCodesByUserId(userId);
        return codes == null ? Set.of() : new HashSet<>(codes);
    }

    private LoginUser buildLoginUser(SysUser user, Set<String> roles, Set<String> perms, Long contestId) {
        return LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(roles)
                .permissions(perms)
                .contestId(contestId)
                .orgCode(user.getOrgCode())
                .build();
    }

    private LoginVO assemble(LoginUser loginUser, SysUser user) {
        return LoginVO.builder()
                .accessToken(jwtService.issueAccessToken(loginUser))
                .refreshToken(jwtService.issueRefreshToken(loginUser))
                .expiresIn(jwtProperties.getExpireSeconds())
                .mustChangePassword(false)
                .user(toProfile(user))
                .roles(loginUser.getRoles())
                .permissions(loginUser.getPermissions())
                .build();
    }

    public static UserProfileVO toProfile(SysUser u) {
        return UserProfileVO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nickname(u.getNickname())
                .realName(u.getRealName())
                .studentNo(u.getStudentNo())
                .phone(u.getPhone())
                .email(u.getEmail())
                .avatar(u.getAvatar())
                .orgCode(u.getOrgCode())
                .orgName(u.getOrgName())
                .className(u.getClassName())
                .status(u.getStatus())
                .mfaEnabled(Integer.valueOf(1).equals(u.getMfaEnabled()))
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt())
                .build();
    }

    // ---------------- 账号找回 / 2FA（需求 41 段） ----------------

    /** 邮箱注册（复用注册逻辑）。 */
    @Transactional
    public void register(com.csp.platform.dto.RegisterCmd cmd) {
        SysUser exist = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, cmd.getUsername().trim()));
        if (exist != null) throw new BizException(ResultCode.CONFLICT, "用户名已存在");
        validatePasswordStrength(cmd.getPassword());
        SysUser u = new SysUser();
        u.setUsername(cmd.getUsername().trim());
        u.setNickname(cmd.getNickname() == null ? cmd.getUsername() : cmd.getNickname());
        u.setEmail(cmd.getEmail());
        u.setPhone(cmd.getPhone());
        u.setPassword(passwordEncoder.encode(cmd.getPassword()));
        u.setStatus(1);
        userMapper.insert(u);
        log.info("邮箱注册成功 userId={} email={}", u.getId(), cmd.getEmail());
    }

    /** 通过邮箱重置密码。 */
    @Transactional
    public void resetPasswordByEmail(String email, String newPassword) {
        SysUser u = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getEmail, email));
        if (u == null) throw new BizException(ResultCode.NOT_FOUND, "该邮箱未注册");
        SysUser upd = new SysUser();
        upd.setId(u.getId());
        upd.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(upd);
        log.info("邮箱重置密码成功 userId={}", u.getId());
    }

    /** 开关双因素认证。 */
    @Transactional
    public void setMfaEnabled(Long userId, boolean enabled) {
        SysUser upd = new SysUser();
        upd.setId(userId);
        upd.setMfaEnabled(enabled ? 1 : 0);
        userMapper.updateById(upd);
    }

    // ---------------- 第三方 Token ↔ 本地会话映射（需求 108 段） ----------------

    /** 建立第三方 Token 与本地会话的映射；记录过期时间，支持刷新与登出销毁。 */
    public void bindThirdSession(String provider, String thirdToken, Long userId, String localSessionId) {
        if (provider == null || thirdToken == null || userId == null) return;
        String key = com.csp.infra.redis.RedisKeys.oauthToken(provider, thirdToken);
        Map<String, String> m = new java.util.LinkedHashMap<>();
        m.put("userId", String.valueOf(userId));
        m.put("sessionId", localSessionId == null ? "" : localSessionId);
        m.put("boundAt", String.valueOf(System.currentTimeMillis()));
        redisService.set(key, m, java.time.Duration.ofHours(12));
        // 反向索引：本地会话 → 第三方 token，便于登出销毁
        redisService.set("csp:session-third:" + localSessionId, provider + ":" + thirdToken,
                java.time.Duration.ofHours(12));
        log.info("[OAUTH] 已绑定第三方会话 provider={} userId={}", provider, userId);
    }

    /** 查询第三方 token 对应的本地用户ID；过期返回 null。 */
    public Long resolveThirdSession(String provider, String thirdToken) {
        if (provider == null || thirdToken == null) return null;
        Object v = redisService.get(com.csp.infra.redis.RedisKeys.oauthToken(provider, thirdToken));
        if (v instanceof Map<?, ?> mm) {
            Object uid = mm.get("userId");
            if (uid != null) {
                try { return Long.parseLong(String.valueOf(uid)); }
                catch (NumberFormatException ignore) { return null; }
            }
        }
        return null;
    }

    /** 登出时销毁第三方会话映射。 */
    public void destroyThirdSession(String localSessionId) {
        if (localSessionId == null) return;
        String rev = "csp:session-third:" + localSessionId;
        String v = redisService.get(rev);
        if (v != null) {
            String[] parts = String.valueOf(v).split(":", 2);
            if (parts.length == 2) {
                redisService.delete(com.csp.infra.redis.RedisKeys.oauthToken(parts[0], parts[1]));
            }
            redisService.delete(rev);
            log.info("[OAUTH] 已销毁第三方会话 sessionId={}", localSessionId);
        }
    }

    // ---------------- 微信扫码登录（需求 41 段） ----------------

    /**
     * 生成扫码会话：返回 state + 二维码内容 + 过期时间。
     *
     * <p>接入真实微信开放平台时，qrContent 换成
     * {@code https://open.weixin.qq.com/connect/qrconnect?appid=..&redirect_uri=..&state=..}；
     * 本地/演示模式返回内置 scheme，回调由 /wechat/callback 承接。</p>
     */
    public Map<String, Object> createWechatQrSession() {
        String state = java.util.UUID.randomUUID().toString().replace("-", "");
        String key = "csp:wx:qr:" + state;
        Map<String, String> v = new java.util.LinkedHashMap<>();
        v.put("status", "WAITING");   // WAITING / SCANNED / CONFIRMED / EXPIRED
        int ttlSeconds = wechatProps.getQrExpireSeconds() > 0 ? wechatProps.getQrExpireSeconds() : 300;
        redisService.set(key, v, java.time.Duration.ofSeconds(ttlSeconds));
Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("state", state);
        boolean real = wechatProps.isReal();
        if (real) {
            // 真实微信开放平台：扫码后微信回调 redirect_uri，由 /wechat/callback 承接
            out.put("qrContent", "https://open.weixin.qq.com/connect/qrconnect?appid="
                    + wechatProps.getAppId() + "&scope=snsapi_login&state=" + state
                    + "&redirect_uri=" + java.net.URLEncoder.encode(wechatProps.getRedirectUri(), java.nio.charset.StandardCharsets.UTF_8));
        } else {
            // 演示模式：二维码直接编码平台自己的「扫码确认页」地址。
            // 手机扫码即打开可点击的确认页面，点确认后走 /wechat/callback → /wechat/exchange 完成登录，
            // 不再出现「扫出一串字符就打不开」的情况。
            String base = wechatProps.getRedirectUri();
            if (base.endsWith("/login")) base = base.substring(0, base.length() - "/login".length());
            out.put("qrContent", base + "/qr-confirm?state=" + state);
        }
        out.put("mode", real ? "real" : "simulated");
        out.put("expireSeconds", ttlSeconds);
        out.put("status", "WAITING");
        return out;
    }

    /** 前端轮询扫码状态；已确认时附带 token，前端直接登录。 */
    public Map<String, Object> queryWechatState(String state) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        if (state == null || state.isBlank()) {
            out.put("status", "EXPIRED");
            return out;
        }
        Object raw = redisService.get("csp:wx:qr:" + state);
        if (raw == null) {
            out.put("status", "EXPIRED");
            return out;
        }
        String status = "WAITING";
        if (raw instanceof Map<?, ?> m && m.get("status") != null) status = String.valueOf(m.get("status"));
        out.put("state", state);
        out.put("status", status);
        if ("CONFIRMED".equals(status)) {
            Object uid = ((Map<?, ?>) raw).get("userId");
            if (uid != null) {
                SysUser u = userMapper.selectById(Long.valueOf(String.valueOf(uid)));
                if (u != null) {
                    Set<String> roles = loadRoles(u.getId());
                    Set<String> perms = loadPermissions(u.getId(), roles);
                    LoginVO vo = assemble(buildLoginUser(u, roles, perms, null), u);
                    out.put("login", vo);
                }
            }
        }
        return out;
    }

    /**
     * 微信回调：用 code 换 openid（本地模拟时以 code 作为 openid），
     * 落库绑定/建号，并把扫码会话置为 CONFIRMED。
     */
    public Map<String, Object> wechatCallback(String state, String code, String clientIp) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        if (state == null || state.isBlank()) throw new BizException(ResultCode.BAD_REQUEST, "state 不能为空");
        if (code == null || code.isBlank()) throw new BizException(ResultCode.BAD_REQUEST, "code 不能为空");
        String key = "csp:wx:qr:" + state;
        Object raw = redisService.get(key);
        if (raw == null) throw new BizException(ResultCode.BAD_REQUEST, "二维码已过期，请重新获取");

        String openid = wechatProps.isReal() ? exchangeWechatOpenId(code.trim()) : "wx_" + code.trim();
        SysUser user = resolveOrCreateThirdUser("wechat", openid);
        Map<String, String> v = new java.util.LinkedHashMap<>();
        v.put("status", "CONFIRMED");
        v.put("userId", String.valueOf(user.getId()));
        redisService.set(key, v, java.time.Duration.ofMinutes(5));
        bindThirdSession("wechat", openid, user.getId(), state);

        out.put("state", state);
        out.put("status", "CONFIRMED");
        out.put("openid", openid);
        out.put("userId", String.valueOf(user.getId()));
        log.info("[WECHAT] 扫码确认 state={} openid={} userId={} ip={}", state, openid, user.getId(), clientIp);
        return out;
    }

    /** 扫码确认后，用 state 直接换令牌（等价于一次第三方登录）。 */
    public LoginVO wechatExchange(String state) {
        if (state == null || state.isBlank()) throw new BizException(ResultCode.BAD_REQUEST, "state 不能为空");
        Object raw = redisService.get("csp:wx:qr:" + state);
        if (raw == null) throw new BizException(ResultCode.BAD_REQUEST, "二维码已过期，请重新获取");
        if (!(raw instanceof Map<?, ?> m) || m.get("userId") == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "尚未扫码确认");
        }
        SysUser user = userMapper.selectById(Long.valueOf(String.valueOf(m.get("userId"))));
        if (user == null) throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        if (user.isLocked()) throw new BizException(ResultCode.FORBIDDEN, "账号已锁定");
        if (!user.isEnabled()) throw new BizException(ResultCode.FORBIDDEN, "账号已禁用");
        Set<String> roles = loadRoles(user.getId());
        Set<String> perms = loadPermissions(user.getId(), roles);
        LoginVO vo = assemble(buildLoginUser(user, roles, perms, null), user);
        redisService.delete("csp:wx:qr:" + state);
        log.info("[WECHAT] 扫码换令牌成功 userId={}", user.getId());
        return vo;
    }

    /**
     * 手机端扫码确认页：查询该 state 是否有效及当前状态。
     * <p>演示模式下二维码内容即本平台 /qr-confirm 页面地址，手机扫码打开后调用本接口渲染确认页。</p>
     */
    public Map<String, Object> qrConfirmInfo(String state) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        if (state == null || state.isBlank()) {
            out.put("valid", false);
            out.put("message", "缺少 state 参数");
            return out;
        }
        Object raw = redisService.get("csp:wx:qr:" + state);
        if (raw == null) {
            out.put("valid", false);
            out.put("message", "二维码已过期，请在电脑端刷新后重试");
            return out;
        }
        String status = "WAITING";
        if (raw instanceof Map<?, ?> m && m.get("status") != null) status = String.valueOf(m.get("status"));
        out.put("valid", true);
        out.put("state", state);
        out.put("status", status);
        out.put("confirmed", "CONFIRMED".equals(status));
        return out;
    }

    /**
     * 手机端点击「确认登录」：把扫码会话置为 CONFIRMED 并绑定一个演示账号，
     * 电脑端轮询到 CONFIRMED 后调 /wechat/exchange 即可换取令牌。
     */
    public Map<String, Object> qrConfirm(String state, String clientIp) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        if (state == null || state.isBlank()) throw new BizException(ResultCode.BAD_REQUEST, "state 不能为空");
        String key = "csp:wx:qr:" + state;
        Object raw = redisService.get(key);
        if (raw == null) throw new BizException(ResultCode.BAD_REQUEST, "二维码已过期，请刷新后重试");
        Map<String, Object> v = new java.util.LinkedHashMap<>();
        v.put("status", "CONFIRMED");
        // 演示模式：用二维码登录专用演示账号，若不存在则创建
        SysUser demo = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, "qr_demo").last("LIMIT 1"));
        if (demo == null) {
            demo = new SysUser();
            demo.setUsername("qr_demo");
            demo.setNickname("扫码演示用户");
            demo.setPassword(passwordEncoder.encode("QrDemo@123"));
            // SysUser 以 Integer status 表示账号状态（1=启用/2=锁定），无 locked 字段
            demo.setStatus(1);
            userMapper.insert(demo);
            // 保证扫码演示账号有角色，避免登录后路由/菜单无权限
            assignDefaultRole(demo.getId(), "PLAYER");
        }
        v.put("userId", String.valueOf(demo.getId()));
        redisService.set(key, v, java.time.Duration.ofMinutes(5));
        out.put("status", "CONFIRMED");
        out.put("userId", String.valueOf(demo.getId()));
        out.put("username", demo.getUsername());
        log.info("[WECHAT] 手机端确认扫码登录 state={} userId={} ip={}", state, demo.getId(), clientIp);
        return out;
    }

    /** 按 provider + 外部标识 找到或创建本地用户。 */
    private SysUser resolveOrCreateThirdUser(String provider, String externalId) {
        String bindKey = "csp:oauth-bind:" + provider + ":" + externalId;
        String uid = redisService.get(bindKey);
        if (uid != null) {
            SysUser u = userMapper.selectById(Long.valueOf(uid));
            if (u != null) return u;
        }
        String uname = provider + "_" + Integer.toHexString(externalId.hashCode());
        SysUser u = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, uname));
        if (u == null) {
            u = new SysUser();
            u.setUsername(uname);
            u.setNickname(provider + "用户");
            u.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            u.setStatus(1);
            userMapper.insert(u);
            log.info("[{}] 首次登录自动建号 userId={}", provider, u.getId());
        }
        redisService.set(bindKey, String.valueOf(u.getId()), java.time.Duration.ofDays(30));
        return u;
    }

    private String wechatAppId() {
        return wechatProps.getAppId() == null ? "" : wechatProps.getAppId().trim();
    }

    private String wechatRedirect() {
        String r = wechatProps.getRedirectUri();
        return (r == null || r.isBlank()) ? "http://127.0.0.1:11001/login" : r.trim();
    }

    /**
     * 用微信 OAuth code 换取 openid（真实模式）。
     * 文档：https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html
     */
    private String exchangeWechatOpenId(String code) {
        try {
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                    + java.net.URLEncoder.encode(wechatAppId(), java.nio.charset.StandardCharsets.UTF_8)
                    + "&secret=" + java.net.URLEncoder.encode(wechatProps.getAppSecret(), java.nio.charset.StandardCharsets.UTF_8)
                    + "&code=" + java.net.URLEncoder.encode(code, java.nio.charset.StandardCharsets.UTF_8)
                    + "&grant_type=authorization_code";
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5)).build();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url)).timeout(java.time.Duration.ofSeconds(8)).GET().build();
            java.net.http.HttpResponse<String> resp = client.send(req,
                    java.net.http.HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new IllegalStateException("微信接口返回 " + resp.statusCode());
            }
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(resp.body());
            if (node.hasNonNull("errcode") && node.get("errcode").asInt() != 0) {
                throw new IllegalStateException("微信错误 " + node.get("errcode").asInt()
                        + ": " + node.path("errmsg").asText());
            }
            String openid = node.path("openid").asText("");
            if (openid.isBlank()) throw new IllegalStateException("微信未返回 openid");
            return openid;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("[WECHAT] code 换 openid 失败：{}", e.getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "微信授权失败，请稍后重试");
        }
    }
}
