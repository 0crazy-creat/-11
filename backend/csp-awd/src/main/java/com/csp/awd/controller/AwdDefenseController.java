package com.csp.awd.controller;

import com.csp.awd.defense.dto.DefenseSubmitResult;
import com.csp.awd.defense.entity.AwdDefenseSubmission;
import com.csp.awd.service.AwdScoreService;
import com.csp.awd.service.DefenseSubmissionService;
import com.csp.common.api.Result;
import com.csp.common.api.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * AWDP 防御修补接口 —— 与攻击 flag 提交（{@code /api/awd/score/attack}）<b>完全分离</b>。
 *
 * <p>路径前缀 {@code /api/awd/defense/**}，修补包走独立表 {@code awd_defense_submission}，
 * 不复用攻击提交接口，避免两类语义混用。</p>
 */
@Tag(name = "AWDP-防御修补", description = "修补包上传 → check 自动校验 → 防御分结算")
@RestController
@RequestMapping("/api/awd/defense")
@RequiredArgsConstructor
public class AwdDefenseController {

    private final DefenseSubmissionService defenseService;
    private final AwdScoreService scoreService;
    private final AwdTeamResolver awdTeamResolver;

    @Operation(summary = "上传修补包（zip/patch）并触发 check 自动校验与防御分结算")
    @PostMapping(value = "/submit", consumes = "multipart/form-data")
    public Result<DefenseSubmitResult> submit(
            @RequestParam Long contestId,
            @RequestParam(required = false) Long teamId,
            @RequestParam Long questionId,
            @RequestParam(required = false) Integer roundNo,
            @RequestParam(required = false) String remark,
            @RequestParam("file") MultipartFile file) {
        if (teamId == null) {
            teamId = awdTeamResolver.currentTeamId(contestId);
        }
        if (teamId == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "无法解析当前用户所属战队，请显式传入 teamId");
        }
        AwdDefenseSubmission sub = defenseService.submit(
                contestId, teamId, questionId, roundNo,
                currentUserId(), remark, file);
        return Result.ok(toResult(sub, scoreService.totalScore(contestId, teamId)));
    }

    @Operation(summary = "查询本队在指定赛事的修补提交记录")
    @GetMapping("/list")
    public Result<List<AwdDefenseSubmission>> list(@RequestParam Long contestId,
                                                   @RequestParam(required = false) Long teamId) {
        if (teamId == null) {
            teamId = awdTeamResolver.currentTeamId(contestId);
        }
        if (teamId == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "无法解析当前用户所属战队");
        }
        return Result.ok(defenseService.listByTeam(contestId, teamId));
    }

    @Operation(summary = "查询单条修补提交（含 check/apply 输出）")
    @GetMapping("/{id}")
    public Result<AwdDefenseSubmission> get(@PathVariable Long id) {
        return Result.ok(defenseService.get(id));
    }

    @Operation(summary = "裁判端：查询某赛事全部修补提交")
    @GetMapping("/contest/{contestId}")
    public Result<List<AwdDefenseSubmission>> listByContest(@PathVariable Long contestId,
                                                            @RequestParam(required = false) String status) {
        return Result.ok(defenseService.listByContest(contestId, status));
    }

    @Operation(summary = "裁判端：复核标记“修补后仍被攻破”，防御分归零")
    @PostMapping("/{id}/compromise")
    public Result<AwdDefenseSubmission> markCompromised(@PathVariable Long id) {
        return Result.ok(defenseService.markCompromised(id));
    }

    // ---- 内部：实体 → 对前端友好的结果 VO ----

    private DefenseSubmitResult toResult(AwdDefenseSubmission sub, long totalScore) {
        boolean verified = DefenseSubmissionService.ST_VERIFIED.equals(sub.getStatus());
        String msg;
        switch (sub.getStatus() == null ? "" : sub.getStatus()) {
            case DefenseSubmissionService.ST_VERIFIED:
                msg = "修补成功，靶机已重启并通过健康检查，防御分 +" + sub.getDefenseScore();
                break;
            case DefenseSubmissionService.ST_COMPROMISED:
                msg = "修补已应用，但本轮该题仍被攻破，防御分 0";
                break;
            case DefenseSubmissionService.ST_REJECTED:
                msg = "修补被拒绝：" + sub.getFailReason();
                break;
            case DefenseSubmissionService.ST_FAILED:
                msg = "修补失败（靶机挂掉，扣分）：" + sub.getFailReason();
                break;
            default:
                msg = "修补处理中，状态 " + sub.getStatus();
        }
        return DefenseSubmitResult.builder()
                .submissionId(sub.getId())
                .verified(verified)
                .status(sub.getStatus())
                .message(msg)
                .gainedScore(sub.getDefenseScore())
                .totalScore(totalScore)
                .sha256(sub.getFileSha256())
                .fileSize(sub.getFileSize())
                .packageType(sub.getPackageType())
                .copyOk(sub.getRestartOk() != null)
                .applyOk(sub.getApplyOutput() != null)
                .restartOk(sub.getRestartOk() != null && sub.getRestartOk() == 1)
                .healthOk(sub.getHealthOk() != null && sub.getHealthOk() == 1)
                .checkOk(verified || DefenseSubmissionService.ST_COMPROMISED.equals(sub.getStatus()))
                .checkOutput(sub.getCheckOutput())
                .applyOutput(sub.getApplyOutput())
                .failReasons(sub.getFailReason() == null ? List.of() : List.of(sub.getFailReason()))
                .build();
    }

    private Long currentUserId() {
        var u = com.csp.security.core.UserContext.get();
        return u == null ? null : u.getUserId();
    }
}