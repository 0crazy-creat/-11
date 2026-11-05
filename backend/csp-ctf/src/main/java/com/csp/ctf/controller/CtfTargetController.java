package com.csp.ctf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.Result;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.entity.ChallengeContainer;
import com.csp.ctf.entity.ContestQuestion;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.CtfQuestion;
import com.csp.ctf.judge.QuestionExt;
import com.csp.ctf.mapper.ContestQuestionMapper;
import com.csp.ctf.mapper.CtfQuestionMapper;
import com.csp.ctf.service.ContainerLifecycleService;
import com.csp.ctf.service.ContestEnrollService;
import com.csp.ctf.service.ContestGuard;
import com.csp.infra.docker.ContainerStatus;
import com.csp.infra.docker.NetworkMode;
import com.csp.security.annotation.RequirePermission;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * CTF 解题赛动态靶机：启动 / 重置 / 销毁 / 查询。
 *
 * <p>对齐 GZCTF 的容器生命周期模型：所有容器状态持久化到 challenge_container 表，
 * 通过 {@link ContainerLifecycleService} 统一管理，不再使用内存 Map。</p>
 *
 * <p>题目类型分流：只有 question.ext.isDocker=true 才允许启动容器；
 * 附件题调用此接口会被拒绝并提示下载附件。</p>
 */
@Slf4j
@Tag(name = "CTF 动态靶机")
@RestController
@RequestMapping("/api/ctf/contests/{contestId}/targets")
@RequiredArgsConstructor
public class CtfTargetController {

    private final ContestGuard guard;
    private final ContestEnrollService enrollService;
    private final ContestQuestionMapper contestQuestionMapper;
    private final CtfQuestionMapper questionMapper;
    private final ContainerLifecycleService lifecycleService;

    @Value("${csp.docker.public-host:}")
    private String publicHost;

    private final HttpServletRequest httpRequest;

    private String resolveHost() {
        if (publicHost != null && !publicHost.isBlank()) return publicHost;
        String host = httpRequest.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) host = httpRequest.getHeader("Host");
        if (host == null || host.isBlank()) return "127.0.0.1";
        int idx = host.indexOf(':');
        return idx > 0 ? host.substring(0, idx) : host;
    }

    @Operation(summary = "查询本队该题靶机状态（未启动返回 null）")
    @GetMapping
    public Result<TargetVO> status(@PathVariable Long contestId, @RequestParam Long questionId) {
        TeamCtx ctx = resolveTeam(contestId, questionId, false);
        ChallengeContainer container = lifecycleService.getContainer(contestId, ctx.teamId, questionId);
        if (container == null) return Result.ok(null);
        return Result.ok(toVO(container));
    }

    @Operation(summary = "启动靶机（持久化到数据库，注入动态 flag）")
    @PostMapping("/start")
    public Result<TargetVO> start(@PathVariable Long contestId, @RequestParam Long questionId) {
        TeamCtx ctx = resolveTeam(contestId, questionId, true);
        LoginUser u = UserContext.require();

        ChallengeContainer container = lifecycleService.createContainer(
                contestId, ctx.teamId, questionId, u.getUserId(),
                ctx.image, ctx.containerPort, ctx.ext, NetworkMode.ISOLATED);

        log.info("CTF 靶机启动 contest={} team={} q={} container={} simulated={}",
                contestId, ctx.teamId, questionId, container.getContainerId(), container.getSimulated());
        return Result.ok(toVO(container));
    }

    @Operation(summary = "重置靶机（销毁后重建，动态 flag 不变）")
    @PostMapping("/reset")
    public Result<TargetVO> reset(@PathVariable Long contestId, @RequestParam Long questionId) {
        TeamCtx ctx = resolveTeam(contestId, questionId, true);
        LoginUser u = UserContext.require();

        ChallengeContainer container = lifecycleService.resetContainer(
                contestId, ctx.teamId, questionId, u.getUserId(),
                ctx.image, ctx.containerPort, ctx.ext, NetworkMode.ISOLATED);
        return Result.ok(toVO(container));
    }

    @Operation(summary = "销毁靶机")
    @PostMapping("/destroy")
    public Result<Map<String, Boolean>> destroy(@PathVariable Long contestId, @RequestParam Long questionId) {
        TeamCtx ctx = resolveTeam(contestId, questionId, false);
        boolean ok = lifecycleService.destroyContainer(contestId, ctx.teamId, questionId);
        return Result.ok(Map.of("destroyed", ok));
    }

    @Operation(summary = "续期靶机（延长 TTL）")
    @PostMapping("/extend")
    public Result<TargetVO> extend(@PathVariable Long contestId, @RequestParam Long questionId) {
        TeamCtx ctx = resolveTeam(contestId, questionId, false);
        ChallengeContainer container = lifecycleService.extendContainer(contestId, ctx.teamId, questionId);
        return Result.ok(toVO(container));
    }

    @Operation(summary = "裁判/管理员：查询本赛事全部靶机")
    @RequirePermission("question:update")
    @GetMapping("/admin/list")
    public Result<List<TargetVO>> listAll(@PathVariable Long contestId) {
        List<ChallengeContainer> containers = lifecycleService.listByContest(contestId);
        List<TargetVO> vos = containers.stream().map(this::toVO).toList();
        return Result.ok(vos);
    }

    @Operation(summary = "裁判/管理员：强制销毁指定靶机")
    @RequirePermission("question:update")
    @PostMapping("/admin/force-destroy/{containerId}")
    public Result<Map<String, Boolean>> forceDestroy(@PathVariable Long contestId,
                                                      @PathVariable Long containerId) {
        boolean ok = lifecycleService.forceDestroy(containerId);
        return Result.ok(Map.of("destroyed", ok));
    }

    // ---------------- 内部 ----------------

    private TeamCtx resolveTeam(Long contestId, Long questionId, boolean requireDocker) {
        LoginUser u = UserContext.require();
        guard.requireContest(contestId);
        if (questionId == null) throw new BizException(ResultCode.BAD_REQUEST, "题目ID不能为空");

        ContestTeam team = enrollService.myTeam(u.getUserId(), contestId);
        if (team == null) throw BizException.forbidden("未报名该赛事，无法使用靶机");
        if (!"APPROVED".equals(team.getStatus())) {
            throw BizException.forbidden("战队尚未通过审核：" + team.getStatus());
        }

        Long linked = contestQuestionMapper.selectCount(new LambdaQueryWrapper<ContestQuestion>()
                .eq(ContestQuestion::getContestId, contestId)
                .eq(ContestQuestion::getQuestionId, questionId));
        if (linked == null || linked == 0) throw BizException.notFound("赛事题目");

        CtfQuestion q = questionMapper.selectById(questionId);
        if (q == null) throw BizException.notFound("题目");
        QuestionExt ext = QuestionExt.parse(q.getExt());

        String image = ext.getImageName() != null && !ext.getImageName().isBlank()
                ? ext.getImageName() : q.getImageName();

        if (requireDocker) {
            if (!ext.dockerEnabled()) {
                throw new BizException(ResultCode.BAD_REQUEST,
                        "该题为附件题（无容器环境），请直接下载附件，无需启动容器");
            }
            if (image == null || image.isBlank()) {
                throw new BizException(ResultCode.CONFLICT, "题目未配置靶机镜像，请联系管理员");
            }
        }

        TeamCtx ctx = new TeamCtx();
        ctx.teamId = team.getId();
        ctx.question = q;
        ctx.ext = ext;
        ctx.image = image;
        ctx.containerPort = ext.getContainerPort() == null ? 80 : ext.getContainerPort();
        return ctx;
    }

    private TargetVO toVO(ChallengeContainer c) {
        Integer accessPort = c.getPublicPort() != null && c.getPublicPort() > 0
                ? c.getPublicPort() : c.getPort();
        String url = accessPort != null && accessPort > 0
                && !ContainerStatus.DESTROYED.name().equals(c.getStatus())
                && !ContainerStatus.ERROR.name().equals(c.getStatus())
                ? "http://" + resolveHost() + ":" + accessPort : null;
        return TargetVO.builder()
                .questionId(c.getQuestionId())
                .teamId(c.getTeamId())
                .containerId(c.getContainerId())
                .port(accessPort)
                .accessUrl(url)
                .status(c.getStatus())
                .simulated(Boolean.TRUE.equals(c.getSimulated()))
                .startedAt(c.getStartedAt())
                .expectStopAt(c.getExpectStopAt())
                .checkedAt(LocalDateTime.now())
                .build();
    }

    private static class TeamCtx {
        Long teamId;
        CtfQuestion question;
        QuestionExt ext;
        String image;
        int containerPort;
    }

    @Data
    @Builder
    public static class TargetVO {
        private Long questionId;
        private Long teamId;
        private String containerId;
        private Integer port;
        private String accessUrl;
        private String status;
        private boolean simulated;
        private LocalDateTime startedAt;
        private LocalDateTime expectStopAt;
        private LocalDateTime checkedAt;
    }
}
