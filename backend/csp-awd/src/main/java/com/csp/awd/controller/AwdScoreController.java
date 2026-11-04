package com.csp.awd.controller;

import com.csp.awd.dto.AttackResultVO;
import com.csp.awd.dto.AttackSubmitRequest;
import com.csp.awd.dto.DefensePatchRequest;
import com.csp.awd.dto.ScoreVO;
import com.csp.awd.engine.AwdScoringEngine;
import com.csp.awd.service.AwdScoreService;
import com.csp.common.api.Result;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** AWD/AWDP 攻防计分接口。 */
@Tag(name = "AWD-攻防计分", description = "攻击提交、AWDP 修补、积分与排行榜")
@RestController
@RequestMapping("/api/awd/score")
@RequiredArgsConstructor
public class AwdScoreController {

    private final AwdScoreService scoreService;
    private final AwdTeamResolver awdTeamResolver;
    private final AwdScoringEngine engine;

    @Operation(summary = "提交攻击 flag")
    @PostMapping("/attack")
    public Result<AttackResultVO> attack(@Valid @RequestBody AttackSubmitRequest req) {
        Long attacker = req.getAttackerTeamId();
        if (attacker == null) {
            attacker = awdTeamResolver.currentTeamId(req.getContestId());
        }
        if (attacker == null) {
            return Result.fail(com.csp.common.api.ResultCode.BAD_REQUEST, "缺少攻击方战队ID");
        }
        return Result.ok(scoreService.submitAttack(req.getContestId(), attacker,
                req.getVictimTeamId(), req.getQuestionId(), req.getFlag(), null));
    }

    @Operation(summary = "AWDP 提交修补说明（获得防御加分）")
    @PostMapping("/patch")
    public Result<Map<String, Object>> patch(@Valid @RequestBody DefensePatchRequest req) {
        Long teamId = awdTeamResolver.currentTeamId(req.getContestId());
        if (teamId == null) {
            return Result.fail(com.csp.common.api.ResultCode.BAD_REQUEST, "缺少战队ID");
        }
        long bonus = scoreService.submitPatch(req.getContestId(), teamId, req.getRoundNo(),
                req.getQuestionId(), req.getDescription());
        return Result.ok(Map.of("gainedScore", bonus,
                "totalScore", scoreService.totalScore(req.getContestId(), teamId)));
    }

    @Operation(summary = "单队积分明细")
    @GetMapping("/team")
    public Result<ScoreVO> teamScore(@RequestParam Long contestId, @RequestParam Long teamId) {
        return Result.ok(scoreService.teamScore(contestId, teamId));
    }

    @Operation(summary = "排行榜")
    @GetMapping("/leaderboard")
    public Result<List<ScoreVO>> leaderboard(@RequestParam Long contestId,
                                             @RequestParam(defaultValue = "50") int topN) {
        return Result.ok(scoreService.leaderboard(contestId, topN));
    }

    @Operation(summary = "计分规则说明（前端展示）")
    @GetMapping("/rules")
    public Result<Map<String, Object>> rules() {
        var p = engine.policy();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("attackBase", p.getAttackBase());
        m.put("repeatDecay", p.getRepeatDecay());
        m.put("firstBloodBonus", p.getFirstBloodBonus());
        m.put("slaPenaltyPerHit", p.getSlaPenaltyPerHit());
        m.put("slaPenaltyFloor", p.getSlaPenaltyFloor());
        m.put("cleanRoundBonus", p.getDefenseBonusPerCleanRound());
        m.put("patchBonus", p.getPatchBonus());
        m.put("formula", "total = attack + defense + slaPenalty");
        return Result.ok(m);
    }

    @Operation(summary = "登记参赛队（开赛初始化 0 分）")
    @RequirePermission("awd:control")
    @PostMapping("/register")
    public Result<Map<String, Boolean>> register(@RequestParam Long contestId,
                                                 @RequestParam Long teamId) {
        scoreService.registerTeam(contestId, teamId);
        return Result.ok(Map.of("registered", true));
    }

    @Operation(summary = "手动触发轮次结算（裁判）")
    @RequirePermission("awd:control")
    @PostMapping("/settle")
    public Result<Map<String, Boolean>> settle(@RequestParam Long contestId,
                                               @RequestParam Integer roundNo) {
        scoreService.settleRound(contestId, roundNo);
        return Result.ok(Map.of("settled", true));
    }
}