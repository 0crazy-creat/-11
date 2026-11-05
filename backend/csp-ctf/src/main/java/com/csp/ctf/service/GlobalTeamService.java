package com.csp.ctf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.dto.TeamProfileCmd;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.ContestTeamMember;
import com.csp.ctf.entity.Team;
import com.csp.ctf.entity.TeamMember;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.ctf.mapper.ContestTeamMemberMapper;
import com.csp.ctf.mapper.TeamMapper;
import com.csp.ctf.mapper.TeamMemberMapper;
import com.csp.ctf.vo.TeamMemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 全局战队：创建、加入、退出、解散、成员管理与邀请码。
 *
 * <p>战队本身与赛事无关；战队参加某赛事时由 {@link ContestEnrollService} 生成参赛记录，
 * 成员变动再反向同步到参赛记录，保证排行榜、容器、AWD 靶机等下游逻辑不变。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalTeamService {

    public static final int DEFAULT_MAX_MEMBERS = 8;
    private static final int MAX_ALLOWED_MEMBERS = 50;
    private static final String CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final ContestTeamMapper contestTeamMapper;
    private final ContestTeamMemberMapper contestMemberMapper;
    private final JdbcTemplate jdbcTemplate;

    // ---------- 查询 ----------

    /** 与我相关的全部战队。 */
    public List<Team> myTeams(Long userId) {
        List<TeamMember> mine = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId));
        if (mine.isEmpty()) return List.of();
        List<Long> ids = mine.stream().map(TeamMember::getTeamId).toList();
        Map<Long, String> roleOf = mine.stream()
                .collect(java.util.stream.Collectors.toMap(TeamMember::getTeamId, TeamMember::getRole, (a, b) -> a));
        List<Team> teams = teamMapper.selectList(new LambdaQueryWrapper<Team>()
                .in(Team::getId, ids).orderByDesc(Team::getCreatedAt));
        teams.forEach(t -> {
            t.setMyRole(roleOf.get(t.getId()));
            t.setEnrolledContests(teamMapper.countEnrollments(t.getId()));
        });
        return teams;
    }

    /** 浏览全部战队（用于加入）。 */
    public List<Team> browse(Long userId, String keyword, int limit) {
        LambdaQueryWrapper<Team> qw = new LambdaQueryWrapper<Team>()
                .orderByDesc(Team::getCreatedAt).last("LIMIT " + Math.min(Math.max(limit, 1), 200));
        if (StringUtils.hasText(keyword)) qw.like(Team::getName, keyword);
        List<Team> teams = teamMapper.selectList(qw);
        teams.forEach(t -> {
            t.setMyRole(roleOf(userId, t.getId()));
            t.setEnrolledContests(teamMapper.countEnrollments(t.getId()));
            if (!java.util.Objects.equals(t.getCaptainId(), userId)) t.setInviteCode(null);
        });
        return teams;
    }

    public Team detail(Long userId, Long teamId) {
        Team team = requireTeam(teamId);
        String role = roleOf(userId, teamId);
        team.setMyRole(role);
        team.setEnrolledContests(teamMapper.countEnrollments(teamId));
        if (!"CAPTAIN".equals(role)) team.setInviteCode(null);
        return team;
    }

    public List<TeamMemberVO> members(Long teamId) {
        requireTeam(teamId);
        String sql = "SELECT m.user_id, m.role, m.joined_at, u.username, u.nickname, u.avatar, u.org_code, u.org_name "
                + "FROM team_member m LEFT JOIN sys_user u ON u.id = m.user_id "
                + "WHERE m.team_id = ? ORDER BY (m.role = 'CAPTAIN') DESC, m.joined_at ASC";
        return jdbcTemplate.query(sql, (rs, n) -> {
            TeamMemberVO vo = new TeamMemberVO();
            vo.setUserId(rs.getLong("user_id"));
            vo.setRole(rs.getString("role"));
            vo.setJoinedAt(rs.getTimestamp("joined_at") == null ? null : rs.getTimestamp("joined_at").toLocalDateTime());
            vo.setUsername(rs.getString("username"));
            vo.setNickname(rs.getString("nickname"));
            vo.setAvatar(rs.getString("avatar"));
            vo.setOrgCode(rs.getString("org_code"));
            vo.setOrgName(rs.getString("org_name"));
            return vo;
        }, teamId);
    }

    /** 战队全部成员用户 id。 */
    public List<Long> memberUserIds(Long teamId) {
        return teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId))
                .stream().map(TeamMember::getUserId).toList();
    }

    public String roleOf(Long userId, Long teamId) {
        if (userId == null || teamId == null) return null;
        TeamMember m = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, userId));
        return m == null ? null : m.getRole();
    }

    // ---------- 变更 ----------

    @Transactional(rollbackFor = Exception.class)
    public Team create(Long userId, TeamProfileCmd cmd) {
        if (teamMapper.countByName(cmd.getName()) > 0) {
            throw new BizException(ResultCode.CONFLICT, "战队名已存在");
        }
        Team team = new Team();
        team.setName(cmd.getName());
        team.setLogo(cmd.getLogo());
        team.setDescription(cmd.getDescription());
        team.setSlogan(cmd.getSlogan());
        team.setCaptainId(userId);
        team.setMaxMembers(normalizeMax(cmd.getMaxMembers()));
        team.setMembers(1);
        team.setInviteCode(newInviteCode());
        team.setInviteExpireAt(LocalDateTime.now().plusYears(10));
        teamMapper.insert(team);

        insertMember(team.getId(), userId, "CAPTAIN");
        log.info("用户 {} 创建战队 {}（{}）", userId, team.getName(), team.getId());
        return team;
    }

    @Transactional(rollbackFor = Exception.class)
    public Team update(Long userId, Long teamId, TeamProfileCmd cmd) {
        Team team = requireCaptain(userId, teamId);
        if (!team.getName().equals(cmd.getName()) && teamMapper.countByName(cmd.getName()) > 0) {
            throw new BizException(ResultCode.CONFLICT, "战队名已存在");
        }
        team.setName(cmd.getName());
        team.setLogo(cmd.getLogo());
        team.setDescription(cmd.getDescription());
        team.setSlogan(cmd.getSlogan());
        team.setMaxMembers(normalizeMax(cmd.getMaxMembers()));
        if (team.getMembers() != null && team.getMembers() > team.getMaxMembers()) {
            team.setMaxMembers(team.getMembers());
        }
        teamMapper.updateById(team);
        syncNameToEnrollments(team);
        return team;
    }

    @Transactional(rollbackFor = Exception.class)
    public Team joinByCode(Long userId, String inviteCode) {
        if (!StringUtils.hasText(inviteCode)) throw new BizException(ResultCode.BAD_REQUEST, "邀请码不能为空");
        Team team = teamMapper.selectOne(new LambdaQueryWrapper<Team>()
                .eq(Team::getInviteCode, inviteCode.trim().toUpperCase()));
        if (team == null) throw new BizException(ResultCode.NOT_FOUND, "邀请码无效");
        if (roleOf(userId, team.getId()) != null) throw new BizException(ResultCode.CONFLICT, "你已在该战队中");
        if (team.getInviteExpireAt() != null && LocalDateTime.now().isAfter(team.getInviteExpireAt())) {
            throw new BizException(ResultCode.CONFLICT, "邀请码已过期");
        }
        if (countMembers(team.getId()) >= maxOf(team)) {
            throw new BizException(ResultCode.CONFLICT, "战队人数已达上限 " + maxOf(team));
        }

        insertMember(team.getId(), userId, "MEMBER");
        refreshCount(team);
        // 已参赛的赛事需同步补上成员，冲突则整体回滚，避免出现「在队未参赛」
        syncAddMember(team.getId(), userId);
        return team;
    }

    @Transactional(rollbackFor = Exception.class)
    public void quit(Long userId, Long teamId) {
        Team team = requireTeam(teamId);
        TeamMember m = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, userId));
        if (m == null) throw new BizException(ResultCode.CONFLICT, "你不在该战队中");
        if ("CAPTAIN".equals(m.getRole()) && countMembers(teamId) > 1) {
            throw new BizException(ResultCode.CONFLICT, "队长需先转让队长或解散战队");
        }
        teamMemberMapper.deleteById(m.getId());
        refreshCount(team);
        syncRemoveMember(teamId, userId);
        if (countMembers(teamId) == 0) disbandInternal(team);
    }

    @Transactional(rollbackFor = Exception.class)
    public void kick(Long userId, Long teamId, Long targetUserId) {
        requireCaptain(userId, teamId);
        if (userId.equals(targetUserId)) throw new BizException(ResultCode.BAD_REQUEST, "不能移除自己");
        TeamMember m = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, targetUserId));
        if (m == null) throw BizException.notFound("成员");
        if ("CAPTAIN".equals(m.getRole())) throw new BizException(ResultCode.CONFLICT, "不能移除队长");
        teamMemberMapper.deleteById(m.getId());
        Team team = requireTeam(teamId);
        refreshCount(team);
        syncRemoveMember(teamId, targetUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void transfer(Long userId, Long teamId, Long targetUserId) {
        requireCaptain(userId, teamId);
        TeamMember target = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, targetUserId));
        if (target == null) throw new BizException(ResultCode.BAD_REQUEST, "目标用户不在该战队");
        target.setRole("CAPTAIN");
        teamMemberMapper.updateById(target);

        TeamMember me = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, userId));
        if (me != null) {
            me.setRole("MEMBER");
            teamMemberMapper.updateById(me);
        }
        Team team = requireTeam(teamId);
        team.setCaptainId(targetUserId);
        teamMapper.updateById(team);

        for (ContestTeam ct : enrolledContests(teamId)) {
            ct.setCaptainId(targetUserId);
            contestTeamMapper.updateById(ct);
            setContestRole(teamId, targetUserId, "CAPTAIN");
            setContestRole(teamId, userId, "MEMBER");
        }
        log.info("战队 {} 队长由 {} 转让给 {}", teamId, userId, targetUserId);
    }

    /** 解散战队：仅队长、且需先退出所有赛事。 */
    @Transactional(rollbackFor = Exception.class)
    public void disband(Long userId, Long teamId) {
        Team team = requireCaptain(userId, teamId);
        List<ContestTeam> enrolled = enrolledContests(teamId);
        if (!enrolled.isEmpty()) {
            throw new BizException(ResultCode.CONFLICT, "该战队已报名 " + enrolled.size() + " 个赛事，请先取消报名");
        }
        disbandInternal(team);
    }

    /** 管理员强制解散：连同参赛记录一并撤下。 */
    @Transactional(rollbackFor = Exception.class)
    public void forceDisband(Long teamId) {
        Team team = requireTeam(teamId);
        for (ContestTeam ct : enrolledContests(teamId)) {
            contestMemberMapper.delete(new LambdaQueryWrapper<ContestTeamMember>()
                    .eq(ContestTeamMember::getTeamId, ct.getId()));
            ct.setStatus("QUIT");
            contestTeamMapper.updateById(ct);
            contestTeamMapper.deleteById(ct.getId());
        }
        disbandInternal(team);
    }

    @Transactional(rollbackFor = Exception.class)
    public String refreshInviteCode(Long userId, Long teamId) {
        Team team = requireCaptain(userId, teamId);
        team.setInviteCode(newInviteCode());
        team.setInviteExpireAt(LocalDateTime.now().plusYears(10));
        teamMapper.updateById(team);
        return team.getInviteCode();
    }

    // ---------- 参赛联动 ----------

    /** 战队名变更后同步到参赛记录（排行榜/靶机展示用）。 */
    private void syncNameToEnrollments(Team team) {
        for (ContestTeam ct : enrolledContests(team.getId())) {
            ct.setName(team.getName());
            ct.setLogo(team.getLogo());
            contestTeamMapper.updateById(ct);
        }
    }

    private List<ContestTeam> enrolledContests(Long teamId) {
        return contestTeamMapper.selectList(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getTeamId, teamId)
                .ne(ContestTeam::getStatus, "QUIT"));
    }

    private void syncAddMember(Long teamId, Long userId) {
        for (ContestTeam ct : enrolledContests(teamId)) {
            Long exists = contestMemberMapper.selectCount(new LambdaQueryWrapper<ContestTeamMember>()
                    .eq(ContestTeamMember::getContestId, ct.getContestId())
                    .eq(ContestTeamMember::getUserId, userId));
            if (exists != null && exists > 0) {
                throw new BizException(ResultCode.CONFLICT, "你已在该赛事中以其他身份参赛，请先退出后再加入此战队");
            }
            ContestTeamMember m = new ContestTeamMember();
            m.setContestId(ct.getContestId());
            m.setTeamId(ct.getId());
            m.setUserId(userId);
            m.setRole("MEMBER");
            m.setJoinedAt(LocalDateTime.now());
            contestMemberMapper.insert(m);
            ct.setMembers(contestTeamMapper.countMembers(ct.getId()));
            contestTeamMapper.updateById(ct);
        }
    }

    private void syncRemoveMember(Long teamId, Long userId) {
        for (ContestTeam ct : enrolledContests(teamId)) {
            contestMemberMapper.delete(new LambdaQueryWrapper<ContestTeamMember>()
                    .eq(ContestTeamMember::getTeamId, ct.getId())
                    .eq(ContestTeamMember::getUserId, userId));
            ct.setMembers(contestTeamMapper.countMembers(ct.getId()));
            contestTeamMapper.updateById(ct);
        }
    }

    private void setContestRole(Long teamId, Long userId, String role) {
        for (ContestTeam ct : enrolledContests(teamId)) {
            ContestTeamMember m = contestMemberMapper.selectOne(new LambdaQueryWrapper<ContestTeamMember>()
                    .eq(ContestTeamMember::getTeamId, ct.getId())
                    .eq(ContestTeamMember::getUserId, userId));
            if (m != null) {
                m.setRole(role);
                contestMemberMapper.updateById(m);
            }
        }
    }

    // ---------- internal ----------

    private void disbandInternal(Team team) {
        teamMemberMapper.delete(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, team.getId()));
        teamMapper.clearInviteCode(team.getId());
        teamMapper.deleteById(team.getId());
        log.info("战队 {}（{}）已解散", team.getName(), team.getId());
    }

    private void insertMember(Long teamId, Long userId, String role) {
        TeamMember m = new TeamMember();
        m.setTeamId(teamId);
        m.setUserId(userId);
        m.setRole(role);
        m.setJoinedAt(LocalDateTime.now());
        teamMemberMapper.insert(m);
    }

    private void refreshCount(Team team) {
        team.setMembers(countMembers(team.getId()));
        teamMapper.updateById(team);
    }

    private int countMembers(Long teamId) {
        return teamMemberMapper.countByTeam(teamId);
    }

    private int maxOf(Team team) {
        return team.getMaxMembers() == null ? DEFAULT_MAX_MEMBERS : team.getMaxMembers();
    }

    private Team requireTeam(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) throw BizException.notFound("战队");
        return team;
    }

    private Team requireCaptain(Long userId, Long teamId) {
        Team team = requireTeam(teamId);
        if (!java.util.Objects.equals(team.getCaptainId(), userId) && !"CAPTAIN".equals(roleOf(userId, teamId))) {
            throw BizException.forbidden("仅队长可执行该操作");
        }
        return team;
    }

    private int normalizeMax(Integer max) {
        if (max == null || max < 1) return DEFAULT_MAX_MEMBERS;
        return Math.min(max, MAX_ALLOWED_MEMBERS);
    }

    private String newInviteCode() {
        SecureRandom random = new SecureRandom();
        List<String> tried = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            StringBuilder sb = new StringBuilder(8);
            for (int j = 0; j < 8; j++) sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            String code = sb.toString();
            tried.add(code);
            if (teamMapper.countByInviteCode(code) == 0) return code;
        }
        throw new BizException(ResultCode.CONFLICT, "邀请码生成失败，请重试");
    }

    /** 转让队长时用于占位的空记录标记。 */
    private interface TeamMe记录 {
    }
}
