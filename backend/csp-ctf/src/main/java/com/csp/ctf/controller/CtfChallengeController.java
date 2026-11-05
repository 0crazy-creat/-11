package com.csp.ctf.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.csp.common.api.Result;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.entity.Contest;
import com.csp.ctf.entity.ContestQuestion;
import com.csp.ctf.entity.CtfQuestion;
import com.csp.ctf.judge.QuestionExt;
import com.csp.ctf.mapper.ContestMapper;
import com.csp.ctf.mapper.ContestQuestionMapper;
import com.csp.ctf.mapper.CtfQuestionMapper;
import com.csp.ctf.score.DynamicScorePolicy;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.ContestTeamMember;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.ctf.mapper.ContestTeamMemberMapper;
import com.csp.security.annotation.RequirePermission;
import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 赛事题目管理（题目管理弹窗使用）。
 *
 * <p>前端 ChallengeManage.vue 调用的是
 * {@code /api/ctf/contests/{contestId}/challenges} 这一族接口，
 * 之前后端从未实现，导致请求落到静态资源兜底并抛
 * NoResourceFoundException，被全局异常处理器统一包成
 * "系统繁忙，请稍后重试"。这里补齐。</p>
 *
 * <p>数据模型：统一题库 {@code question} + 赛事组卷 {@code contest_question}。
 * 列表只返回已挂到该赛事的题目；新建题目时先入库再自动挂载到赛事。</p>
 */
@Slf4j
@Tag(name = "CTF 题目管理")
@RestController
@RequestMapping("/api/ctf/contests/{contestId}/challenges")
@RequiredArgsConstructor
public class CtfChallengeController {

    private final ContestMapper contestMapper;
    private final ContestQuestionMapper contestQuestionMapper;
    private final CtfQuestionMapper questionMapper;
    private final ContestTeamMapper contestTeamMapper;
    private final ContestTeamMemberMapper contestTeamMemberMapper;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * 选手端题目列表：仅返回已报名战队可见的题目，且 <b>不返回 flag/answer</b>。
     *
     * <p>此前选手端直接复用管理端 {@code GET /challenges}，导致题目的 flag 字段
     * 被原样下发到浏览器（选手可直接查看源码拿到答案），且未校验报名关系，
     * 未报名赛事的题目也能被任意登录用户读取。此处补齐正确的选手端视图。</p>
     */
    @Operation(summary = "选手端题目列表（隐藏 flag，需已报名）")
    @GetMapping("/play")
    public Result<List<PlayChallengeVO>> playList(@PathVariable Long contestId) {
        requireContest(contestId);
        LoginUser u = UserContext.get();
        if (u == null || u.getUserId() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        // 报名校验：管理/裁判/平台管理员等特权角色无需报名即可查看全部赛事（用于赛前巡检与赛中监考）；
        // 普通选手必须已组队/报名该赛事。
        if (!isPrivileged(u) && !isEnrolled(contestId, u.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "尚未报名该赛事，无法查看题目");
        }

        List<ContestQuestion> links = contestQuestionMapper.selectList(
                Wrappers.<ContestQuestion>lambdaQuery().eq(ContestQuestion::getContestId, contestId));
        if (links.isEmpty()) return Result.ok(new ArrayList<>());

        List<Long> qids = links.stream().map(ContestQuestion::getQuestionId).filter(Objects::nonNull).toList();
        if (qids.isEmpty()) return Result.ok(new ArrayList<>());

        List<CtfQuestion> questions = questionMapper.selectBatchIds(qids);
        // 赛事题目覆盖分：contest_question.score 优先于题库默认分
        java.util.Map<Long, Integer> overrideScore = new java.util.HashMap<>();
        for (ContestQuestion l : links) {
            if (l.getQuestionId() != null && l.getScore() != null && l.getScore() > 0) {
                overrideScore.put(l.getQuestionId(), l.getScore());
            }
        }
        int totalTeams = approvedTeamCount(contestId);

        List<PlayChallengeVO> out = new ArrayList<>(questions.size());
        for (CtfQuestion q : questions) {
            QuestionExt ext = QuestionExt.parse(q.getExt());
            boolean docker = ext.dockerEnabled() && hasImage(q, ext);
            // 附件地址：题目表 attachment 优先，ext.attachmentUrl 兜底（管理端可只填一处）
            String attachment = (q.getAttachment() != null && !q.getAttachment().isBlank())
                    ? q.getAttachment() : ext.getAttachmentUrl();

            int staticScore = overrideScore.getOrDefault(q.getId(),
                    q.getScore() == null || q.getScore() <= 0 ? 100 : q.getScore());
            // 选手端展示「实时题面分」：随解题人数衰减后的当前价值
            int currentScore = DynamicScorePolicy.currentValue(ext.getDynamicScore(),
                    staticScore, solvedTeamCount(contestId, q.getId()), totalTeams);

            out.add(PlayChallengeVO.builder()
                    .id(q.getId())
                    .title(q.getTitle())
                    .category(q.getCategory())
                    .difficulty(q.getDifficulty())
                    .score(currentScore)
                    .description(q.getContent())
                    // 有容器 → 不暴露附件；无容器 → 附件下载链接
                    .attachmentUrl(docker ? null : emptyToNull(attachment))
                    .docker(docker)
                    .judgeMode(ext.mode().name())
                    .containerPort(docker ? (ext.getContainerPort() == null ? 80 : ext.getContainerPort()) : null)
                    .dynamicScore(ext.dynamicScoreEnabled())
                    .bloodBonus(ext.getBloodBonus() == null ? null : java.util.Map.of(
                            "first", ext.getBloodBonus().bonusFor(1),
                            "second", ext.getBloodBonus().bonusFor(2),
                            "third", ext.getBloodBonus().bonusFor(3)))
                    .build());
        }
        out.sort(Comparator.comparing(PlayChallengeVO::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        return Result.ok(out);
    }

    /**
     * 选手端题目摘要：只含题名/分类/难度/分值/解出数，不含 flag 与题面。
     * 用于赛事详情页（赛前预览、赛后回顾）：赛后对所有登录用户开放，赛前/赛中仅报名选手或特权角色可见。
     */
    @Operation(summary = "选手端题目摘要（不含 flag/题面；赛后公开）")
    @GetMapping("/summary")
    public Result<List<ChallengeSummaryVO>> summary(@PathVariable Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) throw new BizException(ResultCode.NOT_FOUND, "赛事不存在");
        LoginUser u = UserContext.get();
        if (u == null || u.getUserId() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        String st = contest.getStatus();
        boolean ended = "FINISHED".equals(st) || "ARCHIVED".equals(st);
        if (!ended && !isPrivileged(u) && !isEnrolled(contestId, u.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN, "尚未报名该赛事，无法查看题目");
        }

        List<ContestQuestion> links = contestQuestionMapper.selectList(
                Wrappers.<ContestQuestion>lambdaQuery()
                        .eq(ContestQuestion::getContestId, contestId)
                        .eq(ContestQuestion::getVisible, 1));
        if (links.isEmpty()) return Result.ok(new ArrayList<>());
        List<Long> qids = links.stream().map(ContestQuestion::getQuestionId).filter(Objects::nonNull).toList();
        if (qids.isEmpty()) return Result.ok(new ArrayList<>());

        java.util.Map<Long, Integer> overrideScore = new java.util.HashMap<>();
        for (ContestQuestion l : links) {
            if (l.getQuestionId() != null && l.getScore() != null && l.getScore() > 0) {
                overrideScore.put(l.getQuestionId(), l.getScore());
            }
        }

        List<ChallengeSummaryVO> out = new ArrayList<>();
        for (CtfQuestion q : questionMapper.selectBatchIds(qids)) {
            out.add(ChallengeSummaryVO.builder()
                    .id(q.getId())
                    .title(q.getTitle())
                    .category(q.getCategory())
                    .difficulty(q.getDifficulty())
                    .score(overrideScore.getOrDefault(q.getId(),
                            q.getScore() == null || q.getScore() <= 0 ? 100 : q.getScore()))
                    .solvedCount(solvedTeamCount(contestId, q.getId()))
                    .build());
        }
        out.sort(Comparator.comparing(ChallengeSummaryVO::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        return Result.ok(out);
    }

    /** 题目摘要视图（详情页展示，不含题面与答案）。 */
    @Data
    @Builder
    public static class ChallengeSummaryVO {
        private Long id;
        private String title;
        private String category;
        private String difficulty;
        private Integer score;
        private Integer solvedCount;
    }

    /** 选手端题目视图：显式不含 flag / answer 字段。 */
    @Data
    @Builder
    public static class PlayChallengeVO {
        private Long id;
        private String title;
        private String category;
        private String difficulty;
        /** 当前题面分（启用动态积分时为衰减后的实时值）。 */
        private Integer score;
        private String description;
        /** 无容器题的附件下载地址；有容器题恒为 null（前端据此不渲染下载链接）。 */
        private String attachmentUrl;
        /** true = 动态容器靶机题，前端渲染「启动容器」；false = 附件题，前端只渲染附件下载。 */
        private boolean docker;
        /** STATIC / DYNAMIC / SCRIPT，供前端提示判题方式。 */
        private String judgeMode;
        /** 容器内端口（仅 docker=true 时有值）。 */
        private Integer containerPort;
        private boolean dynamicScore;
        private java.util.Map<String, Integer> bloodBonus;
    }

    /** 赛事题目列表（题目管理弹窗打开时调用）。 */
    @Operation(summary = "赛事题目列表（管理端，含 flag，仅管理员/裁判）")
    @RequirePermission("question:update")
    @GetMapping
    public Result<List<ChallengeVO>> list(@PathVariable Long contestId) {
        requireContest(contestId);
        // 双保险：即使权限点配置被放宽，也只允许管理/裁判角色拿到含 flag 的视图
        assertCanSeeFlag();

        List<ContestQuestion> links = contestQuestionMapper.selectList(
                Wrappers.<ContestQuestion>lambdaQuery().eq(ContestQuestion::getContestId, contestId));
        if (links.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        List<Long> qids = links.stream().map(ContestQuestion::getQuestionId).filter(Objects::nonNull).toList();
        if (qids.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        List<CtfQuestion> questions = questionMapper.selectBatchIds(qids);
        List<ChallengeVO> result = new ArrayList<>(questions.size());
        for (CtfQuestion q : questions) {
            result.add(toVO(q));
        }
        result.sort(Comparator.comparing(ChallengeVO::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        return Result.ok(result);
    }

    /** 题目详情。 */
    @Operation(summary = "题目详情")
    @RequirePermission("question:view")
    @GetMapping("/{challengeId}")
    public Result<ChallengeVO> detail(@PathVariable Long contestId, @PathVariable Long challengeId) {
        assertCanSeeFlag();
        requireContest(contestId);
        CtfQuestion q = questionMapper.selectById(challengeId);
        if (q == null) {
            throw new BizException(ResultCode.NOT_FOUND, "题目不存在");
        }
        return Result.ok(toVO(q));
    }

    /** 新建题目并挂载到赛事（对齐 GZCTF 的容器化题目创建流程）。 */
    @Operation(summary = "新建题目")
    @RequirePermission("question:create")
    @PostMapping
    public Result<ChallengeVO> create(@PathVariable Long contestId, @RequestBody ChallengeSaveCmd cmd) {
        requireContest(contestId);
        validate(cmd);

        CtfQuestion q = new CtfQuestion();
        q.setBankType("CTF");
        q.setCategory(defaultIfBlank(cmd.getCategory(), "WEB"));
        q.setTitle(cmd.getTitle());
        q.setContent(cmd.getDescription());
        q.setFlag(cmd.getFlag());
        q.setScore(cmd.getScore() == null ? 100 : cmd.getScore());
        q.setDifficulty(defaultIfBlank(cmd.getDifficulty(), "EASY"));
        q.setAttachment(cmd.getAttachmentUrl());

        // 容器配置：镜像名存入 image_name 字段
        String image = cmd.getContainerImage() != null ? cmd.getContainerImage() : cmd.getImageName();
        q.setImageName(image);

        // 构建 ext JSON（包含所有容器配置）
        q.setExt(buildExtJson(cmd));
        q.setStatus(1);
        questionMapper.insert(q);

        // 自动挂载到赛事
        attach(contestId, q.getId());
        log.info("新建容器题目并挂载 contest={} question={} type={} image={}",
                contestId, q.getId(), cmd.getChallengeType(), image);
        return Result.ok(toVO(q));
    }

    /** 更新题目（对齐 GZCTF 的容器配置更新流程）。 */
    @Operation(summary = "更新题目")
    @RequirePermission("question:update")
    @PutMapping("/{challengeId}")
    public Result<ChallengeVO> update(@PathVariable Long contestId,
                                      @PathVariable Long challengeId,
                                      @RequestBody ChallengeSaveCmd cmd) {
        requireContest(contestId);
        CtfQuestion q = questionMapper.selectById(challengeId);
        if (q == null) {
            throw new BizException(ResultCode.NOT_FOUND, "题目不存在");
        }
        validate(cmd);
        if (cmd.getCategory() != null) q.setCategory(cmd.getCategory());
        if (cmd.getTitle() != null) q.setTitle(cmd.getTitle());
        if (cmd.getDescription() != null) q.setContent(cmd.getDescription());
        if (cmd.getFlag() != null) q.setFlag(cmd.getFlag());
        if (cmd.getScore() != null) q.setScore(cmd.getScore());
        if (cmd.getDifficulty() != null) q.setDifficulty(cmd.getDifficulty());
        if (cmd.getAttachmentUrl() != null) q.setAttachment(cmd.getAttachmentUrl());

        // 容器配置更新
        String image = cmd.getContainerImage() != null ? cmd.getContainerImage() : cmd.getImageName();
        if (image != null) q.setImageName(image);

        // 重建 ext JSON
        q.setExt(buildExtJson(cmd));
        questionMapper.updateById(q);

        // 若尚未挂载则补挂
        attach(contestId, challengeId);
        log.info("更新容器题目 contest={} question={} type={} image={}",
                contestId, challengeId, cmd.getChallengeType(), image);
        return Result.ok(toVO(q));
    }

    /** 删除题目（仅从赛事中移除；题库保留由 question 管理接口负责）。 */
    @Operation(summary = "从赛事移除题目")
    @RequirePermission("question:delete")
    @DeleteMapping("/{challengeId}")
    public Result<Void> remove(@PathVariable Long contestId, @PathVariable Long challengeId) {
        requireContest(contestId);
        contestQuestionMapper.delete(Wrappers.<ContestQuestion>lambdaQuery()
                .eq(ContestQuestion::getContestId, contestId)
                .eq(ContestQuestion::getQuestionId, challengeId));
        return Result.ok();
    }

    // ---------------- 内部方法 ----------------

    /**
     * 构建 ext JSON（对齐 GZCTF 的容器配置结构）。
     * 包含：challengeType、containerImage、exposePort、cpuLimit、memoryLimit、
     * storageLimit、networkMode、flagTemplate 等字段。
     */
    private String buildExtJson(ChallengeSaveCmd cmd) {
        com.fasterxml.jackson.databind.node.ObjectNode node;
        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();

        // 如果前端传了完整的 ext，先解析它作为基础
        if (cmd.getExt() != null && !cmd.getExt().isBlank()) {
            try {
                com.fasterxml.jackson.databind.JsonNode parsed = om.readTree(cmd.getExt());
                node = parsed instanceof com.fasterxml.jackson.databind.node.ObjectNode on
                        ? on : om.createObjectNode();
            } catch (Exception e) {
                node = om.createObjectNode();
            }
        } else {
            node = om.createObjectNode();
        }

        // 题目类型
        String challengeType = cmd.getChallengeType();
        if (challengeType == null || challengeType.isBlank()) {
            challengeType = cmd.getContainerImage() != null || cmd.getImageName() != null
                    ? "STATIC_CONTAINER" : "STATIC_ATTACHMENT";
        }
        node.put("challengeType", challengeType);

        // 兼容旧字段
        boolean isContainer = "STATIC_CONTAINER".equals(challengeType) || "DYNAMIC_CONTAINER".equals(challengeType);
        boolean isDynamic = "DYNAMIC_CONTAINER".equals(challengeType);
        node.put("isDocker", isContainer);
        node.put("judgeMode", isDynamic ? "DYNAMIC" : "STATIC");

        // 容器配置
        String image = cmd.getContainerImage() != null ? cmd.getContainerImage() : cmd.getImageName();
        if (image != null && !image.isBlank()) {
            node.put("containerImage", image);
            node.put("imageName", image);
        }
        if (cmd.getExposePort() != null) {
            node.put("exposePort", cmd.getExposePort());
            node.put("containerPort", cmd.getExposePort());
        }
        if (cmd.getCpuLimit() != null) node.put("cpuLimit", cmd.getCpuLimit());
        if (cmd.getMemoryLimit() != null) node.put("memoryLimit", cmd.getMemoryLimit());
        if (cmd.getStorageLimit() != null) node.put("storageLimit", cmd.getStorageLimit());
        if (cmd.getNetworkMode() != null) node.put("networkMode", cmd.getNetworkMode());

        // Flag 模板
        if (cmd.getFlagTemplate() != null) node.put("flagTemplate", cmd.getFlagTemplate());

        // 附件
        if (cmd.getAttachmentUrl() != null && !cmd.getAttachmentUrl().isBlank()) {
            node.put("attachmentUrl", cmd.getAttachmentUrl());
        }

        return node.toString();
    }

    /**
     * 规整 ext JSON（兼容旧数据的简化版本，保留供其他方法使用）。
     * <p>空值或非法 JSON 一律回退为最保守的 STATIC 纯附件题，避免脏数据导致
     * 判题路由异常（{@code QuestionExt.parse} 本身容错，但这里提前兜住）。</p>
     */
    private String normalizeExt(String extJson, String attachmentUrl, String imageName) {
        com.fasterxml.jackson.databind.node.ObjectNode node;
        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            com.fasterxml.jackson.databind.JsonNode parsed =
                    (extJson == null || extJson.isBlank()) ? null : om.readTree(extJson);
            node = (parsed instanceof com.fasterxml.jackson.databind.node.ObjectNode on)
                    ? on : om.createObjectNode();
        } catch (Exception e) {
            node = om.createObjectNode();
        }
        String mode = node.path("judgeMode").asText("");
        if (!"STATIC".equalsIgnoreCase(mode) && !"DYNAMIC".equalsIgnoreCase(mode)
                && !"SCRIPT".equalsIgnoreCase(mode)) {
            node.put("judgeMode", "STATIC");
        } else {
            node.put("judgeMode", mode.toUpperCase());
        }
        if (!node.hasNonNull("imageName") && imageName != null && !imageName.isBlank()) {
            node.put("imageName", imageName);
        }
        if (!node.hasNonNull("attachmentUrl") && attachmentUrl != null && !attachmentUrl.isBlank()) {
            node.put("attachmentUrl", attachmentUrl);
        }
        // 容器题标志归一化：显式 isDocker 优先；否则有镜像名即视为容器靶机题。
        // 历史坑：只填 imageName 而未写 isDocker 时，选手端 dockerEnabled()=false，
        // 容器题被误降级成附件题（无「启动容器」按钮）。这里补全。
        if (!node.hasNonNull("isDocker")) {
            boolean hasImg = node.path("imageName").asText("").isBlank() == false;
            node.put("isDocker", hasImg);
        }
        return node.toString();
    }

    private void attach(Long contestId, Long questionId) {
        Long exists = contestQuestionMapper.selectCount(Wrappers.<ContestQuestion>lambdaQuery()
                .eq(ContestQuestion::getContestId, contestId)
                .eq(ContestQuestion::getQuestionId, questionId));
        if (exists != null && exists > 0) {
            return;
        }
        ContestQuestion link = new ContestQuestion();
        link.setContestId(contestId);
        link.setQuestionId(questionId);
        link.setSortNo(0);
        link.setVisible(1);
        contestQuestionMapper.insert(link);
    }

    private void requireContest(Long contestId) {
        if (contestId == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "缺少赛事ID");
        }
        Contest c = contestMapper.selectById(contestId);
        if (c == null) {
            throw new BizException(ResultCode.NOT_FOUND, "赛事不存在");
        }
    }

    private void validate(ChallengeSaveCmd cmd) {
        if (cmd == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "请求体不能为空");
        }
        if (cmd.getTitle() != null && cmd.getTitle().length() > 255) {
            throw new BizException(ResultCode.BAD_REQUEST, "标题过长");
        }
        if (cmd.getScore() != null && cmd.getScore() < 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "分数不能为负");
        }
    }

    /**
     * 含 flag 的题目视图仅对管理员/裁判开放。
     *
     * <p>此前该接口只要求 {@code question:view}，而 PLAYER 角色恰好拥有该权限点，
     * 导致参赛选手可以直接调用管理端接口拿到明文 flag。这里按角色做二次收敛，
     * 从根上避免"权限点配错即泄露答案"。</p>
     */
    /** 是否已报名该赛事（队长或队员均算）。 */
    private boolean isEnrolled(Long contestId, Long userId) {
        if (userId == null) return false;
        Long captainCount = contestTeamMapper.selectCount(
                Wrappers.<ContestTeam>lambdaQuery()
                        .eq(ContestTeam::getContestId, contestId)
                        .eq(ContestTeam::getCaptainId, userId));
        if (captainCount != null && captainCount > 0) return true;
        Long memberCount = contestTeamMemberMapper.selectCount(
                Wrappers.<ContestTeamMember>lambdaQuery()
                        .eq(ContestTeamMember::getContestId, contestId)
                        .eq(ContestTeamMember::getUserId, userId));
        return memberCount != null && memberCount > 0;
    }

    /** 特权角色（无需报名即可查看赛事/题目）：管理员、平台管理员、裁判、教师、超级管理员。 */
    private boolean isPrivileged(LoginUser u) {
        if (u == null) return false;
        java.util.Set<String> roles = u.getRoles() == null ? java.util.Set.of() : u.getRoles();
        for (String r : roles) {
            if (r == null) continue;
            String x = r.toUpperCase();
            if (x.equals("ADMIN") || x.equals("SUPER_ADMIN") || x.equals("PLATFORM_ADMIN")
                    || x.equals("JUDGE") || x.equals("TEACHER")) {
                return true;
            }
        }
        // 兜底：拥有通配权限（ADMIN 的 "*"）视为特权
        try {
            return u.hasPermission("*");
        } catch (Exception ignore) {
            return false;
        }
    }

    private void assertCanSeeFlag() {
        LoginUser u = UserContext.get();
        if (u == null) throw new BizException(ResultCode.UNAUTHORIZED, "请先登录");
        java.util.Set<String> roles = u.getRoles() == null ? java.util.Set.of() : u.getRoles();
        boolean privileged = roles.stream().anyMatch(r ->
                "ADMIN".equals(r) || "SUPER_ADMIN".equals(r) || "PLATFORM_ADMIN".equals(r) || "JUDGE".equals(r));
        if (!privileged) {
            throw new BizException(ResultCode.FORBIDDEN, "无权限查看题目答案，请使用选手端接口");
        }
    }

    /** 题目是否配置了可用镜像（镜像名来自 ext 或 question.image_name）。 */
    private boolean hasImage(CtfQuestion q, QuestionExt ext) {
        if (ext.getImageName() != null && !ext.getImageName().isBlank()) return true;
        return q.getImageName() != null && !q.getImageName().isBlank();
    }

    /** 该题已解出的队伍数（动态积分衰减输入）。 */
    private int solvedTeamCount(Long contestId, Long questionId) {
        try {
            Long n = jdbcTemplate.query(
                    "SELECT COUNT(DISTINCT team_id) FROM submission "
                            + "WHERE contest_id = ? AND question_id = ? AND correct = 1 AND deleted = 0",
                    rs -> rs.next() ? rs.getLong(1) : null, contestId, questionId);
            return n == null ? 0 : n.intValue();
        } catch (Exception e) {
            // 兼容 submission 表无 deleted 列的历史库
            try {
                Long n = jdbcTemplate.query(
                        "SELECT COUNT(DISTINCT team_id) FROM submission "
                                + "WHERE contest_id = ? AND question_id = ? AND correct = 1",
                        rs -> rs.next() ? rs.getLong(1) : null, contestId, questionId);
                return n == null ? 0 : n.intValue();
            } catch (Exception ignore) {
                return 0;
            }
        }
    }

    /** 赛事审核通过的队伍数（动态积分衰减分母，至少 1）。 */
    private int approvedTeamCount(Long contestId) {
        try {
            Long n = contestTeamMapper.selectCount(
                    Wrappers.<ContestTeam>lambdaQuery()
                            .eq(ContestTeam::getContestId, contestId)
                            .eq(ContestTeam::getStatus, "APPROVED"));
            return n == null || n <= 0 ? 1 : n.intValue();
        } catch (Exception e) {
            return 1;
        }
    }

    private static String emptyToNull(String v) {
        return v == null || v.isBlank() ? null : v;
    }

    private static String defaultIfBlank(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    private static ChallengeVO toVO(CtfQuestion q) {
        ChallengeVO vo = new ChallengeVO();
        vo.setId(q.getId());
        vo.setTitle(q.getTitle());
        vo.setCategory(q.getCategory());
        vo.setDifficulty(q.getDifficulty());
        vo.setScore(q.getScore());
        vo.setFlag(q.getFlag());
        vo.setAttachmentUrl(q.getAttachment());
        vo.setDescription(q.getContent());
        vo.setStatus(q.getStatus());
        vo.setImageName(q.getImageName());
        vo.setExt(q.getExt());

        // 从 ext JSON 解析容器配置
        QuestionExt ext = QuestionExt.parse(q.getExt());
        vo.setChallengeType(ext.getChallengeType() != null ? ext.getChallengeType() : inferChallengeType(q, ext));
        vo.setContainerImage(ext.getEffectiveImage());
        vo.setExposePort(ext.getEffectivePort());
        vo.setCpuLimit(ext.getCpuLimit());
        vo.setMemoryLimit(ext.getMemoryLimit());
        vo.setStorageLimit(ext.getStorageLimit());
        vo.setNetworkMode(ext.getNetworkMode());
        vo.setFlagTemplate(ext.getFlagTemplate());
        return vo;
    }

    /** 从旧数据推断题目类型（兼容历史数据）。 */
    private static String inferChallengeType(CtfQuestion q, QuestionExt ext) {
        if (ext.isContainer() || (q.getImageName() != null && !q.getImageName().isBlank())) {
            return ext.isDynamic() ? "DYNAMIC_CONTAINER" : "STATIC_CONTAINER";
        }
        if (q.getAttachment() != null && !q.getAttachment().isBlank()) {
            return "STATIC_ATTACHMENT";
        }
        return "STATIC_ATTACHMENT";
    }

    /** 题目管理弹窗的展示/提交结构（对齐 GZCTF 设计）。 */
    @Data
    public static class ChallengeVO {
        private Long id;
        private String title;
        private String category;
        private String difficulty;
        private Integer score;
        private String flag;
        private String attachmentUrl;
        private String description;
        private Integer status;

        // ============ GZCTF 风格的题目类型 ============

        /** 题目类型：STATIC_ATTACHMENT / STATIC_CONTAINER / DYNAMIC_CONTAINER */
        private String challengeType;

        // ============ 容器配置 ============

        /** Docker 镜像名 */
        private String containerImage;
        /** 兼容旧字段 */
        private String imageName;
        /** 容器内暴露端口 */
        private Integer exposePort;
        /** CPU 限制 */
        private Double cpuLimit;
        /** 内存限制（MB） */
        private Integer memoryLimit;
        /** 存储限制（MB） */
        private Integer storageLimit;
        /** 网络模式 */
        private String networkMode;

        // ============ Flag 配置 ============

        /** 动态 Flag 模板 */
        private String flagTemplate;

        /** 题目扩展 JSON 原文（完整保留） */
        private String ext;
    }

    /** 新建/更新题目入参（与前端 form 字段一致，对齐 GZCTF 设计）。 */
    @Data
    public static class ChallengeSaveCmd {
        private Long id;
        private String title;
        private String category;
        private String difficulty;
        private Integer score;
        private String flag;
        private String attachmentUrl;
        private String description;

        // ============ GZCTF 风格的题目类型 ============

        /** 题目类型：STATIC_ATTACHMENT / STATIC_CONTAINER / DYNAMIC_CONTAINER */
        private String challengeType;

        // ============ 容器配置 ============

        /** Docker 镜像名 */
        private String containerImage;
        /** 兼容旧字段 */
        private String imageName;

        /** 容器内暴露端口 */
        private Integer exposePort;
        /** CPU 限制（核数） */
        private Double cpuLimit;
        /** 内存限制（MB） */
        private Integer memoryLimit;
        /** 存储限制（MB） */
        private Integer storageLimit;
        /** 网络模式：OPEN / ISOLATED / CUSTOM */
        private String networkMode;

        // ============ Flag 配置 ============

        /** 动态 Flag 模板（DYNAMIC_CONTAINER 使用） */
        private String flagTemplate;

        // ============ 扩展 JSON ============

        /** 题目扩展 JSON（完整覆盖，高级用户使用） */
        private String ext;
    }
}