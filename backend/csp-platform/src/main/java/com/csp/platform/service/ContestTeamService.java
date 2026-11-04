package com.csp.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.dto.TeamMemberCmd;
import com.csp.platform.dto.TeamSaveCmd;
import com.csp.platform.entity.ContestInfo;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.ContestTeamMember;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.ctf.mapper.ContestTeamMemberMapper;
import com.csp.security.core.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 战队与成员管理。 */
@Service
@RequiredArgsConstructor
public class ContestTeamService {

    private final ContestTeamMapper teamMapper;
    private final ContestTeamMemberMapper memberMapper;
    private final ContestService contestService;

    public List<ContestTeam> listByContest(Long contestId, String status) {
        contestService.get(contestId);
        return teamMapper.selectList(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getContestId, contestId)
                .eq(status != null, ContestTeam::getStatus, status)
                .orderByDesc(ContestTeam::getScore));
    }

    public ContestTeam get(Long id) {
        ContestTeam t = teamMapper.selectById(id);
        if (t == null) throw new BizException(ResultCode.NOT_FOUND, "战队不存在：" + id);
        return t;
    }

    @Transactional
    public ContestTeam create(Long contestId, TeamSaveCmd cmd) {
        ContestInfo c = contestService.get(contestId);
        if (!"SIGNUP".equals(c.getStatus()) && !"PENDING".equals(c.getStatus())
                && !(Integer.valueOf(1).equals(c.getAllowLate()) && "RUNNING".equals(c.getStatus()))) {
            throw new BizException(ResultCode.CONFLICT, "当前赛事状态（" + c.getStatus() + "）不接受报名");
        }
        Long dup = teamMapper.selectCount(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getContestId, contestId).eq(ContestTeam::getName, cmd.getName()));
        if (dup != null && dup > 0) throw new BizException(ResultCode.CONFLICT, "战队名已存在：" + cmd.getName());

        Long captainId = cmd.getCaptainId() != null ? cmd.getCaptainId() : UserContext.requireUserId();
        ContestTeam t = new ContestTeam();
        BeanUtils.copyProperties(cmd, t);
        t.setContestId(contestId);
        t.setCaptainId(captainId);
        t.setMembers(0);
        t.setScore(0L);
        t.setStatus("PENDING");
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        teamMapper.insert(t);

        TeamMemberCmd m = new TeamMemberCmd();
        m.setUserId(captainId);
        m.setRole("CAPTAIN");
        addMember(t.getId(), m);
        return get(t.getId());
    }

    @Transactional
    public ContestTeam reviewTeam(Long teamId, String status) {
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status) && !"QUIT".equals(status)) {
            throw new BizException(ResultCode.BAD_REQUEST, "审核状态非法：" + status);
        }
        ContestTeam t = get(teamId);
        t.setStatus(status);
        t.setUpdatedAt(LocalDateTime.now());
        teamMapper.updateById(t);
        return t;
    }

    @Transactional
    public void addMember(Long teamId, TeamMemberCmd cmd) {
        ContestTeam t = get(teamId);
        if ("QUIT".equals(t.getStatus()) || "REJECTED".equals(t.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "战队不可加入：" + t.getStatus());
        }
        Long dup = memberMapper.selectCount(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getContestId, t.getContestId())
                .eq(ContestTeamMember::getUserId, cmd.getUserId()));
        if (dup != null && dup > 0) throw new BizException(ResultCode.CONFLICT, "该用户已在赛事队伍中");

        ContestTeamMember m = new ContestTeamMember();
        m.setContestId(t.getContestId());
        m.setTeamId(teamId);
        m.setUserId(cmd.getUserId());
        m.setRole("CAPTAIN".equals(cmd.getRole()) ? "CAPTAIN" : "MEMBER");
        m.setJoinedAt(LocalDateTime.now());
        memberMapper.insert(m);

        t.setMembers(memberMapper.selectCount(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getTeamId, teamId)).intValue());
        t.setUpdatedAt(LocalDateTime.now());
        teamMapper.updateById(t);
    }

    @Transactional
    public void removeMember(Long teamId, Long userId) {
        ContestTeam t = get(teamId);
        if (t.getCaptainId() != null && t.getCaptainId().equals(userId)) {
            throw new BizException(ResultCode.CONFLICT, "队长不可直接移除，请先转让队长");
        }
        memberMapper.delete(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getTeamId, teamId)
                .eq(ContestTeamMember::getUserId, userId));
        t.setMembers(memberMapper.selectCount(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getTeamId, teamId)).intValue());
        t.setUpdatedAt(LocalDateTime.now());
        teamMapper.updateById(t);
    }

    public List<ContestTeamMember> listMembers(Long teamId) {
        get(teamId);
        return memberMapper.selectList(new LambdaQueryWrapper<ContestTeamMember>()
                .eq(ContestTeamMember::getTeamId, teamId));
    }
}
