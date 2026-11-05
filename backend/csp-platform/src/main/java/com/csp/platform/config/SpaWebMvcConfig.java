package com.csp.platform.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 单端口部署：后端托管 Vue SPA（history 模式）。
 * 显式列出前端路由前缀，避免使用 ** 通配导致的 PatternParseException。
 */
@Controller
public class SpaWebMvcConfig {

    @GetMapping({
            "/login",
            "/qr-confirm",
            "/portal", "/portal/**",
            "/console", "/console/**",
            "/play/**",
            "/rank", "/rank/**",
            "/situation", "/situation/**",
            "/team", "/team/**",
            "/team-home", "/team-home/**",
            "/awd", "/awd/**",
            "/awd-screen", "/awd-screen/**",
            "/awdp", "/awdp/**"
    })
    public String spa() {
        return "forward:/index.html";
    }
}
