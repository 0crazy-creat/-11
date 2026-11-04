package com.csp.ctf.scenario.controller;

import com.csp.common.api.Result;
import com.csp.ctf.scenario.dto.ScenarioArenaVO;
import com.csp.ctf.scenario.dto.ScenarioSubmitCmd;
import com.csp.ctf.scenario.dto.ScenarioSubmitResult;
import com.csp.ctf.scenario.entity.ScenarioEnv;
import com.csp.ctf.scenario.service.ScenarioEnvService;
import com.csp.ctf.scenario.service.ScenarioService;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 场景实操赛（SCENARIO）选手端接口。
 *
 * <p>场景树结构：场景（Scenario）→ 目标（Objective）→ 任务（Task）→ 关键证据（Evidence Flag）。
 * 任务之间通过 {@code question.ext.preQuestionId} 表达前置依赖，未完成前置任务时拒绝提交。</p>
 *
 * <p>特权角色（管理员/裁判/教师）免报名可直接查看场景树，便于赛前核验与赛中裁决。</p>
 */
@Tag(name = "场景实操赛")
@RestController
@RequestMapping("/api/scenario")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;
    private final ScenarioEnvService envService;

    /** 管理员/裁判等特权角色编码，免报名即可查看。 */
    private static final Set<String> PRIVILEGED_ROLES = Set.of(
            "ADMIN", "SUPER_ADMIN", "PLATFORM_ADMIN", "JUDGE", "TEACHER");

    @Operation(summary = "场景树视图（含任务完成进度、前置依赖、锁定态）")
    @GetMapping("/contests/{contestId}/scenarios")
    public Result<ScenarioArenaVO> arena(@PathVariable Long contestId) {
        LoginUser u = UserContext.get();
        Long userId = u == null ? null : u.getUserId();
        return Result.ok(scenarioService.arena(contestId, userId, isPrivileged(u)));
    }

    @Operation(summary = "提交关键证据 Flag（校验前置任务 → 匹配 → 计分）")
    @PostMapping("/submissions")
    public Result<ScenarioSubmitResult> submit(@Valid @RequestBody ScenarioSubmitCmd cmd) {
        LoginUser u = UserContext.get();
        Long userId = u == null ? null : u.getUserId();
        return Result.ok(scenarioService.submit(userId, cmd));
    }

    // ================================================================== 多机靶场环境

    @Operation(summary = "开赛前一键预检（镜像存在性/端口冲突/依赖环/模板）")
    @GetMapping("/env/precheck")
    public Result<ScenarioEnvService.PrecheckReport> precheck(@RequestParam Long contestId) {
        return Result.ok(envService.precheckContest(contestId));
    }

    @Operation(summary = "单场景预检")
    @GetMapping("/env/precheck/{scenarioId}")
    public Result<ScenarioEnvService.PrecheckReport> precheckOne(@PathVariable Long scenarioId,
                                                                @RequestParam Long contestId) {
        return Result.ok(envService.precheckScenario(contestId, scenarioId));
    }

    @Operation(summary = "拉起某队某场景的多机环境（外网 Web + 内网 DB + 跳板机）")
    @PostMapping("/env/provision")
    public Result<ScenarioEnv> provision(@RequestBody Map<String, Object> body) {
        Long contestId = asLong(body.get("contestId"));
        Long scenarioId = asLong(body.get("scenarioId"));
        Long teamId = asLong(body.get("teamId"));
        return Result.ok(envService.provision(contestId, teamId, scenarioId));
    }

    @Operation(summary = "销毁场景多机环境")
    @PostMapping("/env/destroy")
    public Result<Boolean> destroy(@RequestParam Long contestId,
                                   @RequestParam Long teamId,
                                   @RequestParam Long scenarioId) {
        return Result.ok(envService.destroy(contestId, teamId, scenarioId));
    }

    @Operation(summary = "查询某队某场景环境实例")
    @GetMapping("/env")
    public Result<ScenarioEnv> getEnv(@RequestParam Long contestId,
                                      @RequestParam Long teamId,
                                      @RequestParam Long scenarioId) {
        return Result.ok(envService.findEnv(contestId, teamId, scenarioId));
    }

    @Operation(summary = "某队在某赛事下的全部场景环境")
    @GetMapping("/env/list")
    public Result<List<ScenarioEnv>> listEnvs(@RequestParam Long contestId,
                                              @RequestParam(required = false) Long teamId) {
        return Result.ok(envService.listEnvs(contestId, teamId));
    }

    @Operation(summary = "场景任务依赖树（前置关系/拓扑序/环检测）")
    @GetMapping("/env/dependency/tree")
    public Result<Map<String, Object>> dependencyTree(@RequestParam Long contestId,
                                                      @RequestParam Long scenarioId,
                                                      @RequestParam(required = false) Long teamId) {
        return Result.ok(envService.dependencyTree(contestId, scenarioId, teamId));
    }

    @Operation(summary = "任务前置依赖校验（提交前预问）")
    @GetMapping("/env/dependency/check")
    public Result<Map<String, Object>> checkDependency(@RequestParam Long contestId,
                                                       @RequestParam Long teamId,
                                                       @RequestParam Long taskId) {
        return Result.ok(envService.checkDependency(contestId, teamId, taskId));
    }

    private static Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        return Long.valueOf(o.toString());
    }

    /** 与 CtfChallengeController#isPrivileged 保持一致的特权判定。 */
    private boolean isPrivileged(LoginUser u) {
        if (u == null) return false;
        if (u.hasPermission("*")) return true;
        if (u.getRoles() == null) return false;
        return u.getRoles().stream().anyMatch(PRIVILEGED_ROLES::contains);
    }
}