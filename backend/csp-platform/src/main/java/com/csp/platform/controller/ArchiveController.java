package com.csp.platform.controller;

import com.csp.common.api.PageResult;
import com.csp.common.api.Result;
import com.csp.platform.dto.ArchiveCmd;
import com.csp.platform.dto.ArchiveVO;
import com.csp.platform.entity.ContestArchive;
import com.csp.platform.service.ArchiveService;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 赛事备案归档接口。 */
@Tag(name = "备案归档")
@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @Operation(summary = "归档赛事（生成快照并流转 ARCHIVED）")
    @RequirePermission("contest:archive")
    @PostMapping("/contest/{contestId}")
    public Result<ArchiveVO> archive(@PathVariable Long contestId, @Valid @RequestBody ArchiveCmd cmd) {
        return Result.ok(archiveService.archive(contestId, cmd));
    }

    @Operation(summary = "归档记录分页")
    @RequirePermission("contest:archive")
    @GetMapping
    public Result<PageResult<ContestArchive>> page(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "20") long size,
                                                   @RequestParam(required = false) String filingNo,
                                                   @RequestParam(required = false) Long contestId) {
        return Result.ok(archiveService.page(page, size, filingNo, contestId));
    }

    @Operation(summary = "按赛事查询归档")
    @RequirePermission("contest:archive")
    @GetMapping("/contest/{contestId}")
    public Result<ContestArchive> byContest(@PathVariable Long contestId) {
        return Result.ok(archiveService.getByContest(contestId));
    }

    @Operation(summary = "归档详情")
    @RequirePermission("contest:archive")
    @GetMapping("/{id}")
    public Result<ArchiveVO> detail(@PathVariable Long id) {
        return Result.ok(archiveService.detail(id));
    }

    @Operation(summary = "校验归档快照完整性")
    @RequirePermission("contest:archive")
    @GetMapping("/{id}/verify")
    public Result<Boolean> verify(@PathVariable Long id) {
        return Result.ok(archiveService.verify(id));
    }
}
