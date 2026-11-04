package com.csp.awd.controller;

import com.csp.awd.dto.TargetVO;
import com.csp.awd.service.AwdScoreService;
import com.csp.awd.service.AwdTargetService;
import com.csp.common.api.Result;
import com.csp.security.annotation.RequirePermission;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** AWD 靶场编排接口。 */
@Tag(name = "AWD-靶场编排", description = "靶机拉起、重置、销毁、健康巡检")
@RestController
@RequestMapping("/api/awd/targets")
@RequiredArgsConstructor
public class AwdTargetController {

    private final AwdTargetService targetService;
    private final AwdTeamResolver awdTeamResolver;
    private final AwdScoreService scoreService;
    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "靶机列表（按竞赛/战队过滤；非裁判仅可见本队）")
    @RequirePermission(value = {"awd:control", "contest:view"}, logical = RequirePermission.Logical.OR)
    @GetMapping
    public Result<List<TargetVO>> list(@RequestParam Long contestId,
                                       @RequestParam(required = false) Long teamId) {
        Long scope = teamId;
        LoginUser u = UserContext.get();
        boolean control = u != null && (UserContext.isAdmin() || u.hasPermission("awd:control"));
        if (!control) {
            Long selfTeam = awdTeamResolver.teamIdOf(u.getUserId(), contestId);
            if (selfTeam == null) return Result.ok(List.of());
            if (scope != null && !scope.equals(selfTeam)) return Result.ok(List.of());
            scope = selfTeam;
        }
        return Result.ok(targetService.listByContest(contestId, scope));
    }

    @Operation(summary = "我方靶机列表（参赛端；管理员返回该赛事全部靶机）")
    @GetMapping("/mine")
    public Result<List<TargetVO>> mine(@RequestParam Long contestId) {
        LoginUser u = UserContext.require();
        boolean control = UserContext.isAdmin() || u.hasPermission("awd:control") || u.hasPermission("*");
        if (control) {
            // 管理员/裁判无需报名，直接看全赛事靶机
            return Result.ok(targetService.listByContest(contestId, null));
        }
        Long teamId = awdTeamResolver.currentTeamId(contestId);
        if (teamId == null) return Result.ok(List.of());
        return Result.ok(targetService.teamTargets(contestId, teamId));
    }

    @Operation(summary = "参赛端：启动我方靶机（点击即拉起容器，返回访问地址与随机端口）")
    @PostMapping("/mine/start")
    public Result<TargetVO> startMine(@RequestParam Long contestId,
                                      @RequestParam(required = false) Long questionId,
                                      @RequestParam(required = false) Integer roundNo) {
        LoginUser su = UserContext.require();
        boolean suControl = UserContext.isAdmin() || su.hasPermission("awd:control") || su.hasPermission("*");
        Long teamId = awdTeamResolver.currentTeamId(contestId);
        if (teamId == null && !suControl) {
            return Result.fail(com.csp.common.api.ResultCode.FORBIDDEN, "尚未加入任何战队，无法启动靶机");
        }
        if (teamId == null) {
            // 管理员未组队：默认取该赛事第一支战队，便于巡检/演示
            teamId = jdbcTemplate.query("""
                    SELECT team_id FROM contest_team WHERE contest_id = ?
                    ORDER BY id LIMIT 1
                    """, rs -> rs.next() ? rs.getLong(1) : null, contestId);
            if (teamId == null) {
                return Result.fail(com.csp.common.api.ResultCode.NOT_FOUND, "该赛事尚无战队，无法启动靶机");
            }
        }
        // 未指定题目时，取该赛事绑定的第一道 AWD 题
        Long qid = questionId != null ? questionId
                : jdbcTemplate.query("""
                        SELECT cq.question_id FROM contest_question cq
                        JOIN question q ON q.id = cq.question_id
                        WHERE cq.contest_id = ? AND q.bank_type = 'AWD'
                        ORDER BY cq.question_id LIMIT 1
                        """, rs -> rs.next() ? rs.getLong(1) : null, contestId);
        if (qid == null) {
            return Result.fail(com.csp.common.api.ResultCode.NOT_FOUND, "该赛事尚未配置 AWD 题目");
        }
        // 解析当前轮次用于签发动态 flag
        Integer rn = roundNo != null ? roundNo
                : jdbcTemplate.query("""
                        SELECT round_no FROM awd_round WHERE contest_id = ? AND status IN ('RUNNING','PENDING')
                        ORDER BY round_no DESC LIMIT 1
                        """, rs -> rs.next() ? rs.getInt(1) : null, contestId);
        String flag = rn == null ? null : scoreService.issueFlag(contestId, rn, teamId, qid, "awd");
        Map<String, String> env = flag == null ? null : Map.of("FLAG", flag);
        return Result.ok(targetService.provisionOne(contestId, teamId, qid, null, 80, env));
    }

    @Operation(summary = "编排单台靶机（幂等拉起，自动签发并注入 flag）")
    @RequirePermission("awd:control")
    @PostMapping("/provision")
    public Result<TargetVO> provision(@RequestParam Long contestId,
                                      @RequestParam Long teamId,
                                      @RequestParam Long questionId,
                                      @RequestParam(required = false) String image,
                                      @RequestParam(required = false, defaultValue = "80") Integer containerPort,
                                      @RequestParam(required = false) Integer roundNo,
                                      @RequestParam(required = false, defaultValue = "awd") String flagPrefix) {
        String flag = roundNo == null ? null
                : scoreService.issueFlag(contestId, roundNo, teamId, questionId, flagPrefix);
        Map<String, String> env = flag == null ? null : Map.of("FLAG", flag);
        return Result.ok(targetService.provisionOne(contestId, teamId, questionId, image, containerPort, env));
    }

    @Operation(summary = "为整个竞赛自愈式巡检编排")
    @RequirePermission("awd:control")
    @PostMapping("/provision-all")
    public Result<Map<String, Integer>> provisionAll(@RequestParam Long contestId) {
        return Result.ok(Map.of("provisioned", targetService.provisionForContest(contestId)));
    }

    @Operation(summary = "重置靶机（重新签发 flag）")
    @RequirePermission("awd:control")
    @PostMapping("/reset")
    public Result<TargetVO> reset(@RequestParam Long contestId,
                                  @RequestParam Long teamId,
                                  @RequestParam Long questionId,
                                  @RequestParam(required = false) Integer roundNo) {
        String newFlag = roundNo == null ? null
                : scoreService.issueFlag(contestId, roundNo, teamId, questionId, "awd");
        return Result.ok(targetService.reset(contestId, teamId, questionId, newFlag));
    }

    @Operation(summary = "销毁单台靶机")
    @RequirePermission("awd:control")
    @PostMapping("/destroy")
    public Result<Map<String, Boolean>> destroy(@RequestParam Long contestId,
                                                @RequestParam Long teamId,
                                                @RequestParam Long questionId) {
        return Result.ok(Map.of("destroyed", targetService.destroy(contestId, teamId, questionId)));
    }

    @Operation(summary = "销毁整个竞赛靶机（赛事清理）")
    @RequirePermission("awd:control")
    @PostMapping("/destroy-all")
    public Result<Map<String, Integer>> destroyAll(@RequestParam Long contestId) {
        return Result.ok(Map.of("destroyed", targetService.destroyContest(contestId)));
    }

    @Operation(summary = "靶机健康巡检")
    @RequirePermission("awd:control")
    @PostMapping("/health-check")
    public Result<Map<String, Integer>> healthCheck(@RequestParam Long contestId) {
        return Result.ok(Map.of("healthy", targetService.healthCheck(contestId)));
    }
}