package com.csp.security.filter;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.infra.abac.service.AbacDecisionEngine;
import com.csp.security.annotation.PublicApi;
import com.csp.security.annotation.RequirePermission;
import com.csp.security.annotation.RequireAttribute;
import com.csp.security.annotation.RequireRole;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** 基于注解的权限拦截器：校验登录态、角色、权限点、ABAC属性。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final AbacDecisionEngine abacEngine;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod hm)) return true;

        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // @PublicApi 放行
        if (AnnotatedElementUtils.hasAnnotation(hm.getMethod(), PublicApi.class)
                || AnnotatedElementUtils.hasAnnotation(hm.getBeanType(), PublicApi.class)) {
            return true;
        }

        LoginUser user = UserContext.get();

        // 方法参数名（供 ABAC 的 ${argName} 解析使用）
        String[] paramNames = new String[0];
        Object[] args = new Object[0];
        try {
            var mp = hm.getMethod().getParameters();
            paramNames = new String[mp.length];
            for (int i = 0; i < mp.length; i++) paramNames[i] = mp[i].getName();
            // HandlerMethod#getMethodArgumentValues 为 protected，这里用反射兜底
            var m = HandlerMethod.class.getDeclaredMethod("getMethodArgumentValues",
                    jakarta.servlet.http.HttpServletRequest.class, org.springframework.web.method.support.ModelAndViewContainer.class);
            m.setAccessible(true);
            args = (Object[]) m.invoke(hm, request, null);
        } catch (Exception ignore) {
            // 取不到参数名时，ABAC 的 ${} 占位退化为"非空即可"
        }

        RequireRole role = AnnotatedElementUtils.findMergedAnnotation(hm.getMethod(), RequireRole.class);
        if (role == null) role = AnnotatedElementUtils.findMergedAnnotation(hm.getBeanType(), RequireRole.class);
        if (role != null) {
            if (user == null) throw BizException.unauthorized("请先登录");
            boolean pass = role.logical() == RequireRole.Logical.AND
                    ? Arrays.stream(role.value()).allMatch(user::hasRole)
                    : Arrays.stream(role.value()).anyMatch(user::hasRole);
            if (!pass) throw new BizException(ResultCode.FORBIDDEN, "当前角色无权访问该接口");
        }

        RequirePermission perm = AnnotatedElementUtils.findMergedAnnotation(hm.getMethod(), RequirePermission.class);
        if (perm == null) perm = AnnotatedElementUtils.findMergedAnnotation(hm.getBeanType(), RequirePermission.class);
        if (perm != null) {
            if (user == null) throw BizException.unauthorized("请先登录");
            boolean pass = perm.logical() == RequirePermission.Logical.AND
                    ? Arrays.stream(perm.value()).allMatch(user::hasPermission)
                    : Arrays.stream(perm.value()).anyMatch(user::hasPermission);
            if (!pass) throw new BizException(ResultCode.FORBIDDEN, "缺少必要权限");
        }

        // ===== ABAC：属性级校验 =====
        RequireAttribute attr = AnnotatedElementUtils.findMergedAnnotation(hm.getMethod(), RequireAttribute.class);
        if (attr == null) attr = AnnotatedElementUtils.findMergedAnnotation(hm.getBeanType(), RequireAttribute.class);
        if (attr != null) {
            if (user == null) throw BizException.unauthorized("请先登录");
            
            // 管理员豁免
            for (String r : attr.bypassRoles()) {
                if (user.hasRole(r)) return true;
            }
            
            // 判断使用哪种模式
            boolean usePolicyMode = !attr.resourceType().isEmpty() && !attr.action().isEmpty();
            
            if (usePolicyMode) {
                // 策略模式：调用 ABAC 决策引擎
                handlePolicyMode(request, hm, user, attr);
            } else {
                // 简单模式：兼容旧逻辑
                handleSimpleMode(request, user, attr, paramNames, args);
            }
        }
        return true;
    }

    /**
     * 简单模式：校验用户属性非空或等于指定值。
     */
    private void handleSimpleMode(HttpServletRequest request, LoginUser user,
                                   RequireAttribute attr, String[] paramNames, Object[] args) {
        String actual = switch (attr.name()) {
            case "orgCode" -> user.getOrgCode();
            case "userId" -> user.getUserId() == null ? null : String.valueOf(user.getUserId());
            case "username" -> user.getUsername();
            default -> null;
        };
        String expect = attr.value();
        if (expect != null && expect.startsWith("${") && expect.endsWith("}")) {
            // ${argName}：从方法参数取期望值
            String pname = expect.substring(2, expect.length() - 1);
            Object v = null;
            // 方式一：编译期保留参数名（-parameters）
            if (paramNames != null && args != null) {
                for (int k = 0; k < paramNames.length && k < args.length; k++) {
                    if (pname.equals(paramNames[k])) { v = args[k]; break; }
                }
            }
            // 方式二：兜底从请求参数取（不依赖 -parameters）
            if (v == null) {
                String q = request.getParameter(pname);
                if (q != null) v = q;
            }
            expect = v == null ? null : String.valueOf(v);
        }
        boolean ok = (expect == null || expect.isEmpty())
                ? (actual != null && !actual.isBlank())
                : expect.equals(actual);
        if (!ok) throw new BizException(ResultCode.FORBIDDEN, attr.message());
    }

    /**
     * 策略模式：调用 ABAC 决策引擎评估。
     */
    private void handlePolicyMode(HttpServletRequest request, HandlerMethod hm,
                                   LoginUser user, RequireAttribute attr) {
        String resourceType = attr.resourceType();
        String action = attr.action();
        
        // 提取资源ID
        String resourceId = extractResourceId(request, hm, attr.resourceIdExpr());
        
        // 构建资源对象（简化版，仅包含ID）
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", resourceId);
        
        // 调用 ABAC 决策引擎
        boolean allowed = abacEngine.check(user, resource, resourceType, resourceId, action);
        
        if (!allowed) {
            log.warn("ABAC策略拒绝访问: user={}, resourceType={}, resourceId={}, action={}",
                    user.getUserId(), resourceType, resourceId, action);
            throw new BizException(ResultCode.FORBIDDEN, attr.message());
        }
    }

    /**
     * 从请求中提取资源ID。
     */
    private String extractResourceId(HttpServletRequest request, HandlerMethod hm, String resourceIdExpr) {
        // 优先从路径变量提取
        try {
            var pathVars = (Map<String, String>) request.getAttribute(
                org.springframework.web.servlet.HandlerMapping.class.getName() + ".pathVariables");
            if (pathVars != null) {
                // 尝试常见的ID参数名
                for (String key : Arrays.asList("id", "contestId", "teamId", "questionId", "targetId")) {
                    if (pathVars.containsKey(key)) {
                        return pathVars.get(key);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("提取路径变量失败: {}", e.getMessage());
        }
        
        // 从请求参数提取
        for (String key : Arrays.asList("id", "contestId", "teamId", "questionId", "targetId")) {
            String value = request.getParameter(key);
            if (value != null) {
                return value;
            }
        }
        
        // TODO: 支持 SpEL 表达式从方法参数提取
        if (resourceIdExpr != null && !resourceIdExpr.isEmpty()) {
            log.warn("SpEL resourceIdExpr 尚未实现，使用默认值");
        }
        
        return "unknown";
    }
}
