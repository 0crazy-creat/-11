package com.csp.awd.round.controller;

import com.csp.awd.round.DisputeService;
import com.csp.awd.round.RoundControlService;
import com.csp.awd.round.RoundInterruptService;
import com.csp.awd.round.dto.ArbitrateRequest;
import com.csp.awd.round.dto.DisputeSubmitRequest;
import com.csp.awd.round.dto.InterruptRequest;
import com.csp.awd.round.entity.AwdRoundDispute;
import com.csp.common.api.Result;
import com.csp.common.exception.BizException;
import com.csp.security.annotation.RequirePermission;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AWD 轮次控制面接口。
 *
 * <p>补齐轮次控制四接口中缺失的 begin / running，并新增 settle / interrupt / dispute / arbitrate。
 * 所有状态迁移一律经 {@code com.csp.awd.round.RoundStateMachine} 校验，
 * Controller 层不写任何状态 if 判断。</p>
 *
 * <p>接口清单（均挂在 /api/awd/rounds/{contestId}/{roundNo} 下，与既有 AwdRoundController 的
 * /start /check /finish /abort 不冲突）：</p>
 * <ul>
 *   <li>POST /begin            PENDING → RUNNING，开启攻击窗口</li>
 *   <li>GET  /running          攻击窗口运行态（剩余秒数、是否冻结、是否接受提交）</li>
 *   <li>POST /settle           RUNNING → SETTLING → FINISHED，执行结算</li>
 *   <li>POST /interrupt        软中断（冻结窗口）/ 硬中断（立即中止并结算）</li>
 *   <li>POST /resume           解除软中断冻结</li>
 *   <li>GET  /control          轮次控制视图（状态、可迁移状态、中断信息）</li>
 *   <li>POST /dispute          选手提交申诉</li>
 *   <li>GET  /dispute          轮次内申诉列表</li>
 *   <li>POST /arbitrate        JUDGE 仲裁（approve 回滚扣分 / reject）</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/awd/rounds/{contestId}/{roundNo}")
@RequiredArgsConstructor
public class AwdRoundControlController {

    private final RoundControlService controlService;
    private final RoundInterruptService interruptService;
    private final DisputeService disputeService;

    // ================================================================== 轮次四接口

    /**
     * begin：PENDING → RUNNING，开启本轮攻击窗口。
     */
    @RequirePermission("awd:control")
    @PostMapping("/begin")
    public Result<Map<String, Object>> begin(@PathVariable Long contestId,
                                             @PathVariable Integer roundNo) {
        LoginUser u = UserContext.require();
        return Result.ok(controlService.begin(contestId, roundNo, u.getUserId(), u.getUsername()));
    }

    /**
     * running：查询攻击窗口运行态（不迁移状态，供前端轮询与提交前预检）。
     */
    @RequirePermission(value = {"awd:control", "contest:view"}, logical = RequirePermission.Logical.OR)
    @GetMapping("/running")
    public Result<Map<String, Object>> running(@PathVariable Long contestId,
                                               @PathVariable Integer roundNo) {
        return Result.ok(controlService.running(contestId, roundNo));
    }

    /**
     * settle：RUNNING → SETTLING → FINISHED，执行结算并推送排行榜。
     */
    @RequirePermission("awd:control")
    @PostMapping("/settle")
    public Result<Map<String, Object>> settle(@PathVariable Long contestId,
                                              @PathVariable Integer roundNo) {
        LoginUser u = UserContext.require();
        return Result.ok(controlService.settle(contestId, roundNo, u.getUserId(), u.getUsername()));
    }

    /**
     * control：轮次控制视图（当前状态、可迁移状态、中断/冻结信息）。
     */
    @RequirePermission(value = {"awd:control", "contest:view"}, logical = RequirePermission.Logical.OR)
    @GetMapping("/control")
    public Result<Map<String, Object>> control(@PathVariable Long contestId,
                                               @PathVariable Integer roundNo) {
        return Result.ok(controlService.view(contestId, roundNo));
    }

    // ================================================================== 中断

    /**
     * interrupt：软/硬中断统一入口。
     *
     * <p>body: {"type":"SOFT"|"HARD","reason":"..."}</p>
     */
    @RequirePermission("awd:control")
    @PostMapping("/interrupt")
    public Result<Map<String, Object>> interrupt(@PathVariable Long contestId,
                                                 @PathVariable Integer roundNo,
                                                 @RequestBody InterruptRequest req) {
        if (req == null) {
            throw BizException.of("请求体不能为空");
        }
        LoginUser u = UserContext.require();
        return Result.ok(interruptService.interrupt(contestId, roundNo, req.getType(), req.getReason(),
                u.getUserId(), u.getUsername()));
    }

    /**
     * resume：解除软中断冻结，恢复攻击窗口。
     */
    @RequirePermission("awd:control")
    @PostMapping("/resume")
    public Result<Map<String, Object>> resume(@PathVariable Long contestId,
                                              @PathVariable Integer roundNo) {
        LoginUser u = UserContext.require();
        return Result.ok(interruptService.resume(contestId, roundNo, u.getUserId(), u.getUsername()));
    }

    /**
     * interrupt-status：中断与冻结状态查询。
     */
    @RequirePermission(value = {"awd:control", "contest:view"}, logical = RequirePermission.Logical.OR)
    @GetMapping("/interrupt-status")
    public Result<Map<String, Object>> interruptStatus(@PathVariable Long contestId,
                                                       @PathVariable Integer roundNo) {
        return Result.ok(interruptService.status(contestId, roundNo));
    }

    // ================================================================== 申诉仲裁

    /**
     * dispute：选手提交申诉。
     *
     * <p>队伍身份从登录用户解析（{@code LoginUser.contestId} 不是队伍ID，
     * 因此要求请求显式带 teamId 或由具备 awd:control 的裁判代提；普通选手走
     * {@code ?teamId=} 参数由前端带入并在服务端做归属校验）。</p>
     */
    // 参赛选手（PLAYER）是 AWD 申诉的合法发起方，故此处不放 @RequirePermission，
    // 由方法体内做「裁判可代提 / 选手只能提自己队」的归属校验。
    @PostMapping("/dispute")
    public Result<AwdRoundDispute> submitDispute(@PathVariable Long contestId,
                                                 @PathVariable Integer roundNo,
                                                 @RequestParam(required = false) Long teamId,
                                                 @RequestBody DisputeSubmitRequest req) {
        if (req == null) {
            throw BizException.of("请求体不能为空");
        }
        LoginUser u = UserContext.require();
        // 普通选手未指定 teamId 时，从本赛事的战队成员关系自动解析所属战队
        if (teamId == null) {
            teamId = disputeService.resolveMyTeamId(contestId, u.getUserId());
        }
        // 裁判可为任意队伍代提；普通选手只能为自己的队伍提交
        if (!UserContext.isAdmin() && !u.hasPermission("awd:control")) {
            if (teamId == null) {
                throw BizException.of("未在本赛事中找到你的战队，无法提交申诉");
            }
            if (!disputeService.isMyTeam(contestId, teamId, u.getUserId())) {
                throw BizException.of("只能为自己的战队提交申诉");
            }
        }
        return Result.ok(disputeService.submit(contestId, roundNo, teamId, req.getQuestionId(),
                req.getDisputeType(), req.getReason(), req.getEvidence(), u.getUserId()));
    }

    /**
     * dispute：轮次内申诉列表。传 teamId 则只查该队。
     */
    @RequirePermission(value = {"awd:control", "contest:view"}, logical = RequirePermission.Logical.OR)
    @GetMapping("/dispute")
    public Result<List<AwdRoundDispute>> listDisputes(@PathVariable Long contestId,
                                                      @PathVariable Integer roundNo,
                                                      @RequestParam(required = false) Long teamId) {
        if (teamId != null) {
            return Result.ok(disputeService.listByTeam(contestId, roundNo, teamId));
        }
        return Result.ok(disputeService.listByRound(contestId, roundNo));
    }

    /**
     * arbitrate：JUDGE 仲裁。
     *
     * <p>body: {"disputeId":123,"approve":true,"note":"..."}</p>
     */
    @RequirePermission("awd:arbitrate")
    @PostMapping("/arbitrate")
    public Result<Map<String, Object>> arbitrate(@PathVariable Long contestId,
                                                 @PathVariable Integer roundNo,
                                                 @RequestBody ArbitrateRequest req) {
        if (req == null || req.getDisputeId() == null) {
            throw BizException.of("disputeId 不能为空");
        }
        if (req.getApprove() == null) {
            throw BizException.of("approve 不能为空");
        }
        LoginUser u = UserContext.require();
        return Result.ok(disputeService.arbitrate(contestId, roundNo, req.getDisputeId(),
                req.getApprove(), req.getNote(), u.getUserId(), u.getUsername()));
    }
}