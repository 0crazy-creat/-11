package com.csp.cfs.controller;

import com.csp.cfs.dto.CfsSubmitCmd;
import com.csp.cfs.dto.CfsSubmitResult;
import com.csp.cfs.dto.CfsTopologyVO;
import com.csp.cfs.entity.CfsInstance;
import com.csp.cfs.service.CfsService;
import com.csp.common.api.Result;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** CFS 选手端：拓扑视图、按层递进提交 flag、解锁进度。 */
@Tag(name = "CFS 场景化渗透赛")
@RestController
@RequestMapping("/api/cfs")
@RequiredArgsConstructor
public class CfsController {

    private final CfsService cfsService;

    @Operation(summary = "我的分层拓扑视图（含已解锁/未解锁层）")
    @GetMapping("/contests/{contestId}/topology")
    public Result<CfsTopologyVO> topology(@PathVariable Long contestId) {
        return Result.ok(cfsService.topologyView(contestId, currentUserId()));
    }

    @Operation(summary = "按层递进提交 flag（外层未破则内层拒绝）")
    @PostMapping("/submissions")
    public Result<CfsSubmitResult> submit(@Valid @RequestBody CfsSubmitCmd cmd) {
        return Result.ok(cfsService.submit(currentUserId(), cmd));
    }

    @Operation(summary = "我的解锁进度")
    @GetMapping("/contests/{contestId}/instance")
    public Result<CfsInstance> myInstance(@PathVariable Long contestId) {
        return Result.ok(cfsService.myInstance(contestId, currentUserId()));
    }

    private Long currentUserId() {
        LoginUser u = UserContext.get();
        return u == null ? null : u.getUserId();
    }
}