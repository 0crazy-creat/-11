package com.csp.common.util;

import jakarta.servlet.http.HttpServletRequest;

/** 客户端 IP 提取：兼容反向代理场景。 */
public final class IpUtils {
    private static final String[] HEADERS = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP"
    };

    private IpUtils() {}

    public static String getClientIp(HttpServletRequest request) {
        for (String h : HEADERS) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                int idx = ip.indexOf(',');
                return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
            }
        }
        String remote = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(remote) ? "127.0.0.1" : remote;
    }
}
