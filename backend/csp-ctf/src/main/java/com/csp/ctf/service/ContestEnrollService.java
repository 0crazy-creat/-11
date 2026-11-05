package com.csp.ctf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.entity.Contest;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.ContestTeamMember;
import com.csp.ctf.entity.Team;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.ctf.mapper.ContestTeamMemberMapper;
import com.csp.ctf.mapper.TeamMapper;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 参赛报名。
 *
 * <p>战队本身在全局战队模块维护，这里只负责把「个人」或「已有战队」绑定到某个赛事，
 * 生成 contest_team 参赛记录并镜像成员，供计分、容器、靶机等下游逻辑复用。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContestEnrollService {

    private final ContestGuard guard;
    private final ContestTeamMapper teamMapper;
    private final ContestTeamMemberMapper memberMapper;
    private final TeamMapper globalTeamMapper;
    private final GlobalTeamService globalTeamService;
    private final WsPublisher wsPublisher;
    private final RedisService redisService;

    /** 报名页所需信息：赛事模式、当前参赛状态、可用于报名的全局战队。 */
    public Map<String, Object> options(Long userId, Long contestId) {
        Contest contest = guard.requireContest(contestId);
        ContestTeam current = myTeam(userId, contestId);
        List<Map<String, Object>> myTeams = globalTeamService.myTeams(userId).stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("name", t.getName());
                    m.put("logo", t.getLogo());
                    m.put("members", t.getMembers());
                    m.put("maxMembers", t.getMaxMembers());
                    m.put("myRole", t.getMyRole());
                    m.put("captain", java.util.Objects.equals(t.getCaptainId(), userId));
                    return m;
                })
                .filter(m -> Boolean.TRUE.equals(m.get("captain")))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contestId", contestId);
        result.put("mode", contest.getMode() == null ? "TEAM" : contest.getMode());
        result.put("enrolled", current != null);
        result.put("enrollTeamId", current == null ? null : current.getTeamId());
        result.put("enrollName", current == null ? null : current.getName());
        result.put("enrollStatus", current == null ? null : current.getStatus());
        result.put("teams", myTeams);
        return result;
    }

    /** 个人参赛报名。 */
    @Transactional(rollbackFor = Exception.class)
    public ContestTeam enrollIndividual(Long userId, Long contestId, String username) {
        Contest contest = guard.requireSignupOpen(contestId);
        if ("TEAM".equals(contest.getMode())) {
            throw new BizException(ResultCode.CONFLICT, "该赛事为团队赛，请选择战队报名");
        }
        ContestTeam existing = myTeam(userId, contestId);
        if (existing != null) {
            assertIndividual(existing);
            return existing;
        }
        assertCapacity(contest);

        ContestTeam team = new ContestTeam();
        team.setContestId(contestId);
        team.setName("选手_" + username);
        team.setCaptainId(userId);
        team.setMembers(1);
        team.setStatus("APPROVED");
        team.setScore(0L);
        team.setMaxMembers(1);
        teamMapper.insert(team);

        joinMember(contestId, team.getId(), userId, "CAPTAIN");
        registerScoring(contest, team);
        log.info("个人报名: 用户 {} 赛事 {} 参赛记录 {}", userId, contestId, team.getId());
        return team;
    }

    /** 战队报名：仅队长可提交，成员自动随队报名。 */
    @Transactional(rollbackFor = Exception.class)
    public ContestTeam enrollTeam(Long userId, Long contestId, Long globalTeamId) {
        Contest contest = guard.requireSignupOpen(contestId);
        if (!"TEAM".equals(contest.getMode())) {
            throw new BizException(ResultCode.CONFLICT, "该赛事为个人赛，不支持战队报名");
        }
        assertNotEnrolled(contestId, userId);
        if (globalTeamId == null) throw new BizException(ResultCode.BAD_REQUEST, "请选择参赛战队");

        Team team = globalTeamMapper.selectById(globalTeamId);
        if (team == null) throw BizException.notFound("战队");
        if (!java.util.Objects.equals(team.getCaptainId(), userId)) {
            throw new BizException(ResultCode.FORBIDDEN, "仅队长可为战队报名");
        }
        Long bound = teamMapper.selectCount(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getContestId, contestId)
                .eq(ContestTeam::getTeamId, globalTeamId));
        if (bound != null && bound > 0) throw new BizException(ResultCode.CONFLICT, "该战队已报名本赛事");

        List<Long> memberIds = globalTeamService.memberUserIds(globalTeamId);
        if (memberIds.isEmpty()) memberIds = List.of(userId);
        for (Long uid : memberIds) assertNotEnrolled(contestId, uid);
        for (int i = 0; i < memberIds.size(); i++) assertCapacity(contest);

        ContestTeam enroll = new ContestTeam();
        enroll.setContestId(contestId);
        enroll.setTeamId(globalTeamId);
        enroll.setName(team.getName());
        enroll.setLogo(team.getLogo());
        enroll.setDescription(team.getDescription());
        enroll.setSlogan(team.getSlogan());
        enroll.setCaptainId(userId);
        enroll.setMembers(memberIds.size());
        enroll.setStatus("APPROVED");
        enroll.setScore(0L);
        enroll.setMaxMembers(team.getMaxMembers());
        teamMapper.insert(enroll);

        for (Long uid : memberIds) {
            joinMember(contestId, enroll.getId(), uid, java.util.Objects.equals(uid, userId) ? "CAPTAIN" : "MEMBER");
        }
        registerScoring(contest, enroll);

        wsPublisher.publish("TEAM_ENROLLED", contestId,
                Map.of("teamId", enroll.getId(), "teamName", enroll.getName(), "members", memberIds.size()));
        log.info("战队报名: 队长 {} 以战队 {} 报名赛事 {}，成员 {} 人", userId, team.getName(), contestId, memberIds.size());
        return enroll;
    }

    /** 取消报名。队长退出即整支战队撤赛，队员仅退出自己。 */
    @Transactional(rollbackFor = Exception.class)
    public void quit(Long userId, Long contestId) {
        Contest contest = guard.requireContest(contestId);
        ContestTeamMember m = memberMapper.selectOne(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getContestId, contestId)
                .eq(ContestTeamMember::getUserId, userId));
        if (m == null) throw new BizException(ResultCode.CONFLICT, "未报名该赛事");
        ContestTeam team = teamMapper.selectById(m.getTeamId());
        if (team == null) return;

        boolean captain = "CAPTAIN".equals(m.getRole());
        if (!captain) {
            memberMapper.deleteById(m.getId());
            int left = contestMemberCount(team.getId());
            if (team.getTeamId() != null) {
                team.setMembers(left);
                teamMapper.updateById(team);
            }
            return;
        }

        if (team.getTeamId() != null) {
            // 战队参赛：整队撤赛，全局战队本身保留
            memberMapper.delete(new LambdaQueryWrapper<ContestTeamMember>()
                    .eq(ContestTeamMember::getTeamId, team.getId()));
        } else {
            int others = contestMemberCount(team.getId());
            if (others > 1) throw new BizException(ResultCode.CONFLICT, "队长需先转让或解散战队");
            memberMapper.deleteById(m.getId());
        }
        team.setStatus("QUIT");
        team.setMembers(0);
        teamMapper.updateById(team);
        unregisterScoring(contest, team);
    }

    /** 审核战队（裁判/管理员）。 */
    public void review(Long contestId, Long teamId, String status) {
        if (!List.of("APPROVED", "REJECTED", "PENDING").contains(status)) {
            throw new BizException(ResultCode.BAD_REQUEST, "非法审核状态：" + status);
        }
        ContestTeam team = requireEnroll(contestId, teamId);
        team.setStatus(status);
        teamMapper.updateById(team);
        wsPublisher.publish("TEAM_REVIEWED", contestId, Map.of("teamId", teamId, "status", status));
    }

    /** 当前用户在某赛事中的参赛记录（可能为 null）。 */
    public ContestTeam myTeam(Long userId, Long contestId) {
        if (userId == null) return null;
        ContestTeamMember m = memberMapper.selectOne(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getContestId, contestId)
                .eq(ContestTeamMember::getUserId, userId));
        return m == null ? null : teamMapper.selectById(m.getTeamId());
    }

    public ContestTeam requireEnroll(Long contestId, Long teamId) {
        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null || !contestId.equals(team.getContestId())) throw BizException.notFound("参赛战队");
        return team;
    }

    public List<ContestTeamMember> teamMembers(Long teamId) {
        return memberMapper.selectList(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getTeamId, teamId));
    }

    // ---------- internal ----------

    private void assertIndividual(ContestTeam existing) {
        if (existing.getTeamId() != null) {
            throw new BizException(ResultCode.CONFLICT, "你已随战队参赛，请先退出");
        }
    }

    private void assertNotEnrolled(Long contestId, Long userId) {
        Long n = memberMapper.selectCount(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getContestId, contestId)
                .eq(ContestTeamMember::getUserId, userId));
        if (n != null && n > 0) throw new BizException(ResultCode.CONFLICT, "有成员已在本赛事中参赛，请先让其退出");
    }

    private void assertCapacity(Contest contest) {
        if (contest.getMaxPlayers() == null) return;
        Long n = memberMapper.selectCount(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getContestId, contest.getId()));
        if (n != null && n >= contest.getMaxPlayers()) {
            throw new BizException(ResultCode.CONFLICT, "参赛人数已达上限");
        }
    }

    private int contestMemberCount(Long teamId) {
        return teamMapper.countMembers(teamId);
    }

    private void joinMember(Long contestId, Long teamId, Long userId, String role) {
        ContestTeamMember m = new ContestTeamMember();
        m.setContestId(contestId);
        m.setTeamId(teamId);
        m.setUserId(userId);
        m.setRole(role);
        m.setJoinedAt(LocalDateTime.now());
        try {
            memberMapper.insert(m);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new BizException(ResultCode.CONFLICT, "有成员已在本赛事中参赛，请先让其退出");
        }
    }

    private void registerScoring(Contest contest, ContestTeam team) {
        if ("AWD".equals(contest.getType()) || "AWDP".equals(contest.getType())) {
            redisService.zAdd(awdKey(contest.getId()), team.getId(), 0);
        }
    }

    private void unregisterScoring(Contest contest, ContestTeam team) {
        if ("AWD".equals(contest.getType()) || "AWDP".equals(contest.getType())) {
            redisService.zRemove(awdKey(contest.getId()), team.getId());
        }
    }

    private String awdKey(Long contestId) {
        // 必须与 csp-awd 的 AwdKeys.total() 保持一致；csp-ctf 不能反向依赖 csp-awd，故此处内联
        return "csp:awd:total:" + contestId;
    }
}
