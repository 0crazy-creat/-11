package com.csp.platform.controller;

import com.csp.common.api.Result;
import com.csp.platform.dto.PermissionAssignCmd;
import com.csp.platform.dto.RoleSaveCmd;
import com.csp.platform.dto.RoleVO;
import com.csp.platform.entity.SysPermission;
import com.csp.platform.service.RoleService;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 角色与权限点管理。 */
@Tag(name = "角色权限")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色列表（含权限点）")
    @RequirePermission("role:list")
    @GetMapping
    public Result<List<RoleVO>> list() {
        return Result.ok(roleService.list());
    }

    @Operation(summary = "角色详情")
    @RequirePermission("role:list")
    @GetMapping("/{id}")
    public Result<RoleVO> detail(@PathVariable Long id) {
        return Result.ok(roleService.detail(id));
    }

    @Operation(summary = "全部权限点（按模块分组）")
    @RequirePermission("role:list")
    @GetMapping("/permissions")
    public Result<Map<String, List<SysPermission>>> permissions() {
        return Result.ok(roleService.permissionTree());
    }

    @Operation(summary = "新增角色")
    @RequirePermission("role:create")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody RoleSaveCmd cmd) {
        return Result.ok(roleService.create(cmd));
    }

    @Operation(summary = "编辑角色")
    @RequirePermission("role:update")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveCmd cmd) {
        roleService.update(id, cmd);
        return Result.ok();
    }

    @Operation(summary = "重置角色的权限点")
    @RequirePermission("role:assign-permission")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody PermissionAssignCmd cmd) {
        roleService.replacePermissions(id, cmd.getPermissions());
        return Result.ok();
    }

    @Operation(summary = "删除角色")
    @RequirePermission("role:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }
}