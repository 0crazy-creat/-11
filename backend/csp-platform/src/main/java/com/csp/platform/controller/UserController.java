package com.csp.platform.controller;

import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.platform.dto.ResetPasswordCmd;
import com.csp.platform.dto.UserProfileVO;
import com.csp.platform.dto.UserQuery;
import com.csp.platform.dto.UserSaveCmd;
import com.csp.platform.dto.UserStatusCmd;
import com.csp.platform.service.UserService;
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

import java.util.Set;

/** 用户管理（管理员）。 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户分页查询")
    @RequirePermission("user:list")
    @GetMapping
    public Result<PageResult<UserProfileVO>> page(@Valid UserQuery query) {
        return Result.ok(userService.page(query));
    }

    @Operation(summary = "用户详情")
    @RequirePermission("user:list")
    @GetMapping("/{id}")
    public Result<UserProfileVO> detail(@PathVariable Long id) {
        return Result.ok(userService.detail(id));
    }

    @Operation(summary = "用户角色码")
    @RequirePermission("user:list")
    @GetMapping("/{id}/roles")
    public Result<Set<String>> roles(@PathVariable Long id) {
        return Result.ok(userService.rolesOf(id));
    }

    @Operation(summary = "新增用户")
    @RequirePermission("user:create")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserSaveCmd cmd) {
        return Result.ok(userService.create(cmd));
    }

    @Operation(summary = "编辑用户")
    @RequirePermission("user:update")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserSaveCmd cmd) {
        userService.update(id, cmd);
        return Result.ok();
    }

    @Operation(summary = "重置用户密码")
    @RequirePermission("user:reset-password")
    @PostMapping("/{id}/password/reset")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) ResetPasswordCmd cmd) {
        userService.resetPassword(id, cmd == null ? null : cmd.getNewPassword());
        return Result.ok();
    }

    @Operation(summary = "启用/禁用/锁定用户")
    @RequirePermission("user:update")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody UserStatusCmd cmd) {
        userService.changeStatus(id, cmd.getStatus());
        return Result.ok();
    }

    @Operation(summary = "删除用户（逻辑删除）")
    @RequirePermission("user:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }
}