package com.csp.ctf.controller;

import com.csp.common.api.Result;
import com.csp.ctf.service.TheoryService;
import com.csp.ctf.vo.TheoryPaperVO;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 理论赛：组卷下发与交卷判分。 */
@Tag(name = "理论赛")
@RestController
@RequestMapping("/api/ctf/contests/{contestId}/theory")
@RequiredArgsConstructor
public class TheoryController {

    private final TheoryService theoryService;

    @Operation(summary = "获取理论赛试卷（不含答案）")
    @GetMapping("/paper")
    public Result<TheoryPaperVO> paper(@PathVariable Long contestId) {
        return Result.ok(theoryService.paper(contestId, currentUserId()));
    }

    @Operation(summary = "交卷判分")
    @PostMapping("/submit")
    public Result<TheoryService.SubmitSummary> submit(@PathVariable Long contestId,
                                                      @Valid @RequestBody List<TheoryService.TheoryAnswer> answers,
                                                      HttpServletRequest request) {
        return Result.ok(theoryService.grade(contestId, currentUserId(), answers, request));
    }

    private Long currentUserId() {
        LoginUser u = UserContext.get();
        return u == null ? null : u.getUserId();
    }
}