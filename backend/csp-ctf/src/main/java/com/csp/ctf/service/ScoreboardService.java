package com.csp.ctf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.ctf.dto.BoardDetailVO;
import com.csp.ctf.dto.ChallengeMeta;
import com.csp.ctf.dto.FirstBloodItem;
import com.csp.ctf.dto.FirstBloodRow;
import com.csp.ctf.dto.RankItem;
import com.csp.ctf.dto.SolveCell;
import com.csp.ctf.dto.StageBoardRow;
import com.csp.ctf.dto.StageVO;
import com.csp.ctf.dto.TimelineVO;
import com.csp.ctf.entity.Contest;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.Scoreboard;
import com.csp.ctf.mapper.ContestMapper;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.ctf.mapper.ScoreboardMapper;
import com.csp.infra.redis.RedisKeys;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 排行榜：Redis ZSet 实时榜 + scoreboard 表持久快照。
 * <p>排序规则：分数降序；同分则最后一次得分时间早者优先（CTF 标准）。</p>
 *
 * <p>三类榜单口径（对齐 GZCTF 的 scoreboard 与得分时间线）：</p>
 * <ul>
 *   <li>总分榜：实时，Redis 优先、DB 兜底；</li>
 *   <li>阶段榜：按比赛时间窗切片，回放「截至阶段末」的累计榜，得分取提交当时的分值，
 *       因此不会被后续动态衰减扭曲；</li>
 *   <li>首杀榜：每题最早的一次正确提交，同时给出该题的解出队伍数。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreboardService {

    private static final Duration BOARD_TTL = Duration.ofHours(72);
    private static final int MAX_TOP_N = 500;
    private static final int MAX_STAGES = 12;
    private static final int MAX_TRACES = 20;
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MD_HM = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private final ScoreboardMapper scoreboardMapper;
    private final ContestTeamMapper teamMapper;
    private final ContestMapper contestMapper;
    private final RedisService redisService;
    private final WsPublisher wsPublisher;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 记录一次正确得分并刷新榜单。
     *
     * @return 该战队最新累计分数
     */
    @Transactional(rollbackFor = Exception.class)
    public long applyScore(Long contestId, Long teamId, String teamName, int delta) {
        Scoreboard sb = scoreboardMapper.selectOne(new LambdaQueryWrapper<Scoreboard>()
                .eq(Scoreboard::getContestId, contestId)
                .eq(Scoreboard::getTeamId, teamId));
        LocalDateTime now = LocalDateTime.now();
        if (sb == null) {
            sb = new Scoreboard();
            sb.setContestId(contestId);
            sb.setTeamId(teamId);
            sb.setScore((long) delta);
            sb.setSolved(1);
            sb.setAttackScore(0L);
            sb.setDefenseScore(0L);
            sb.setPenalty(0);
            sb.setLastSolveAt(now);
            sb.setUpdatedAt(now);
            scoreboardMapper.insert(sb);
        } else {
            sb.setScore((sb.getScore() == null ? 0L : sb.getScore()) + delta);
            sb.setLastSolveAt(now);
            sb.setUpdatedAt(now);
            // 解题数按去重后的题目数维护：重复刷同一题不再累加，与阶段榜、解题矩阵口径一致。
            int distinct = scoreboardMapper.countSolvedQuestions(contestId, teamId);
            sb.setSolved(Math.max(distinct, sb.getSolved() == null ? 0 : sb.getSolved()));
            scoreboardMapper.updateById(sb);
        }

        // 战队表冗余分数同步（用于列表页展示）
        ContestTeam team = teamMapper.selectById(teamId);
        if (team != null) {
            team.setScore(sb.getScore());
            teamMapper.updateById(team);
        }

        // Redis 实时榜只存分数；solved/一血等展示字段在读取时由 DB 回填。
        try {
            String key = RedisKeys.rankBoard(contestId);
            redisService.zAdd(key, String.valueOf(teamId), sb.getScore());
            redisService.expire(key, BOARD_TTL);
        } catch (Exception e) {
            log.warn("排行榜 Redis 写入失败（不影响判分）: {}", e.getMessage());
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("teamId", teamId);
        payload.put("teamName", teamName == null ? "" : teamName);
        payload.put("score", sb.getScore());
        payload.put("solved", sb.getSolved() == null ? 0 : sb.getSolved());
        payload.put("delta", delta);
        wsPublisher.scoreUpdate(contestId, payload);
        return sb.getScore();
    }

    /** 刷新排名号并回写（定时/结束前调用）。 */
    @Transactional(rollbackFor = Exception.class)
    public void refreshRanks(Long contestId) {
        List<Scoreboard> all = scoreboardMapper.selectRank(contestId, 0, 10000);
        int rank = 1;
        for (Scoreboard sb : all) {
            sb.setRankNo(rank++);
            scoreboardMapper.updateById(sb);
            ContestTeam team = teamMapper.selectById(sb.getTeamId());
            if (team != null) {
                team.setRank(sb.getRankNo());
                team.setScore(sb.getScore());
                teamMapper.updateById(team);
            }
        }
    }

    /** 取排行榜 TopN（实时 Redis 优先，失败回落 DB）。 */
    public List<RankItem> topN(Long contestId, int n) {
        List<RankItem> items = topFromRedis(contestId, n);
        if (items == null || items.isEmpty()) items = topFromDb(contestId, n);
        return decorate(contestId, items, null);
    }

    /**
     * 榜单明细：一次取齐表头、榜单行、解题矩阵与阶段划分。
     *
     * @param asOf 回放截止时刻，null 表示实时总分榜
     */
    public BoardDetailVO detail(Long contestId, int n, LocalDateTime asOf) {
        List<RankItem> items;
        String source;
        List<SolveCell> cells;
        if (asOf == null) {
            List<RankItem> redis = topFromRedis(contestId, n);
            boolean realtime = redis != null && !redis.isEmpty();
            items = realtime ? redis : topFromDb(contestId, n);
            source = realtime ? "REDIS" : "DB";
            cells = scoreboardMapper.selectSolveCells(contestId);
        } else {
            items = stageRows(contestId, asOf, n);
            source = "REPLAY";
            cells = scoreboardMapper.selectSolveCellsAsOf(contestId, asOf);
        }
        decorate(contestId, items, asOf);

        Map<Long, Integer> solvedPerQuestion = new HashMap<>();
        for (SolveCell c : cells) solvedPerQuestion.merge(c.getQuestionId(), 1, Integer::sum);
        return BoardDetailVO.builder()
                .ranks(items)
                .challenges(challenges(contestId, solvedPerQuestion))
                .cells(cells)
                .stages(stages(contestId, defaultSlices(contestId)))
                .snapshotAt(asOf == null ? LocalDateTime.now() : asOf)
                .source(source)
                .build();
    }

    /** 首杀榜：每题最早的一次正确提交 + 解题者 + 题目热度。 */
    public List<FirstBloodItem> firstBloodBoard(Long contestId) {
        List<FirstBloodRow> rows = scoreboardMapper.selectFirstBloodDetail(contestId);
        if (rows.isEmpty()) return List.of();
        Map<Long, ContestTeam> teams = loadTeams(contestId);
        Map<Long, ChallengeMeta> meta = new HashMap<>();
        for (ChallengeMeta m : challenges(contestId, solveCounts(contestId))) meta.put(m.getQuestionId(), m);
        Map<Long, String> nick = nicknames(rows.stream().map(FirstBloodRow::getUserId).toList());

        List<FirstBloodItem> out = new ArrayList<>(rows.size());
        for (FirstBloodRow r : rows) {
            ContestTeam t = teams.get(r.getTeamId());
            ChallengeMeta q = meta.get(r.getQuestionId());
            out.add(FirstBloodItem.builder()
                    .questionId(r.getQuestionId())
                    .title(q == null ? "题目 " + r.getQuestionId() : q.getTitle())
                    .category(q == null ? null : q.getCategory())
                    .difficulty(q == null ? null : q.getDifficulty())
                    .score(q == null ? null : q.getScore())
                    .teamId(r.getTeamId())
                    .globalTeamId(t == null ? null : t.getTeamId())
                    .teamName(t == null ? "" : t.getName())
                    .logo(t == null ? null : t.getLogo())
                    .userId(r.getUserId())
                    .userNickname(r.getUserId() == null ? null : nick.get(r.getUserId()))
                    .solvedAt(r.getSolvedAt())
                    .solveTeamCount(q == null ? 0 : q.getSolvedCount())
                    .build());
        }
        return out;
    }

    /**
     * 阶段切分：按比赛时间窗等分，末阶段随当前时间滚动。
     *
     * @param slices 期望阶段数，clamp 到 1..12
     */
    public List<StageVO> stages(Long contestId, int slices) {
        List<SolveCell> cells = scoreboardMapper.selectSolveCells(contestId);
        Contest contest = contestMapper.selectById(contestId);
        LocalDateTime from = resolveFrom(contest, cells);
        LocalDateTime to = resolveTo(contest, cells, from);
        int count = Math.max(1, Math.min(slices, MAX_STAGES));
        long step = Math.max(1, Duration.between(from, to).toMillis() / count);
        boolean longSpan = Duration.between(from, to).toHours() >= 24;
        DateTimeFormatter fmt = longSpan ? MD_HM : HM;
        LocalDateTime now = LocalDateTime.now();

        List<StageVO> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            LocalDateTime start = i == 0 ? from : from.plusNanos(step * 1_000_000L * i);
            LocalDateTime end = (i == count - 1) ? to : start.plusNanos(step * 1_000_000L);
            if (!end.isAfter(start)) end = start.plusMinutes(1);
            final LocalDateTime s = start;
            final LocalDateTime e = end;
            int solves = (int) cells.stream()
                    .filter(c -> !c.getSolvedAt().isBefore(s) && c.getSolvedAt().isBefore(e))
                    .count();
            out.add(StageVO.builder()
                    .index(i + 1)
                    .label("阶段 " + (i + 1) + " · " + fmt.format(s) + "-" + fmt.format(e))
                    .startAt(s)
                    .endAt(e)
                    .solves(solves)
                    .current(!now.isBefore(s) && now.isBefore(e))
                    .build());
        }
        if (!out.isEmpty() && !now.isBefore(out.get(out.size() - 1).getStartAt())) {
            out.get(out.size() - 1).setCurrent(true);
        }
        return out;
    }

    /** 指定阶段的累计榜。 */
    public List<RankItem> stageBoard(Long contestId, int stageIndex, int slices, int n) {
        StageVO stage = pickStage(contestId, stageIndex, slices);
        if (stage == null) return List.of();
        List<RankItem> items = stageRows(contestId, stage.getEndAt(), n);
        return decorate(contestId, items, stage.getEndAt());
    }

    /**
     * 分数趋势：与阶段榜共用时间轴，便于「趋势 + 阶段回放」联动。
     *
     * @param traceCount 曲线条数（按最终得分取前 N）
     */
    public TimelineVO timeline(Long contestId, int traceCount, int slices) {
        List<StageVO> stages = stages(contestId, slices);
        List<SolveCell> cells = scoreboardMapper.selectSolveCells(contestId);
        Map<Long, ContestTeam> teams = loadTeams(contestId);
        Map<Long, Long> totals = new HashMap<>();
        for (SolveCell c : cells) totals.merge(c.getTeamId(), c.getScore() == null ? 0L : c.getScore(), Long::sum);

        List<Long> topTeams = totals.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(Math.max(1, Math.min(traceCount, MAX_TRACES)))
                .map(Map.Entry::getKey)
                .toList();

        DateTimeFormatter labelFmt = !stages.isEmpty()
                && Duration.between(stages.get(0).getStartAt(), stages.get(stages.size() - 1).getEndAt()).toHours() >= 24
                ? MD_HM : HM;
        List<String> labels = stages.stream().map(s -> labelFmt.format(s.getEndAt())).toList();
        List<TimelineVO.Trace> traces = new ArrayList<>(topTeams.size());
        for (Long teamId : topTeams) {
            ContestTeam t = teams.get(teamId);
            List<Long> scores = new ArrayList<>(stages.size());
            for (StageVO s : stages) {
                final LocalDateTime end = s.getEndAt();
                long acc = cells.stream()
                        .filter(c -> Objects.equals(c.getTeamId(), teamId) && !c.getSolvedAt().isAfter(end))
                        .mapToLong(c -> c.getScore() == null ? 0L : c.getScore()).sum();
                scores.add(acc);
            }
            traces.add(TimelineVO.Trace.builder()
                    .teamId(teamId)
                    .teamName(t == null ? String.valueOf(teamId) : t.getName())
                    .scores(scores)
                    .build());
        }
        return TimelineVO.builder().labels(labels).traces(traces).build();
    }

    /** 默认阶段数：按比赛时长每 2 小时一段，clamp 3..8。 */
    public int defaultSlices(Long contestId) {
        Contest c = contestMapper.selectById(contestId);
        if (c == null || c.getStartAt() == null) return 6;
        LocalDateTime end = c.getEndAt() == null ? LocalDateTime.now() : c.getEndAt();
        long hours = Math.max(1, Duration.between(c.getStartAt(), end).toHours());
        return (int) Math.max(3, Math.min(8, Math.ceil(hours / 2.0)));
    }

    public Scoreboard get(Long contestId, Long teamId) {
        return scoreboardMapper.selectOne(new LambdaQueryWrapper<Scoreboard>()
                .eq(Scoreboard::getContestId, contestId)
                .eq(Scoreboard::getTeamId, teamId));
    }

    // ---------- 内部实现 ----------

    private StageVO pickStage(Long contestId, int stageIndex, int slices) {
        List<StageVO> stages = stages(contestId, slices);
        if (stages.isEmpty()) return null;
        return stages.stream().filter(s -> s.getIndex() == stageIndex).findFirst()
                .orElse(stages.get(stages.size() - 1));
    }

    private List<RankItem> stageRows(Long contestId, LocalDateTime asOf, int n) {
        List<StageBoardRow> rows = scoreboardMapper.selectBoardAsOf(contestId, asOf, clamp(n));
        Map<Long, ContestTeam> teams = loadTeams(contestId);
        List<RankItem> items = new ArrayList<>(rows.size());
        int rank = 1;
        for (StageBoardRow r : rows) {
            ContestTeam t = teams.get(r.getTeamId());
            items.add(RankItem.builder()
                    .rank(rank++)
                    .teamId(r.getTeamId())
                    .teamName(t == null ? "" : t.getName())
                    .score(r.getScore() == null ? 0L : r.getScore())
                    .solved(r.getSolved() == null ? 0 : r.getSolved())
                    .lastSolveAt(r.getLastSolveAt())
                    .build());
        }
        return items;
    }

    private List<RankItem> topFromRedis(Long contestId, int n) {
        try {
            Set<ZSetOperations.TypedTuple<Object>> tuples =
                    redisService.zTop(RedisKeys.rankBoard(contestId), clamp(n));
            if (tuples == null || tuples.isEmpty()) return null;
            Map<Long, ContestTeam> teams = loadTeams(contestId);
            // Redis ZSet 只保存分数，solved / lastSolveAt 由 scoreboard 表回填，避免「已解出」恒为 0。
            Map<Long, Scoreboard> dbRows = new HashMap<>();
            List<Scoreboard> rows = scoreboardMapper.selectRank(contestId, 0, clamp(n));
            if (rows != null) for (Scoreboard sb : rows) dbRows.put(sb.getTeamId(), sb);

            List<RankItem> items = new ArrayList<>();
            int rank = 1;
            for (ZSetOperations.TypedTuple<Object> t : tuples) {
                Long teamId = Long.valueOf(String.valueOf(t.getValue()));
                ContestTeam team = teams.get(teamId);
                Scoreboard db = dbRows.get(teamId);
                items.add(RankItem.builder()
                        .rank(rank++)
                        .teamId(teamId)
                        .teamName(team == null ? "" : team.getName())
                        .score(t.getScore() == null ? 0L : Math.round(t.getScore()))
                        .solved(db == null || db.getSolved() == null ? 0 : db.getSolved())
                        .lastSolveAt(db == null ? null : db.getLastSolveAt())
                        .build());
            }
            return items;
        } catch (Exception e) {
            log.debug("排行榜 Redis 读取失败，回落数据库: {}", e.getMessage());
            return null;
        }
    }

    private List<RankItem> topFromDb(Long contestId, int n) {
        List<Scoreboard> rows = scoreboardMapper.selectRank(contestId, 0, clamp(n));
        Map<Long, ContestTeam> teams = loadTeams(contestId);
        List<RankItem> items = new ArrayList<>(rows.size());
        int rank = 1;
        for (Scoreboard sb : rows) {
            ContestTeam team = teams.get(sb.getTeamId());
            items.add(RankItem.builder()
                    .rank(rank++)
                    .teamId(sb.getTeamId())
                    .teamName(team == null ? "" : team.getName())
                    .score(sb.getScore() == null ? 0L : sb.getScore())
                    .solved(sb.getSolved() == null ? 0 : sb.getSolved())
                    .lastSolveAt(sb.getLastSolveAt())
                    .build());
        }
        return items;
    }

    /** 补齐 logo / 院校 / 全局战队 ID / 一血数，供首杀特殊标识与战队主页跳转使用。 */
    private List<RankItem> decorate(Long contestId, List<RankItem> items, LocalDateTime asOf) {
        if (items.isEmpty()) return items;
        Map<Long, ContestTeam> teams = loadTeams(contestId);
        Map<Long, Integer> fbCount = firstBloodCounts(contestId, asOf);
        for (RankItem it : items) {
            ContestTeam team = teams.get(it.getTeamId());
            if (team != null) {
                it.setLogo(team.getLogo());
                it.setOrgName(team.getOrgCode());
                it.setGlobalTeamId(team.getTeamId());
            }
            int fb = fbCount.getOrDefault(it.getTeamId(), 0);
            it.setFirstBloodCount(fb);
            it.setFirstBlood(fb > 0);
        }
        return items;
    }

    private Map<Long, Integer> firstBloodCounts(Long contestId, LocalDateTime asOf) {
        List<FirstBloodRow> rows = asOf == null
                ? scoreboardMapper.selectFirstBloodDetail(contestId)
                : scoreboardMapper.selectFirstBloodDetailAsOf(contestId, asOf);
        Map<Long, Integer> counts = new HashMap<>();
        for (FirstBloodRow r : rows) counts.merge(r.getTeamId(), 1, Integer::sum);
        return counts;
    }

    private Map<Long, Integer> solveCounts(Long contestId) {
        Map<Long, Integer> counts = new HashMap<>();
        for (SolveCell c : scoreboardMapper.selectSolveCells(contestId)) {
            counts.merge(c.getQuestionId(), 1, Integer::sum);
        }
        return counts;
    }

    /** 题目表头：题库元信息 + 实时解题人数 + 正确率分母。 */
    private List<ChallengeMeta> challenges(Long contestId, Map<Long, Integer> solvedPerQuestion) {
        List<ChallengeMeta> list = scoreboardMapper.selectContestChallenges(contestId);
        if (list == null) return List.of();
        int total = Math.max(0, scoreboardMapper.countContestTeams(contestId));
        for (ChallengeMeta m : list) {
            m.setSolvedCount(solvedPerQuestion.getOrDefault(m.getQuestionId(), 0));
            m.setTotalCount(total);
        }
        return list;
    }

    private Map<Long, ContestTeam> loadTeams(Long contestId) {
        List<ContestTeam> teams = teamMapper.selectList(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getContestId, contestId));
        Map<Long, ContestTeam> map = new LinkedHashMap<>(teams.size() * 2);
        for (ContestTeam t : teams) map.put(t.getId(), t);
        return map;
    }

    /** 批量取用户昵称，首杀榜展示解题者。 */
    private Map<Long, String> nicknames(Collection<Long> userIds) {
        List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return Collections.emptyMap();
        String in = String.join(",", Collections.nCopies(ids.size(), "?"));
        Map<Long, String> out = new HashMap<>(ids.size() * 2);
        jdbcTemplate.query("SELECT id, IFNULL(NULLIF(nickname, ''), username) AS name FROM sys_user WHERE id IN (" + in + ")",
                rs -> {
                    out.put(rs.getLong("id"), rs.getString("name"));
                }, ids.toArray());
        return out;
    }

    private LocalDateTime resolveFrom(Contest c, List<SolveCell> cells) {
        if (c != null && c.getStartAt() != null) return c.getStartAt();
        return cells.stream().map(SolveCell::getSolvedAt).filter(Objects::nonNull)
                .min(LocalDateTime::compareTo).orElse(LocalDateTime.now().minusHours(1));
    }

    private LocalDateTime resolveTo(Contest c, List<SolveCell> cells, LocalDateTime from) {
        LocalDateTime last = cells.stream().map(SolveCell::getSolvedAt).filter(Objects::nonNull)
                .max(LocalDateTime::compareTo).orElse(null);
        LocalDateTime to = LocalDateTime.now();
        if (c != null && c.getEndAt() != null && c.getEndAt().isAfter(from)) to = c.getEndAt();
        // 已结束赛事若存在超时提交，把窗口延伸到最后一解，避免阶段榜丢数据
        if (last != null && last.isAfter(to)) to = last;
        if (!to.isAfter(from)) to = from.plusMinutes(1);
        return to;
    }

    private int clamp(int n) {
        return Math.max(1, Math.min(n, MAX_TOP_N));
    }
}
