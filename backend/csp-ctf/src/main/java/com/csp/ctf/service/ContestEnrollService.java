package com.csp.ctf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.dto.TeamCreateRequest;
import com.csp.ctf.entity.Contest;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.ContestTeamMember;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.ctf.mapper.ContestTeamMemberMapper;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 报名：建队、加入、退出、审核。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContestEnrollService {

    private static final int MAX_TEAM_MEMBERS = 8;

    private final ContestGuard guard;
    private final ContestTeamMapper teamMapper;
    private final ContestTeamMemberMapper memberMapper;
    private final WsPublisher wsPublisher;
    private final RedisService redisService;

    /** 创建战队并自动成为队长。 */
    @Transactional(rollbackFor = Exception.class)
    public ContestTeam createTeam(Long userId, TeamCreateRequest req) {
        Contest contest = guard.requireSignupOpen(req.getContestId());
        assertNotEnrolled(contest.getId(), userId);
        assertCapacity(contest);

        // 同一赛事下战队名唯一
        Long dup = teamMapper.selectCount(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getContestId, contest.getId())
                .eq(ContestTeam::getName, req.getName()));
        if (dup != null && dup > 0) throw new BizException(ResultCode.CONFLICT, "战队名已存在");

        ContestTeam team = new ContestTeam();
        team.setContestId(contest.getId());
        team.setName(req.getName());
        team.setCode(req.getCode());
        team.setLogo(req.getLogo());
        team.setOrgCode(req.getOrgCode());
        team.setCaptainId(userId);
        team.setMembers(1);
        team.setStatus("APPROVED");
        team.setScore(0L);
        team.setMaxMembers(MAX_TEAM_MEMBERS);
        teamMapper.insert(team);

        // 自动生成邀请码
        generateInviteCode(team.getId());

        joinMember(contest.getId(), team.getId(), userId, "CAPTAIN");
        
        // AWD/AWDP 赛事自动注册计分（初始化 Redis ZSet 分数为 0）
        if ("AWD".equals(contest.getType()) || "AWDP".equals(contest.getType())) {
            String redisKey = "awd:total:" + contest.getId();
            redisService.zAdd(redisKey, team.getId(), 0);
            log.info("AWD 赛事 {} 自动注册战队 {} 计分", contest.getId(), team.getId());
        }
        
        wsPublisher.publish("TEAM_CREATED", contest.getId(),
                Map.of("teamId", team.getId(), "teamName", team.getName()));
        log.info("用户 {} 在赛事 {} 创建战队 {}（{}）", userId, contest.getId(), team.getName(), team.getId());
        return team;
    }

    /** 加入已有战队。 */
    @Transactional(rollbackFor = Exception.class)
    public ContestTeam joinTeam(Long userId, Long contestId, Long teamId) {
        Contest contest = guard.requireSignupOpen(contestId);
        assertNotEnrolled(contestId, userId);

        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null || !contestId.equals(team.getContestId())) throw BizException.notFound("战队");
        if (!"APPROVED".equals(team.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "战队当前状态不可加入：" + team.getStatus());
        }
        if (teamMapper.countMembers(teamId) >= MAX_TEAM_MEMBERS) {
            throw new BizException(ResultCode.CONFLICT, "战队人数已达上限 " + MAX_TEAM_MEMBERS);
        }
        joinMember(contestId, teamId, userId, "MEMBER");
        team.setMembers(team.getMembers() == null ? 1 : team.getMembers() + 1);
        teamMapper.updateById(team);
        return team;
    }

    /** 退出/取消报名。队长退出需先转让或解散。 */
    @Transactional(rollbackFor = Exception.class)
    public void quit(Long userId, Long contestId) {
        Contest contest = guard.requireContest(contestId);
        ContestTeamMember m = memberMapper.selectOne(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getContestId, contestId)
                .eq(ContestTeamMember::getUserId, userId));
        if (m == null) throw new BizException(ResultCode.CONFLICT, "未报名该赛事");
        if ("CAPTAIN".equals(m.getRole())) {
            Long others = memberMapper.selectCount(new LambdaQueryWrapper<ContestTeamMember>()
                    .eq(ContestTeamMember::getTeamId, m.getTeamId()));
            if (others != null && others > 1) {
                throw new BizException(ResultCode.CONFLICT, "队长需先转让或解散战队");
            }
        }
        memberMapper.deleteById(m.getId());
        ContestTeam team = teamMapper.selectById(m.getTeamId());
        if (team != null) {
            int left = team.getMembers() == null ? 0 : Math.max(0, team.getMembers() - 1);
            team.setMembers(left);
            if (left == 0) {
                team.setStatus("QUIT");
                // AWD/AWDP 赛事清理计分
                if ("AWD".equals(contest.getType()) || "AWDP".equals(contest.getType())) {
                    String redisKey = "awd:total:" + contest.getId();
                    redisService.zRemove(redisKey, team.getId());
                    log.info("AWD 赛事 {} 清理战队 {} 计分", contest.getId(), team.getId());
                }
            }
            teamMapper.updateById(team);
        }
        // 释放唯一约束占位：物理删除即可让 uk_ctm(contest_id,user_id) 可再次使用
    }

    /** 审核战队（裁判/管理员）。 */
    public void review(Long contestId, Long teamId, String status) {
        if (!List.of("APPROVED", "REJECTED", "PENDING").contains(status)) {
            throw new BizException(ResultCode.BAD_REQUEST, "非法审核状态：" + status);
        }
        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null || !contestId.equals(team.getContestId())) throw BizException.notFound("战队");
        team.setStatus(status);
        teamMapper.updateById(team);
        wsPublisher.publish("TEAM_REVIEWED", contestId, Map.of("teamId", teamId, "status", status));
    }

    /** 当前用户在某赛事中的战队（可能为 null）。 */
    public ContestTeam myTeam(Long userId, Long contestId) {
        ContestTeamMember m = memberMapper.selectOne(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getContestId, contestId)
                .eq(ContestTeamMember::getUserId, userId));
        return m == null ? null : teamMapper.selectById(m.getTeamId());
    }

    public List<ContestTeamMember> teamMembers(Long teamId) {
        return memberMapper.selectList(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getTeamId, teamId));
    }

    // ---------- 邀请码相关 ----------

    /**
     * 生成邀请码（8位大写字母+数字，排除易混淆字符）。
     */
    @Transactional(rollbackFor = Exception.class)
    public String generateInviteCode(Long teamId) {
        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null) throw BizException.notFound("战队");

        // 生成8位随机码（排除0/O, 1/I/L等易混淆字符）
        String alphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        String code;
        do {
            code = generateRandomCode(alphabet, 8);
        } while (teamMapper.selectCount(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getInviteCode, code)) > 0);

        team.setInviteCode(code);
        team.setInviteExpireAt(LocalDateTime.now().plusHours(24)); // 24小时有效
        teamMapper.updateById(team);

        log.info("战队 {} 生成邀请码: {}", teamId, code);
        return code;
    }

    /**
     * 通过邀请码加入战队。
     */
    @Transactional(rollbackFor = Exception.class)
    public ContestTeam joinByInviteCode(Long userId, Long contestId, String inviteCode) {
        // 查找战队
        ContestTeam team = teamMapper.selectOne(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getContestId, contestId)
                .eq(ContestTeam::getInviteCode, inviteCode));

        if (team == null) {
            throw new BizException(ResultCode.NOT_FOUND, "邀请码无效");
        }

        // 校验过期
        if (team.getInviteExpireAt() != null && LocalDateTime.now().isAfter(team.getInviteExpireAt())) {
            throw new BizException(ResultCode.CONFLICT, "邀请码已过期");
        }

        // 校验人数上限
        int maxMembers = team.getMaxMembers() != null ? team.getMaxMembers() : MAX_TEAM_MEMBERS;
        if (team.getMembers() >= maxMembers) {
            throw new BizException(ResultCode.CONFLICT, "战队人数已达上限 " + maxMembers);
        }

        // 加入战队
        return joinTeam(userId, contestId, team.getId());
    }

    /**
     * 刷新邀请码（重新生成）。
     */
    @Transactional(rollbackFor = Exception.class)
    public String refreshInviteCode(Long teamId, Long userId) {
        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null) throw BizException.notFound("战队");
        if (!team.getCaptainId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, "仅队长可刷新邀请码");
        }
        return generateInviteCode(teamId);
    }

    private String generateRandomCode(String alphabet, int length) {
        StringBuilder sb = new StringBuilder(length);
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    // ---------- internal ----------

    private void assertNotEnrolled(Long contestId, Long userId) {
        Long n = memberMapper.selectCount(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getContestId, contestId)
                .eq(ContestTeamMember::getUserId, userId));
        if (n != null && n > 0) throw new BizException(ResultCode.CONFLICT, "已报名该赛事，不可重复报名");
    }

    private void assertCapacity(Contest contest) {
        if (contest.getMaxPlayers() == null) return;
        Long n = memberMapper.selectCount(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getContestId, contest.getId()));
        if (n != null && n >= contest.getMaxPlayers()) {
            throw new BizException(ResultCode.CONFLICT, "参赛人数已达上限");
        }
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
            throw new BizException(ResultCode.CONFLICT, "已报名该赛事，不可重复报名");
        }
    }
}
