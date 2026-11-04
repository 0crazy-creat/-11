package com.csp.platform.controller;

import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.platform.dto.AuditLogQuery;
import com.csp.platform.entity.AuditLog;
import com.csp.platform.service.AuditService;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 审计日志查询：只读，不提供改删。 */
@Tag(name = "审计日志")
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "分页查询审计日志")
    @RequirePermission("audit:view")
    @GetMapping
    public Result<PageResult<AuditLog>> page(@Valid AuditLogQuery query) {
        return Result.ok(auditService.page(query));
    }

    @Operation(summary = "审计日志详情")
    @RequirePermission("audit:view")
    @GetMapping("/{id}")
    public Result<AuditLog> get(@PathVariable Long id) {
        return Result.ok(auditService.get(id));
    }
}
