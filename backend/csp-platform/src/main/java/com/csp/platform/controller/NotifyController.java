package com.csp.platform.controller;

import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.platform.dto.NotifyQuery;
import com.csp.platform.dto.NotifyReadCmd;
import com.csp.platform.dto.NotifySendCmd;
import com.csp.platform.entity.NotifyMessage;
import com.csp.platform.service.NotifyService;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 通知接口：我的消息 + 管理端发送。 */
@Tag(name = "通知中心")
@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @Operation(summary = "我的通知分页")
    @GetMapping("/my")
    public Result<PageResult<NotifyMessage>> my(@Valid NotifyQuery query) {
        return Result.ok(notifyService.myPage(query));
    }

    @Operation(summary = "我的未读数")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(notifyService.unreadCount());
    }

    @Operation(summary = "通知详情")
    @GetMapping("/inbox")
    public Result<java.util.List<java.util.Map<String, Object>>> inbox(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.ok(java.util.List.of());
    }

    @GetMapping("/{id}")
    public Result<NotifyMessage> get(@PathVariable Long id) {
        return Result.ok(notifyService.get(id));
    }

    @Operation(summary = "批量标记已读")
    @PostMapping("/read")
    public Result<Integer> markRead(@Valid @RequestBody NotifyReadCmd cmd) {
        return Result.ok(notifyService.markRead(cmd.getIds()));
    }

    @Operation(summary = "全部标记已读")
    @PostMapping("/read-all")
    public Result<Integer> markAllRead() {
        return Result.ok(notifyService.markAllRead());
    }

    @Operation(summary = "发送通知（管理员）")
    @RequirePermission("notify:send")
    @PostMapping("/send")
    public Result<NotifyMessage> send(@Valid @RequestBody NotifySendCmd cmd) {
        return Result.ok(notifyService.send(cmd));
    }
}
