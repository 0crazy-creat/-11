package com.csp.ctf.controller;

import com.csp.common.api.Result;
import com.csp.ctf.dto.SubmitRequest;
import com.csp.ctf.dto.SubmitResult;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.Submission;
import com.csp.ctf.service.ContestEnrollService;
import com.csp.ctf.service.SubmissionService;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CTF 答题：提交 flag、查询已解题目与提交记录。 */
@Tag(name = "CTF 答题")
@RestController
@RequestMapping("/api/ctf")
@RequiredArgsConstructor
public class CtfSubmitController {

    private final SubmissionService submissionService;
    private final ContestEnrollService enrollService;

    @Operation(summary = "提交 flag / 理论答案并即时判分")
    @PostMapping("/submissions")
    public Result<SubmitResult> submit(@Valid @RequestBody SubmitRequest req, HttpServletRequest request) {
        Long userId = currentUserId();
        return Result.ok(submissionService.submit(userId, req, request));
    }

    @Operation(summary = "我在赛事内已解出的题目ID")
    @GetMapping("/contests/{contestId}/solved")
    public Result<List<Long>> solved(@PathVariable Long contestId) {
        Long userId = currentUserId();
        ContestTeam team = enrollService.myTeam(userId, contestId);
        if (team == null) return Result.ok(List.of());
        return Result.ok(submissionService.solvedQuestionIds(contestId, team.getId()));
    }

    @Operation(summary = "我在赛事内的近期提交记录")
    @GetMapping("/contests/{contestId}/submissions")
    public Result<List<Submission>> mySubmissions(@PathVariable Long contestId,
                                                  @RequestParam(defaultValue = "50") int limit) {
        Long userId = currentUserId();
        ContestTeam team = enrollService.myTeam(userId, contestId);
        if (team == null) return Result.ok(List.of());
        return Result.ok(submissionService.recentSubmissions(contestId, team.getId(), limit));
    }

    private Long currentUserId() {
        LoginUser u = UserContext.get();
        if (u == null) return null;
        return u.getUserId();
    }
}