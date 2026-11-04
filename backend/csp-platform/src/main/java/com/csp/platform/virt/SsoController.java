package com.csp.platform.virt;

import com.csp.common.api.Result;
import com.csp.security.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** SSO / LDAP 认证接口。 */
@Tag(name = "SSO 与 LDAP")
@RestController
@RequestMapping("/api/sso")
@RequiredArgsConstructor
public class SsoController {

    private final SsoLdapService ssoLdap;

    @Operation(summary = "LDAP 认证")
    @PublicApi
    @PostMapping("/ldap/login")
    public Result<Map<String, String>> ldapLogin(@RequestParam String username,
                                                 @RequestParam String password) {
        return Result.ok(ssoLdap.ldapAuthenticate(username, password));
    }

    @Operation(summary = "LDAP 是否已配置")
    @PublicApi
    @GetMapping("/ldap/status")
    public Result<Map<String, Object>> ldapStatus() {
        return Result.ok(Map.of("configured", ssoLdap.ldapConfigured()));
    }

    @Operation(summary = "SSO 票据校验")
    @PublicApi
    @PostMapping("/ticket/validate")
    public Result<Map<String, String>> validate(@RequestParam String ticket,
                                                @RequestParam(required = false) String provider) {
        return Result.ok(ssoLdap.ssoValidate(ticket, provider));
    }

    @Operation(summary = "生成测试用 SSO 票据")
    @PublicApi
    @GetMapping("/ticket/issue")
    public Result<Map<String, String>> issue(@RequestParam String username) {
        String t = ssoLdap.issueTicket(username);
        return Result.ok(Map.of("ticket", t, "username", username));
    }
}
