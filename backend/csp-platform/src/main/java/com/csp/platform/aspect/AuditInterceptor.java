package com.csp.platform.aspect;

import com.csp.platform.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 审计拦截器：仅对标注 @Audited 的控制器方法生效，记录操作类型、资源、耗时与结果。
 * 使用 MVC 拦截器而非 AOP，避免额外依赖，同时能拿到真实 HTTP 请求上下文。
 */
@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {

    private static final String ATTR_START = "__audit_start";
    private static final String ATTR_ACTION = "__audit_action";
    private static final String ATTR_RESOURCE = "__audit_resource";

    private final AuditService auditService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod hm)) return true;
        Audited audited = hm.getMethodAnnotation(Audited.class);
        if (audited == null) return true;
        request.setAttribute(ATTR_START, System.currentTimeMillis());
        request.setAttribute(ATTR_ACTION, audited.action());
        request.setAttribute(ATTR_RESOURCE, resolveResource(audited.resource(), request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Object action = request.getAttribute(ATTR_ACTION);
        if (action == null) return;
        Object startObj = request.getAttribute(ATTR_START);
        long cost = startObj instanceof Long s ? System.currentTimeMillis() - s : 0L;
        String resource = (String) request.getAttribute(ATTR_RESOURCE);
        boolean ok = ex == null && response.getStatus() < 400;
        if (ok) {
            auditService.record((String) action, resource, null, "SUCCESS", cost);
        } else {
            String msg = ex != null ? ex.getMessage() : ("HTTP " + response.getStatus());
            if (msg != null && msg.length() > 500) msg = msg.substring(0, 500);
            auditService.record((String) action, resource, msg, "FAIL", cost);
        }
    }

    /** 资源模板中的 {id} 替换为路径变量；缺省回落到请求路径。 */
    private String resolveResource(String template, HttpServletRequest request) {
        String path = request.getRequestURI();
        if (template == null || template.isBlank()) return path;
        String result = template;
        String uri = request.getRequestURI();
        // 简化处理：若模板含 {id}，取路径末段数字作为 id
        if (result.contains("{id}")) {
            String[] segs = uri.split("/");
            String last = segs.length > 0 ? segs[segs.length - 1] : "";
            if (last.matches("\\d+")) result = result.replace("{id}", last);
            else result = result.replace("{id}", "");
        }
        return result;
    }
}
