package com.csp.platform.virt;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SSO / LDAP 集成：
 * - LDAP：真实 JNDI 绑定校验 + 属性拉取（未配置时降级为本地模拟，便于验证流程）
 * - SSO：票据（ticket）校验，CAS/OAuth2 风格的 /sso/login 换取本地会话
 */
@Slf4j
@Service
public class SsoLdapService {

    @Value("${csp.sso.ldap.url:}")
    private String ldapUrl;
    @Value("${csp.sso.ldap.base-dn:}")
    private String baseDn;
    @Value("${csp.sso.ldap.bind-dn-pattern:uid={0},ou=users,dc=example,dc=org}")
    private String bindPattern;
    @Value("${csp.sso.ticket-secret:csp-sso-dev}")
    private String ticketSecret;

    public boolean ldapConfigured() {
        return ldapUrl != null && !ldapUrl.isBlank() && baseDn != null && !baseDn.isBlank();
    }

    /** LDAP 认证：返回用户属性；未配置时走模拟校验（密码 = "ldap123" 通过）。 */
    public Map<String, String> ldapAuthenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户名与密码不能为空");
        }
        if (!ldapConfigured()) {
            log.warn("[LDAP] 未配置 csp.sso.ldap.*，使用模拟校验");
            if (!"ldap123".equals(password)) {
                throw new BizException(ResultCode.UNAUTHORIZED, "LDAP 认证失败（模拟模式密码应为 ldap123）");
            }
            Map<String, String> m = new LinkedHashMap<>();
            m.put("username", username);
            m.put("cn", "LDAP-" + username);
            m.put("mail", username + "@ldap.local");
            m.put("mode", "simulated");
            return m;
        }
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, bindPattern.replace("{0}", username));
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            Map<String, String> m = new LinkedHashMap<>();
            m.put("username", username);
            m.put("mode", "ldap");
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            sc.setReturningAttributes(new String[]{"cn", "mail", "displayName"});
            String filter = bindPattern.replace("{0}", username).replaceAll("^[^=]+=", "(uid=").replaceAll(",.+$", ")");
            if (!filter.startsWith("(")) filter = "(" + filter + ")";
            var r = ctx.search(baseDn, filter, sc);
            if (r.hasMore()) {
                Attributes at = r.next().getAttributes();
                for (String k : new String[]{"cn", "mail", "displayName"}) {
                    var a = at.get(k);
                    if (a != null) m.put(k, String.valueOf(a.get()));
                }
            }
            log.info("[LDAP] 认证成功 user={}", username);
            return m;
        } catch (Exception e) {
            log.warn("[LDAP] 认证失败 user={} err={}", username, e.getMessage());
            throw new BizException(ResultCode.UNAUTHORIZED, "LDAP 认证失败：" + e.getMessage());
        } finally {
            if (ctx != null) try { ctx.close(); } catch (Exception ignore) {}
        }
    }

    /** SSO 票据校验（HMAC 简化实现，可替换为 CAS/OAuth2 回调）。 */
    public Map<String, String> ssoValidate(String ticket, String provider) {
        if (ticket == null || ticket.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "SSO ticket 不能为空");
        }
        // 简化校验：ticket 形如 base64(user:ts:sign)，sign = md5(user:ts:secret)
        try {
            String raw = new String(java.util.Base64.getDecoder().decode(ticket));
            String[] parts = raw.split(":");
            if (parts.length != 3) throw new IllegalArgumentException("格式错误");
            String user = parts[0];
            String ts = parts[1];
            String sign = parts[2];
            String expect = md5(user + ":" + ts + ":" + ticketSecret);
            if (!expect.equalsIgnoreCase(sign)) throw new IllegalArgumentException("签名不匹配");
            long age = Math.abs(System.currentTimeMillis() - Long.parseLong(ts));
            if (age > 5 * 60 * 1000L) throw new IllegalArgumentException("票据已过期");
            Map<String, String> m = new LinkedHashMap<>();
            m.put("username", user);
            m.put("provider", provider == null ? "sso" : provider);
            m.put("mode", "sso");
            log.info("[SSO] 票据校验成功 user={} provider={}", user, provider);
            return m;
        } catch (Exception e) {
            throw new BizException(ResultCode.UNAUTHORIZED, "SSO 票据无效：" + e.getMessage());
        }
    }

    /** 供测试生成合法票据。 */
    public String issueTicket(String username) {
        String ts = String.valueOf(System.currentTimeMillis());
        String sign = md5(username + ":" + ts + ":" + ticketSecret);
        return java.util.Base64.getEncoder().encodeToString((username + ":" + ts + ":" + sign).getBytes());
    }

    private static String md5(String s) {
        try {
            var md = java.security.MessageDigest.getInstance("MD5");
            byte[] d = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
