package com.csp.infra.abac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.infra.abac.entity.AbacAuditLog;
import com.csp.infra.abac.entity.AbacPolicy;
import com.csp.infra.abac.mapper.AbacAuditLogMapper;
import com.csp.infra.abac.mapper.AbacPolicyMapper;
import com.csp.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ABAC 属性访问控制决策引擎。
 * 
 * <p>基于 SpEL 表达式评估策略条件，支持动态属性级访问控制。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AbacDecisionEngine {

    private final AbacPolicyMapper policyMapper;
    private final AbacAuditLogMapper auditLogMapper;
    
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 执行 ABAC 决策。
     *
     * @param user         当前登录用户
     * @param resource     目标资源对象
     * @param resourceType 资源类型
     * @param resourceId   资源ID
     * @param action       操作类型
     * @return true 表示允许访问，false 表示拒绝
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean check(LoginUser user, Object resource, String resourceType, 
                         String resourceId, String action) {
        return check(user, resource, resourceType, resourceId, action, null);
    }

    /**
     * 执行 ABAC 决策（带额外上下文变量）。
     *
     * @param user         当前登录用户
     * @param resource     目标资源对象
     * @param resourceType 资源类型
     * @param resourceId   资源ID
     * @param action       操作类型
     * @param extraVars    额外上下文变量
     * @return true 表示允许访问，false 表示拒绝
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean check(LoginUser user, Object resource, String resourceType,
                         String resourceId, String action, Map<String, Object> extraVars) {
        if (user == null) {
            log.warn("ABAC决策: 用户未登录, resourceType={}, action={}", resourceType, action);
            return false;
        }

        // 1. 加载匹配的策略（按优先级降序）
        List<AbacPolicy> policies = policyMapper.selectList(
            new LambdaQueryWrapper<AbacPolicy>()
                .eq(AbacPolicy::getResourceType, resourceType)
                .eq(AbacPolicy::getAction, action)
                .eq(AbacPolicy::getEnabled, 1)
                .orderByDesc(AbacPolicy::getPriority)
        );

        if (policies.isEmpty()) {
            log.debug("ABAC决策: 无匹配策略, resourceType={}, action={}", resourceType, action);
            // 无策略时默认拒绝
            recordAudit(user.getUserId(), null, resourceType, resourceId, action, 
                       "DENY", "无匹配策略");
            return false;
        }

        // 2. 构建评估上下文
        Map<String, Object> userContext = buildUserContext(user);
        Map<String, Object> resourceContext = buildResourceContext(resource);
        
        EvaluationContext evalContext = new StandardEvaluationContext();
        ((StandardEvaluationContext) evalContext).setVariable("user", userContext);
        ((StandardEvaluationContext) evalContext).setVariable("resource", resourceContext);
        
        if (extraVars != null) {
            extraVars.forEach(((StandardEvaluationContext) evalContext)::setVariable);
        }

        // 3. 依次评估策略
        for (AbacPolicy policy : policies) {
            try {
                Expression expr = parser.parseExpression(policy.getConditionExpr());
                Boolean result = expr.getValue(evalContext, Boolean.class);
                
                if (Boolean.TRUE.equals(result)) {
                    boolean allowed = "ALLOW".equalsIgnoreCase(policy.getEffect());
                    
                    log.info("ABAC决策命中: policyId={}, policyName={}, effect={}, user={}, resource={}",
                            policy.getId(), policy.getPolicyName(), policy.getEffect(),
                            user.getUserId(), resourceId);
                    
                    recordAudit(user.getUserId(), policy.getId(), resourceType, resourceId,
                               action, allowed ? "ALLOW" : "DENY", 
                               "命中策略: " + policy.getPolicyName());
                    
                    return allowed;
                }
            } catch (Exception e) {
                log.error("ABAC策略评估失败: policyId={}, expr={}, error={}",
                         policy.getId(), policy.getConditionExpr(), e.getMessage());
                // 策略评估失败时跳过，继续评估下一条
            }
        }

        // 4. 所有策略均未命中，默认拒绝
        recordAudit(user.getUserId(), null, resourceType, resourceId, action,
                   "DENY", "所有策略均未命中");
        return false;
    }

    /**
     * 构建用户上下文变量。
     */
    private Map<String, Object> buildUserContext(LoginUser user) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("id", user.getUserId());
        ctx.put("username", user.getUsername());
        ctx.put("roles", user.getRoles());
        ctx.put("permissions", user.getPermissions());
        ctx.put("orgCode", user.getOrgCode());
        ctx.put("orgName", user.getOrgName());
        ctx.put("className", user.getClassName());
        // 可扩展更多用户属性
        return ctx;
    }

    /**
     * 构建资源上下文变量。
     */
    private Map<String, Object> buildResourceContext(Object resource) {
        Map<String, Object> ctx = new HashMap<>();
        
        if (resource == null) {
            return ctx;
        }

        // 通过反射提取资源属性
        try {
            var methods = resource.getClass().getMethods();
            for (var method : methods) {
                String name = method.getName();
                if (name.startsWith("get") && name.length() > 3 && method.getParameterCount() == 0) {
                    String propName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                    try {
                        Object value = method.invoke(resource);
                        ctx.put(propName, value);
                    } catch (Exception e) {
                        // 忽略无法访问的属性
                    }
                }
            }
        } catch (Exception e) {
            log.warn("构建资源上下文失败: {}", e.getMessage());
        }

        return ctx;
    }

    /**
     * 记录审计日志。
     */
    private void recordAudit(Long userId, Long policyId, String resourceType,
                            String resourceId, String action, String decision, String reason) {
        try {
            AbacAuditLog log = new AbacAuditLog();
            log.setUserId(userId);
            log.setPolicyId(policyId);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setAction(action);
            log.setDecision(decision);
            log.setReason(reason);
            log.setCreatedAt(LocalDateTime.now());
            auditLogMapper.insert(log);
        } catch (Exception e) {
            log.error("记录ABAC审计日志失败: {}", e.getMessage());
        }
    }

    /**
     * 强制允许访问（用于管理员豁免等场景）。
     */
    public void forceAllow(LoginUser user, String resourceType, String resourceId, 
                          String action, String reason) {
        recordAudit(user.getUserId(), null, resourceType, resourceId, action, "ALLOW", 
                   "强制允许: " + reason);
    }

    /**
     * 强制拒绝访问。
     */
    public void forceDeny(LoginUser user, String resourceType, String resourceId,
                         String action, String reason) {
        recordAudit(user.getUserId(), null, resourceType, resourceId, action, "DENY",
                   "强制拒绝: " + reason);
        throw new BizException(ResultCode.FORBIDDEN, reason);
    }
}
