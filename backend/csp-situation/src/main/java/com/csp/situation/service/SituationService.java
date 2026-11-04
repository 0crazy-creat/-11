package com.csp.situation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.csp.awd.dto.TargetVO;
import com.csp.awd.service.AwdTargetService;
import com.csp.awd.service.AwdScoreService;
import com.csp.awd.dto.ScoreVO;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.dto.RankItem;
import com.csp.ctf.entity.ContestTeam;
import com.csp.ctf.mapper.ContestMapper;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.ctf.mapper.SubmissionMapper;
import com.csp.ctf.service.ScoreboardService;
import com.csp.ctf.entity.Contest;
import com.csp.ctf.entity.Submission;
import com.csp.situation.dto.OverviewVO;
import com.csp.situation.dto.ScreenVO;
import com.csp.situation.dto.TeamHomeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 态势感知服务：聚合 CTF 排行/提交 与 AWD 靶机数据，供大屏与战队主页消费。
 *
 * <p>本服务只做聚合与派生，不写业务表；所有底层能力复用 csp-ctf / csp-awd 的公开服务，
 * 保证排行榜与靶机状态单一数据源。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SituationService {

    private static final int DEFAULT_TOP_N = 10;
    private static final int MAX_TOP_N = 100;

    private final ContestMapper contestMapper;
    private final ContestTeamMapper teamMapper;
    private final SubmissionMapper submissionMapper;
    private final ScoreboardService scoreboardService;
    private final AwdTargetService targetService;
    private final AwdScoreService awdScoreService;

    // ---------------- 总览 ----------------

    public OverviewVO overview(Long contestId) {
        Contest contest = requireContest(contestId);
        List<ContestTeam> teams = teamsOf(contestId);
        List<RankItem> top = ranking(contestId, 1);
        RankItem leader = top.isEmpty() ? null : top.get(0);

        int solvedTotal = 0;
        for (RankItem it : ranking(contestId, MAX_TOP_N)) {
            solvedTotal += it.getSolved();
        }

        List<TargetVO> targets = safeTargets(contestId, null);
        int healthy = 0;
        for (TargetVO t : targets) {
            if (Integer.valueOf(1).equals(t.getHealth())) healthy++;
        }

        return OverviewVO.builder()
                .contestId(contestId)
                .contestName(contest.getName())
                .teamCount(teams.size())
                .targetCount(targets.size())
                .healthyCount(healthy)
                .solvedTotal(solvedTotal)
                .submitTotal(countSubmissions(contestId))
                .leadingTeam(leader == null ? "" : leader.getTeamName())
                .topScore(leader == null ? 0L : leader.getScore())
                .build();
    }

    // ---------------- 排行榜 ----------------

    public List<RankItem> ranking(Long contestId, Integer topN) {
        Contest contest = requireContest(contestId);
        int n = topN == null ? DEFAULT_TOP_N : Math.min(Math.max(topN, 1), MAX_TOP_N);
        // AWD/AWDP 的分数存在 csp:awd:total:{contestId}，与 CTF 的 csp:rank:{contestId} 是两套数据源，
        // 必须按赛事类型分流，否则 AWD 大屏/排行榜永远为空。
        if (isAwdType(contest.getType())) {
            List<RankItem> awd = awdRanking(contestId, n);
            if (!awd.isEmpty()) return awd;
        }
        return scoreboardService.topN(contestId, n);
    }

    private static boolean isAwdType(String type) {
        return "AWD".equalsIgnoreCase(type) || "AWDP".equalsIgnoreCase(type);
    }

    /** 把 AWD 实时榜（Redis）映射为通用 RankItem，供大屏/排行榜复用。 */
    private List<RankItem> awdRanking(Long contestId, int n) {
        try {
            List<ScoreVO> board = awdScoreService.leaderboard(contestId, n);
            if (board == null || board.isEmpty()) return List.of();
            Map<Long, ContestTeam> teams = new LinkedHashMap<>();
            for (ContestTeam t : teamsOf(contestId)) teams.put(t.getId(), t);
            List<RankItem> items = new ArrayList<>();
            int rank = 1;
            for (ScoreVO vo : board) {
                ContestTeam team = teams.get(vo.getTeamId());
                items.add(RankItem.builder()
                        .rank(rank++)
                        .teamId(vo.getTeamId())
                        .teamName(team == null ? String.valueOf(vo.getTeamId()) : team.getName())
                        .score(vo.getScore())
                        .solved((int) vo.getAttackSuccessCount())
                        .build());
            }
            return items;
        } catch (Exception e) {
            log.warn("AWD 排行榜获取失败 contestId={}: {}", contestId, e.getMessage());
            return List.of();
        }
    }

    // ---------------- 战队主页 ----------------

    public TeamHomeVO teamHome(Long contestId, Long teamId) {
        requireContest(contestId);
        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null || !contestId.equals(team.getContestId())) {
            throw BizException.notFound("战队");
        }
        List<RankItem> all = ranking(contestId, MAX_TOP_N);
        RankItem mine = null;
        int idx = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getTeamId().equals(teamId)) {
                mine = all.get(i);
                idx = i;
                break;
            }
        }
        List<RankItem> nearby = new ArrayList<>();
        if (idx >= 0) {
            int from = Math.max(0, idx - 2);
            int to = Math.min(all.size(), idx + 3);
            nearby.addAll(all.subList(from, to));
        }
        return TeamHomeVO.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .orgCode(team.getOrgCode())
                .logo(team.getLogo())
                .status(team.getStatus())
                .members(team.getMembers() == null ? 0 : team.getMembers())
                .rank(mine == null ? 0 : mine.getRank())
                .score(mine == null ? 0L : mine.getScore())
                .solved(mine == null ? 0 : mine.getSolved())
                .nearby(nearby)
                .targets(safeTargets(contestId, teamId))
                .build();
    }

    // ---------------- 3D 大屏 ----------------

    public ScreenVO screen(Long contestId, Integer topN) {
        int n = topN == null ? DEFAULT_TOP_N : Math.min(Math.max(topN, 1), MAX_TOP_N);
        List<RankItem> top = ranking(contestId, n);
        return ScreenVO.builder()
                .overview(overview(contestId))
                .top(top)
                .scoreDistribution(distribution(top))
                .targets(safeTargets(contestId, null))
                .serverTime(System.currentTimeMillis())
                .build();
    }

    // ---------------- 靶机监控 ----------------

    public List<TargetVO> targets(Long contestId, Long teamId) {
        requireContest(contestId);
        return safeTargets(contestId, teamId);
    }

    // ---------------- 内部 ----------------

    private Contest requireContest(Long contestId) {
        Contest c = contestMapper.selectById(contestId);
        if (c == null) throw BizException.notFound("赛事");
        return c;
    }

    private List<ContestTeam> teamsOf(Long contestId) {
        return teamMapper.selectList(Wrappers.<ContestTeam>lambdaQuery()
                .eq(ContestTeam::getContestId, contestId)
                .eq(ContestTeam::getStatus, "APPROVED"));
    }

    /** 靶机查询失败不应拖垮整个大屏，降级为空列表。 */
    private List<TargetVO> safeTargets(Long contestId, Long teamId) {
        try {
            if (teamId != null) return targetService.teamTargets(contestId, teamId);
            return targetService.listByContest(contestId, null);
        } catch (Exception e) {
            log.warn("靶机数据获取失败 contestId={} teamId={}: {}", contestId, teamId, e.getMessage());
            return List.of();
        }
    }

    private int countSubmissions(Long contestId) {
        try {
            Long c = submissionMapper.selectCount(Wrappers.<Submission>lambdaQuery()
                    .eq(Submission::getContestId, contestId));
            return c == null ? 0 : c.intValue();
        } catch (Exception e) {
            log.warn("提交数统计失败 contestId={}: {}", contestId, e.getMessage());
            return 0;
        }
    }

    /** 分数分布：按 100 分一档分桶。 */
    private Map<String, Long> distribution(List<RankItem> items) {
        Map<String, Long> buckets = new LinkedHashMap<>();
        buckets.put("0-100", 0L);
        buckets.put("100-300", 0L);
        buckets.put("300-600", 0L);
        buckets.put("600+", 0L);
        for (RankItem it : items) {
            long s = it.getScore();
            String key;
            if (s < 100) key = "0-100";
            else if (s < 300) key = "100-300";
            else if (s < 600) key = "300-600";
            else key = "600+";
            buckets.merge(key, 1L, Long::sum);
        }
        return buckets;
    }

    // ---------------- 前端兼容支持 ----------------

    /** 选一个适合大屏展示的赛事：优先 RUNNING，其次 SIGNUP/PENDING，最后取最近创建的。 */
    public Long pickDisplayContestId() {
        for (String st : new String[]{"RUNNING", "SIGNUP", "PENDING"}) {
            Contest c = contestMapper.selectOne(Wrappers.<Contest>lambdaQuery()
                    .eq(Contest::getStatus, st)
                    .orderByDesc(Contest::getCreatedAt)
                    .last("LIMIT 1"));
            if (c != null) return c.getId();
        }
        Contest latest = contestMapper.selectOne(Wrappers.<Contest>lambdaQuery()
                .orderByDesc(Contest::getCreatedAt).last("LIMIT 1"));
        if (latest != null) return latest.getId();
        throw BizException.notFound("赛事");
    }

    /** 按 teamId 反查所属赛事后返回战队主页。 */
    public TeamHomeVO teamHomeByTeamId(Long teamId) {
        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null) throw BizException.notFound("战队");
        return teamHome(team.getContestId(), teamId);
    }
}
