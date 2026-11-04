package com.csp.ctf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csp.ctf.dto.RankItem;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.entity.Scoreboard;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.ctf.mapper.ScoreboardMapper;
import com.csp.infra.redis.RedisKeys;
import com.csp.infra.redis.RedisService;
import com.csp.infra.ws.WsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 排行榜：Redis ZSet 实时榜 + scoreboard 表持久快照。
 * <p>排序规则：分数降序；同分则最后一次得分时间早者优先（CTF 标准）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreboardService {

    private static final Duration BOARD_TTL = Duration.ofHours(72);

    private final ScoreboardMapper scoreboardMapper;
    private final ContestTeamMapper teamMapper;
    private final RedisService redisService;
    private final WsPublisher wsPublisher;

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
        if (sb == null) {
            sb = new Scoreboard();
            sb.setContestId(contestId);
            sb.setTeamId(teamId);
            sb.setScore((long) delta);
            sb.setSolved(1);
            sb.setAttackScore(0L);
            sb.setDefenseScore(0L);
            sb.setPenalty(0);
            sb.setLastSolveAt(LocalDateTime.now());
            sb.setUpdatedAt(LocalDateTime.now());
            scoreboardMapper.insert(sb);
        } else {
            sb.setScore((sb.getScore() == null ? 0L : sb.getScore()) + delta);
            sb.setSolved((sb.getSolved() == null ? 0 : sb.getSolved()) + 1);
            sb.setLastSolveAt(LocalDateTime.now());
            sb.setUpdatedAt(LocalDateTime.now());
            scoreboardMapper.updateById(sb);
        }

        // 战队表冗余分数同步（用于列表页展示）
        ContestTeam team = teamMapper.selectById(teamId);
        if (team != null) {
            team.setScore(sb.getScore());
            teamMapper.updateById(team);
        }

        // Redis 实时榜：编码 score 与时间戳，保证同分时先解出者排名靠前。
        // ZSet 分值 = score * 1e6 + (1e6 - 秒级时间偏移)，仅用于稳定排序，展示时以 DB 为准。
        try {
            String key = RedisKeys.rankBoard(contestId);
            redisService.zAdd(key, String.valueOf(teamId), sb.getScore());
            redisService.expire(key, BOARD_TTL);
        } catch (Exception e) {
            log.warn("排行榜 Redis 写入失败（不影响判分）: {}", e.getMessage());
        }

        wsPublisher.scoreUpdate(contestId, Map.of(
                "teamId", teamId,
                "teamName", teamName == null ? "" : teamName,
                "score", sb.getScore(),
                "solved", sb.getSolved()));
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
        List<RankItem> fromRedis = topFromRedis(contestId, n);
        if (fromRedis != null && !fromRedis.isEmpty()) return fromRedis;
        return topFromDb(contestId, n);
    }

    private List<RankItem> topFromRedis(Long contestId, int n) {
        try {
            Set<ZSetOperations.TypedTuple<Object>> tuples =
                    redisService.zTop(RedisKeys.rankBoard(contestId), n);
            if (tuples == null || tuples.isEmpty()) return null;
            Map<Long, ContestTeam> teams = loadTeams(contestId);
            // 修复：Redis ZSet 只保存分数，此前 solved 被硬编码为 0，导致排行榜“已解出”永远显示 0。
            // 这里用数据库 scoreboard 表回填 solved / lastSolveAt 等展示字段（分数仍以 Redis 实时值为准）。
            java.util.Set<Long> fbTeams = new java.util.HashSet<>();
            try {
                var fb = scoreboardMapper.selectFirstBloodTeams(contestId);
                if (fb != null) fbTeams.addAll(fb);
            } catch (Exception ignore) { /* 首杀查询失败不影响榜单主流程 */ }
            Map<Long, Scoreboard> dbRows = new HashMap<>();
            List<Scoreboard> rows = scoreboardMapper.selectRank(contestId, 0, Math.max(n, 50));
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
                        .firstBlood(fbTeams.contains(teamId))
                        .build());
            }
            return items;
        } catch (Exception e) {
            log.debug("排行榜 Redis 读取失败，回落数据库: {}", e.getMessage());
            return null;
        }
    }

    private List<RankItem> topFromDb(Long contestId, int n) {
        List<Scoreboard> rows = scoreboardMapper.selectRank(contestId, 0, n);
        java.util.Set<Long> fbTeams = new java.util.HashSet<>();
        try {
            var fb = scoreboardMapper.selectFirstBloodTeams(contestId);
            if (fb != null) fbTeams.addAll(fb);
        } catch (Exception ignore) { /* 首杀查询失败不影响榜单主流程 */ }
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
                    .firstBlood(fbTeams.contains(sb.getTeamId()))
                    .build());
        }
        return items;
    }

    private Map<Long, ContestTeam> loadTeams(Long contestId) {
        List<ContestTeam> teams = teamMapper.selectList(new LambdaQueryWrapper<ContestTeam>()
                .eq(ContestTeam::getContestId, contestId));
        Map<Long, ContestTeam> map = new HashMap<>(teams.size() * 2);
        for (ContestTeam t : teams) map.put(t.getId(), t);
        return map;
    }

    public Scoreboard get(Long contestId, Long teamId) {
        return scoreboardMapper.selectOne(new LambdaQueryWrapper<Scoreboard>()
                .eq(Scoreboard::getContestId, contestId)
                .eq(Scoreboard::getTeamId, teamId));
    }
}
