package com.csp.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csp.common.api.Result;
import com.csp.common.api.ResultCode;
import com.csp.infra.abac.entity.AbacPolicy;
import com.csp.infra.abac.mapper.AbacPolicyMapper;
import com.csp.infra.abac.service.AbacDecisionEngine;
import com.csp.security.annotation.RequirePermission;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ABAC 策略管理接口。
 */
@Tag(name = "ABAC 策略管理")
@RestController
@RequestMapping("/api/admin/abac")
@RequiredArgsConstructor
public class AbacPolicyController {

    private final AbacPolicyMapper policyMapper;
    private final AbacDecisionEngine abacEngine;

    @Operation(summary = "查询策略列表（分页）")
    @RequirePermission("abac:view")
    @GetMapping("/policies")
    public Result<Page<AbacPolicy>> listPolicies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String action) {
        
        LambdaQueryWrapper<AbacPolicy> wrapper = new LambdaQueryWrapper<>();
        if (resourceType != null && !resourceType.isEmpty()) {
            wrapper.eq(AbacPolicy::getResourceType, resourceType);
        }
        if (action != null && !action.isEmpty()) {
            wrapper.eq(AbacPolicy::getAction, action);
        }
        wrapper.orderByDesc(AbacPolicy::getPriority);
        
        Page<AbacPolicy> result = policyMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.ok(result);
    }

    @Operation(summary = "查询所有启用的策略")
    @RequirePermission("abac:view")
    @GetMapping("/policies/enabled")
    public Result<List<AbacPolicy>> listEnabledPolicies() {
        List<AbacPolicy> policies = policyMapper.selectList(
            new LambdaQueryWrapper<AbacPolicy>()
                .eq(AbacPolicy::getEnabled, 1)
                .orderByDesc(AbacPolicy::getPriority)
        );
        return Result.ok(policies);
    }

    @Operation(summary = "查询策略详情")
    @RequirePermission("abac:view")
    @GetMapping("/policies/{id}")
    public Result<AbacPolicy> getPolicy(@PathVariable Long id) {
        AbacPolicy policy = policyMapper.selectById(id);
        if (policy == null) {
            return Result.fail(ResultCode.NOT_FOUND, "策略不存在");
        }
        return Result.ok(policy);
    }

    @Operation(summary = "创建策略")
    @RequirePermission("abac:create")
    @PostMapping("/policies")
    public Result<AbacPolicy> createPolicy(@RequestBody AbacPolicy policy) {
        LoginUser user = UserContext.get();
        policy.setCreatedBy(user.getUserId());
        policyMapper.insert(policy);
        return Result.ok(policy);
    }

    @Operation(summary = "更新策略")
    @RequirePermission("abac:update")
    @PutMapping("/policies/{id}")
    public Result<Void> updatePolicy(@PathVariable Long id, @RequestBody AbacPolicy policy) {
        policy.setId(id);
        LoginUser user = UserContext.get();
        policy.setUpdatedBy(user.getUserId());
        policyMapper.updateById(policy);
        return Result.ok();
    }

    @Operation(summary = "删除策略")
    @RequirePermission("abac:delete")
    @DeleteMapping("/policies/{id}")
    public Result<Void> deletePolicy(@PathVariable Long id) {
        policyMapper.deleteById(id);
        return Result.ok();
    }

    @Operation(summary = "启用/禁用策略")
    @RequirePermission("abac:update")
    @PutMapping("/policies/{id}/toggle")
    public Result<Void> togglePolicy(@PathVariable Long id, @RequestParam boolean enabled) {
        AbacPolicy policy = policyMapper.selectById(id);
        if (policy == null) {
            return Result.fail(ResultCode.NOT_FOUND, "策略不存在");
        }
        policy.setEnabled(enabled ? 1 : 0);
        policyMapper.updateById(policy);
        return Result.ok();
    }

    @Operation(summary = "测试策略评估")
    @RequirePermission("abac:test")
    @PostMapping("/policies/test")
    public Result<Map<String, Object>> testPolicy(
            @RequestBody Map<String, Object> request) {
        
        LoginUser user = UserContext.get();
        String resourceType = (String) request.get("resourceType");
        String resourceId = (String) request.get("resourceId");
        String action = (String) request.get("action");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resource = (Map<String, Object>) request.get("resource");
        
        boolean allowed = abacEngine.check(user, resource, resourceType, resourceId, action);
        
        return Result.ok(Map.of(
            "allowed", allowed,
            "user", Map.of(
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "roles", user.getRoles(),
                "orgCode", user.getOrgCode()
            ),
            "resource", resource != null ? resource : Map.of(),
            "resourceType", resourceType,
            "action", action
        ));
    }
}
