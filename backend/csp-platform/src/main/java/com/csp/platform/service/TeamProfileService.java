package com.csp.platform.service;

import com.csp.ctf.entity.Team;
import com.csp.ctf.mapper.TeamMapper;
import com.csp.ctf.service.GlobalTeamService;
import com.csp.platform.dto.TeamProfileVO;
import com.csp.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 战队主页聚合（全局战队维度，跨赛事）。
 *
 * <p>口径说明：一血按「同一赛事同一题的全局最早一次正确提交」判定，
 * 提交人属于本队成员即记为该队一血；每人每题只计最早一次得分，与实时排行榜一致。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamProfileService {

    private static final int MAX_WRITEUPS = 50;

    private final JdbcTemplate jdbcTemplate;
    private final TeamMapper teamMapper;
    private final GlobalTeamService globalTeamService;

    public TeamProfileVO profile(Long teamId, Long currentUserId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) throw BizException.notFound("战队");

        TeamProfileVO vo = new TeamProfileVO();
        vo.setTeamId(team.getId());
        vo.setName(team.getName());
        vo.setLogo(team.getLogo());
        vo.setSlogan(team.getSlogan());
        vo.setDescription(team.getDescription());
        vo.setCaptainId(team.getCaptainId());
        vo.setMembers(team.getMembers());
        vo.setMaxMembers(team.getMaxMembers());
        vo.setCreatedAt(team.getCreatedAt());
        vo.setMyRole(globalTeamService.roleOf(currentUserId, teamId));

        List<TeamProfileVO.MemberStat> roster = buildRoster(teamId);
        vo.setRoster(roster);
        Set<Long> memberIds = new java.util.HashSet<>();
        roster.forEach(m -> memberIds.add(m.getUserId()));
        vo.setCaptainName(roster.stream().filter(m -> Objects.equals(m.getUserId(), team.getCaptainId()))
                .map(TeamProfileVO.MemberStat::getNickname).findFirst().orElse(null));

        List<ContestRow> contests = contestHistory(teamId);
        Set<Long> ownRecordIds = new java.util.HashSet<>();
        contests.forEach(c -> ownRecordIds.add(c.contestTeamId));
        Map<Long, List<SolveRow>> byContestTeam = new HashMap<>();
        List<SolveRow> solves = solveRows(teamId, memberIds, ownRecordIds);
        for (SolveRow s : solves) {
            if (s.contestTeamId != null) byContestTeam.computeIfAbsent(s.contestTeamId, k -> new ArrayList<>()).add(s);
        }

        vo.setHistory(contests.stream().map(c -> toHistory(c, byContestTeam.getOrDefault(c.contestTeamId, List.of())))
                .toList());
        vo.setAwards(awardsOf(vo.getHistory()));
        vo.setWriteups(writeups(teamId, memberIds));
        vo.setAbilities(aggregate(solves, true));
        vo.setDifficulties(aggregate(solves, false));
        vo.setStats(stats(vo, solves));
        fillMemberStats(roster, solves);
        return vo;
    }

    // ---------- 查询 ----------

    /** 成员基础信息（含昵称、头像、院校、角色）。 */
    private List<TeamProfileVO.MemberStat> buildRoster(Long teamId) {
        return globalTeamService.members(teamId).stream().map(m -> {
            TeamProfileVO.MemberStat s = new TeamProfileVO.MemberStat();
            s.setUserId(m.getUserId());
            s.setUsername(m.getUsername());
            s.setNickname(m.getNickname() == null || m.getNickname().isBlank() ? m.getUsername() : m.getNickname());
            s.setAvatar(m.getAvatar());
            s.setOrgName(m.getOrgName());
            s.setRole(m.getRole());
            s.setCategories(new LinkedHashMap<>());
            s.setDifficulties(new LinkedHashMap<>());
            return s;
        }).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    /** 参赛历史：本队在各赛事的参赛记录（已撤赛不计）。 */
    private List<ContestRow> contestHistory(Long teamId) {
        String sql = "SELECT ct.id AS contest_team_id, ct.contest_id, ct.status AS enroll_status, ct.members,"
                + "       IFNULL(ct.`rank`, 0) AS rank_no, IFNULL(ct.score, 0) AS score,"
                + "       c.name AS contest_name, c.code AS contest_code, c.type, c.status AS contest_status,"
                + "       c.start_at, c.end_at, c.organizer"
                + " FROM contest_team ct JOIN contest c ON c.id = ct.contest_id"
                + " WHERE ct.team_id = ? AND ct.deleted = 0 AND ct.status <> 'QUIT' AND c.deleted = 0"
                + " ORDER BY c.start_at DESC, c.id DESC";
        return jdbcTemplate.query(sql, (rs, n) -> {
            ContestRow r = new ContestRow();
            r.contestTeamId = rs.getLong("contest_team_id");
            r.contestId = rs.getLong("contest_id");
            r.enrollStatus = rs.getString("enroll_status");
            r.memberCount = rs.getInt("members");
            r.rank = rs.getInt("rank_no");
            r.score = rs.getLong("score");
            r.contestName = rs.getString("contest_name");
            r.contestCode = rs.getString("contest_code");
            r.type = rs.getString("type");
            r.contestStatus = rs.getString("contest_status");
            r.startAt = ts(rs, "start_at");
            r.endAt = ts(rs, "end_at");
            r.organizer = rs.getString("organizer");
            return r;
        }, teamId);
    }

    /**
     * 解题明细：本队参加过的赛事 + 成员个人参赛的赛事，全部题目去重后按全局解出顺序编号。
     * <p>bloodRank = 1 即一血；score 取提交当时的分值，历史回放不被动态衰减扭曲。</p>
     * <p>全局编号完成后再过滤，只保留属于本队参赛记录或本队成员（个人参赛）的解题行。</p>
     */
    private List<SolveRow> solveRows(Long teamId, Set<Long> memberIds, Set<Long> ownRecordIds) {
        List<Long> contestIds = jdbcTemplate.queryForList(
                "SELECT DISTINCT contest_id FROM contest_team WHERE team_id = ? AND deleted = 0 AND status <> 'QUIT'",
                Long.class, teamId);
        // 成员个人参赛（contest_team.team_id 为空）也要进入能力画像
        if (!memberIds.isEmpty()) {
            String in = placeholders(memberIds.size());
            contestIds.addAll(jdbcTemplate.queryForList(
                    "SELECT DISTINCT contest_id FROM submission WHERE deleted = 0 AND correct = 1 AND user_id IN (" + in + ")",
                    Long.class, memberIds.toArray()));
        }
        List<Long> unique = contestIds.stream().filter(Objects::nonNull).distinct().toList();
        if (unique.isEmpty()) return List.of();

        String in = placeholders(unique.size());
        String sql = "SELECT x.contest_id, x.team_id, x.user_id, x.question_id, x.score, x.created_at,"
                + "       ROW_NUMBER() OVER (PARTITION BY x.contest_id, x.question_id"
                + "         ORDER BY x.created_at ASC, x.id ASC) AS blood_rank,"
                + "       q.title, q.category, q.difficulty"
                + " FROM (SELECT s.id, s.contest_id, s.team_id, s.user_id, s.question_id, s.score, s.created_at,"
                + "              ROW_NUMBER() OVER (PARTITION BY s.contest_id,"
                + "                IFNULL(s.team_id, s.user_id), s.question_id"
                + "                ORDER BY s.created_at ASC, s.id ASC) rn"
                + "       FROM submission s"
                + "       WHERE s.deleted = 0 AND s.correct = 1 AND s.contest_id IN (" + in + ")) x"
                + " LEFT JOIN question q ON q.id = x.question_id"
                + " WHERE x.rn = 1"
                + " ORDER BY x.created_at ASC";
        List<SolveRow> all = jdbcTemplate.query(sql, (rs, n) -> {
            SolveRow r = new SolveRow();
            r.contestId = rs.getLong("contest_id");
            long teamRef = rs.getLong("team_id");
            r.contestTeamId = rs.wasNull() ? null : teamRef;
            r.userId = rs.getLong("user_id");
            r.questionId = rs.getLong("question_id");
            r.questionTitle = rs.getString("title");
            r.category = rs.getString("category");
            r.difficulty = rs.getString("difficulty");
            r.score = rs.getLong("score");
            r.solvedAt = ts(rs, "created_at");
            r.bloodRank = rs.getInt("blood_rank");
            return r;
        }, unique.toArray());

        List<SolveRow> mine = new ArrayList<>();
        for (SolveRow r : all) {
            boolean byTeam = r.contestTeamId != null && ownRecordIds.contains(r.contestTeamId);
            boolean byMember = r.userId != null && memberIds.contains(r.userId);
            if (byTeam || byMember) mine.add(r);
        }
        return mine;
    }

    private List<TeamProfileVO.WriteupBrief> writeups(Long teamId, Set<Long> memberIds) {
        String memberIn = memberIds.isEmpty() ? null : placeholders(memberIds.size());
        StringBuilder sql = new StringBuilder(
                "SELECT w.id, w.title, w.contest_id, w.question_id, w.user_id, w.view_count, w.like_count, w.created_at,"
                        + " c.name AS contest_name, q.title AS question_title, q.category, u.nickname, u.username"
                        + " FROM writeup w"
                        + " LEFT JOIN contest c ON c.id = w.contest_id"
                        + " LEFT JOIN question q ON q.id = w.question_id"
                        + " LEFT JOIN sys_user u ON u.id = w.user_id"
                        + " WHERE w.deleted = 0 AND w.status = 'APPROVED' AND (w.team_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(teamId);
        if (memberIn != null) {
            sql.append(" OR w.user_id IN (").append(memberIn).append(')');
            args.addAll(memberIds);
        }
        sql.append(") ORDER BY w.created_at DESC LIMIT ").append(MAX_WRITEUPS);
        return jdbcTemplate.query(sql.toString(), (rs, n) -> {
            TeamProfileVO.WriteupBrief b = new TeamProfileVO.WriteupBrief();
            b.setId(rs.getLong("id"));
            b.setTitle(rs.getString("title"));
            b.setContestId(rs.getLong("contest_id"));
            b.setContestName(rs.getString("contest_name"));
            b.setQuestionId(rs.getLong("question_id"));
            b.setQuestionTitle(rs.getString("question_title"));
            b.setCategory(rs.getString("category"));
            b.setUserId(rs.getLong("user_id"));
            String nick = rs.getString("nickname");
            b.setAuthor(nick == null || nick.isBlank() ? rs.getString("username") : nick);
            b.setViewCount(rs.getInt("view_count"));
            b.setLikeCount(rs.getInt("like_count"));
            b.setCreatedAt(ts(rs, "created_at"));
            return b;
        }, args.toArray());
    }

    // ---------- 组装 ----------

    private TeamProfileVO.ContestHistory toHistory(ContestRow c, List<SolveRow> teamSolves) {
        TeamProfileVO.ContestHistory h = new TeamProfileVO.ContestHistory();
        h.setContestTeamId(c.contestTeamId);
        h.setContestId(c.contestId);
        h.setContestName(c.contestName);
        h.setContestCode(c.contestCode);
        h.setType(c.type);
        h.setContestStatus(c.contestStatus);
        h.setEnrollStatus(c.enrollStatus);
        h.setStartAt(c.startAt);
        h.setEndAt(c.endAt);
        h.setMemberCount(c.memberCount);
        h.setOrganizer(c.organizer);
        h.setRank(c.rank > 0 ? c.rank : null);
        h.setScore(c.score);
        h.setSolved(teamSolves.size());
        h.setFirstBloods((int) teamSolves.stream().filter(s -> s.bloodRank == 1).count());
        // 冗余分数可能与明细不同步，以明细汇总为准
        long computed = teamSolves.stream().mapToLong(s -> s.score).sum();
        if (computed > 0) h.setScore(computed);
        return h;
    }

    private List<TeamProfileVO.Award> awardsOf(List<TeamProfileVO.ContestHistory> history) {
        List<TeamProfileVO.Award> out = new ArrayList<>();
        for (TeamProfileVO.ContestHistory h : history) {
            boolean settled = "FINISHED".equals(h.getContestStatus()) || "ARCHIVED".equals(h.getContestStatus());
            Integer rank = h.getRank();
            if (!settled || rank == null || rank > 3) continue;
            TeamProfileVO.Award a = new TeamProfileVO.Award();
            a.setContestId(h.getContestId());
            a.setContestName(h.getContestName());
            a.setType(h.getType());
            a.setRank(rank);
            a.setLevel(switch (rank) {
                case 1 -> "冠军";
                case 2 -> "亚军";
                default -> "季军";
            });
            a.setScore(h.getScore());
            a.setEndAt(h.getEndAt());
            out.add(a);
        }
        out.sort((x, y) -> {
            int cmp = Integer.compare(x.getRank(), y.getRank());
            return cmp != 0 ? cmp : y.getEndAt() == null ? -1 : x.getEndAt() == null ? 1 : y.getEndAt().compareTo(x.getEndAt());
        });
        return out;
    }

    private void fillMemberStats(List<TeamProfileVO.MemberStat> roster, List<SolveRow> solves) {
        Map<Long, List<SolveRow>> byUser = new HashMap<>();
        for (SolveRow s : solves) byUser.computeIfAbsent(s.userId, k -> new ArrayList<>()).add(s);
        for (TeamProfileVO.MemberStat m : roster) {
            List<SolveRow> mine = byUser.getOrDefault(m.getUserId(), List.of());
            m.setSolves(mine.size());
            m.setScore(mine.stream().mapToLong(s -> s.score).sum());
            m.setFirstBloods((int) mine.stream().filter(s -> s.bloodRank == 1).count());
            m.setContests((int) mine.stream().map(s -> s.contestId).distinct().count());
            m.setLastSolveAt(mine.stream().map(s -> s.solvedAt).filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo).orElse(null));
            mine.forEach(s -> {
                if (s.category != null && !s.category.isBlank()) {
                    m.getCategories().merge(s.category, 1, Integer::sum);
                }
                if (s.difficulty != null && !s.difficulty.isBlank()) {
                    m.getDifficulties().merge(s.difficulty, 1, Integer::sum);
                }
            });
        }
        roster.sort((a, b) -> Long.compare(b.getScore(), a.getScore()));
    }

    private Map<String, Integer> aggregate(List<SolveRow> solves, boolean byCategory) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (SolveRow s : solves) {
            String key = byCategory ? s.category : s.difficulty;
            if (key == null || key.isBlank()) continue;
            out.merge(key, 1, Integer::sum);
        }
        return out;
    }

    private TeamProfileVO.Stats stats(TeamProfileVO vo, List<SolveRow> solves) {
        TeamProfileVO.Stats st = new TeamProfileVO.Stats();
        List<TeamProfileVO.ContestHistory> history = vo.getHistory();
        st.setContests(history.size());
        st.setRunningContests((int) history.stream()
                .filter(h -> "RUNNING".equals(h.getContestStatus())).count());
        st.setTotalSolves(solves.size());
        st.setTotalScore(solves.stream().mapToLong(s -> s.score).sum());
        st.setTotalFirstBloods((int) solves.stream().filter(s -> s.bloodRank == 1).count());
        st.setWriteups(vo.getWriteups().size());
        st.setMemberCount(vo.getRoster().size());
        history.stream()
                .filter(h -> h.getRank() != null && h.getRank() > 0)
                .filter(h -> "FINISHED".equals(h.getContestStatus()) || "ARCHIVED".equals(h.getContestStatus()))
                .min((a, b) -> Integer.compare(a.getRank(), b.getRank()))
                .ifPresent(h -> {
                    st.setBestRank(h.getRank());
                    st.setBestContest(h.getContestName());
                });
        return st;
    }

    private static String placeholders(int n) {
        return String.join(",", Collections.nCopies(n, "?"));
    }

    private static LocalDateTime ts(ResultSet rs, String column) throws SQLException {
        // 直接按 LocalDateTime 读取：getTimestamp 会按 JVM 时区（容器为 UTC）换算，导致时间偏移 8 小时
        return rs.getObject(column, LocalDateTime.class);
    }

    /** 参赛记录原始行 */
    private static final class ContestRow {
        Long contestTeamId;
        Long contestId;
        String contestName;
        String contestCode;
        String type;
        String contestStatus;
        String enrollStatus;
        LocalDateTime startAt;
        LocalDateTime endAt;
        String organizer;
        int memberCount;
        int rank;
        long score;
    }

    /** 解题明细原始行 */
    private static final class SolveRow {
        Long contestId;
        Long contestTeamId;
        Long userId;
        Long questionId;
        String questionTitle;
        String category;
        String difficulty;
        long score;
        LocalDateTime solvedAt;
        int bloodRank;
    }
}
