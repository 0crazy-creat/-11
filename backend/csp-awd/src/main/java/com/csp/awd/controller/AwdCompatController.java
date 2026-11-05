package com.csp.awd.controller;

import com.csp.awd.dto.RoundVO;
import com.csp.awd.dto.ScoreVO;
import com.csp.awd.dto.TargetVO;
import com.csp.awd.entity.AwdRound;
import com.csp.awd.service.AwdRoundService;
import com.csp.awd.service.AwdScoreService;
import com.csp.awd.service.AwdTargetService;
import com.csp.common.api.Result;
import com.csp.infra.redis.RedisService;
import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AWD 前端兼容接口：把前端 {@code /api/awd/contests/{contestId}/*} 形式的调用
 * 映射到既有 {@code /api/awd/score}、{@code /api/awd/rounds}、{@code /api/awd/targets} 实现，
 * 避免前端页面因路径不一致而 404/500。
 */
@Tag(name = "AWD-前端兼容", description = "按赛事聚合的只读视图，供态势/大屏页面调用")
@RestController
@RequestMapping("/api/awd/contests/{contestId}")
@RequiredArgsConstructor
public class AwdCompatController {

    private static final String AWD_P = "csp:awd:";

    private final AwdScoreService scoreService;
    private final AwdRoundService roundService;
    private final AwdTargetService targetService;
    private final RedisService redisService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Operation(summary = "赛事总览（队伍/轮次/榜单/靶机/队伍名）")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(@PathVariable Long contestId,
                                                @RequestParam(defaultValue = "10") int topN) {
        List<ScoreVO> board = scoreService.leaderboard(contestId, Math.max(topN, 50));
        List<RoundVO> rounds = roundService.listRounds(contestId);
        AwdRound cur = roundService.currentRunning(contestId);
        Map<String, Object> m = new LinkedHashMap<>();
        // 大屏需要赛事名/时间/轮次总量，csp-awd 不依赖 csp-ctf，按只读 SQL 取
        Map<String, Object> info = jdbcTemplate.queryForList(
                "SELECT name, status, start_at, end_at FROM contest WHERE id = ? AND deleted = 0", contestId)
                .stream().findFirst().orElse(Map.of());
        m.put("name", info.get("name"));
        m.put("status", info.get("status"));
        m.put("startAt", isoOf(info.get("start_at")));
        m.put("endAt", isoOf(info.get("end_at")));
        m.put("endTime", isoOf(info.get("end_at")));
        m.put("teamCount", board == null ? 0 : board.size());
        m.put("top", board);
        m.put("teamNames", teamNames(contestId));
        m.put("rounds", rounds);
        m.put("roundCount", rounds == null ? 0 : rounds.size());
        // 无进行中的轮次时展示最近一轮，避免大屏出现"第 0 轮"
        m.put("currentRound", cur != null ? cur.getRoundNo()
                : (rounds == null ? 0 : rounds.stream().mapToInt(RoundVO::getRoundNo).max().orElse(0)));
        m.put("currentRoundDetail", cur == null ? null : roundService.toVO(cur));
        m.put("targetCount", targetService.listByContest(contestId, null).size());
        // 比分历史快照（大屏"得分趋势"数据源），每次被轮询时最多 1 分钟记 1 个点
        recordTrendSnapshot(contestId, board);
        return Result.ok(m);
    }

    private static String isoOf(Object v) {
        if (v instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toString();
        return v == null ? null : v.toString();
    }

    @Operation(summary = "参赛队伍积分列表")
    @GetMapping("/teams")
    public Result<List<ScoreVO>> teams(@PathVariable Long contestId,
                                       @RequestParam(defaultValue = "50") int topN) {
        return Result.ok(scoreService.leaderboard(contestId, topN));
    }

    @Operation(summary = "赛事排行榜（别名）")
    @GetMapping("/scoreboard")
    public Result<List<ScoreVO>> scoreboard(@PathVariable Long contestId,
                                            @RequestParam(defaultValue = "50") int topN) {
        return Result.ok(scoreService.leaderboard(contestId, topN));
    }

    @Operation(summary = "轮次列表")
    @GetMapping("/rounds")
    public Result<List<RoundVO>> rounds(@PathVariable Long contestId) {
        return Result.ok(roundService.listRounds(contestId));
    }

    @Operation(summary = "靶机列表")
    @GetMapping("/targets")
    public Result<List<TargetVO>> targets(@PathVariable Long contestId,
                                          @RequestParam(required = false) Long teamId) {
        return Result.ok(targetService.listByContest(contestId, teamId));
    }

    @Operation(summary = "攻击流水（按 Redis 攻破事实还原，含首杀归属）")
    @GetMapping("/attacks")
    public Result<List<Map<String, Object>>> attacks(@PathVariable Long contestId) {
        return Result.ok(attackLog(contestId));
    }

    @Operation(summary = "比分历史快照（大屏得分趋势数据源）")
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> history(@PathVariable Long contestId,
                                                     @RequestParam(defaultValue = "240") int limit) {
        List<Object> raw = redisService.lRange("csp:awd:trend:" + contestId, -Math.min(limit, 240), -1);
        List<Map<String, Object>> out = new ArrayList<>();
        if (raw != null) {
            for (Object o : raw) {
                try {
                    out.add(objectMapper.convertValue(o, new TypeReference<Map<String, Object>>() {}));
                } catch (IllegalArgumentException ignored) {
                    // 脏数据点直接丢弃
                }
            }
        }
        return Result.ok(out);
    }

    // ------------------------------------------------------------------ 内部

    /** 队伍 ID -> 名称（只读 SQL）。 */
    private Map<String, String> teamNames(Long contestId) {
        Map<String, String> names = new LinkedHashMap<>();
        try {
            jdbcTemplate.queryForList(
                    "SELECT id, name FROM contest_team WHERE contest_id = ? AND deleted = 0", contestId)
                    .forEach(row -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> r = (Map<String, Object>) row;
                        Object id = r.get("id");
                        Object name = r.get("name");
                        if (id != null && name != null) names.put(String.valueOf(id), String.valueOf(name));
                    });
        } catch (Exception ignored) {
            // 查不到就留空，前端会回退为 "战队{teamId}"
        }
        return names;
    }

    /**
     * 攻击流水：由 Redis 攻破事实键 csp:awd:hit:{cid}:r{round}:{atk}:{vic}:{qid}
     * 与首杀键 csp:awd:firstblood:{cid}:r{round}:{vic}:{qid}（值为首杀队）还原。
     */
    private List<Map<String, Object>> attackLog(Long contestId) {
        List<Map<String, Object>> out = new ArrayList<>();
        try {
            Set<String> keys = redisService.keys(AWD_P + "hit:" + contestId + ":*");
            if (keys == null || keys.isEmpty()) return out;

            // 按 (round, victim, qid) 聚合：一个目标被谁攻破过、首杀是谁
            Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
            for (String key : keys) {
                if (key.endsWith(":lock")) continue; // 并发守卫锁不算攻破事实
                String[] t = key.split(":");
                // csp awd hit {cid} r{round} {atk} {vic} {qid}
                if (t.length < 8) continue;
                String round = t[4].startsWith("r") ? t[4].substring(1) : t[4];
                String attacker = t[5], victim = t[6], qid = t[7];
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("roundNo", Integer.parseInt(round));
                e.put("attackerTeamId", attacker);
                e.put("victimTeamId", victim);
                e.put("questionId", qid);
                groups.computeIfAbsent(round + ":" + victim + ":" + qid, k -> new ArrayList<>()).add(e);
            }
            for (List<Map<String, Object>> group : groups.values()) {
                for (Map<String, Object> e : group) {
                    String fbKey = AWD_P + "firstblood:" + contestId + ":r" + e.get("roundNo") + ":"
                            + e.get("victimTeamId") + ":" + e.get("questionId");
                    String fb = redisService.getString(fbKey);
                    // tryLock 用 JSON 序列化器写入，实际落库为带引号的 JSON 字符串，如 "60033"
                    fb = fb == null ? null : fb.replace("\"", "").trim();
                    e.put("firstBlood", fb != null && String.valueOf(e.get("attackerTeamId")).equals(fb.trim()));
                }
                group.sort(Comparator.comparing(m -> Boolean.TRUE.equals(m.get("firstBlood")) ? 0 : 1));
                out.addAll(group);
            }
            out.sort(Comparator.comparingInt(m -> (int) m.get("roundNo")));
        } catch (Exception ignored) {
            // 读取失败时返回空列表，不让大屏 500
        }
        return out;
    }

    /** 记录比分历史快照：相邻两点间隔至少 60 秒，保留最近 240 个点。 */
    private void recordTrendSnapshot(Long contestId, List<ScoreVO> board) {
        if (board == null || board.isEmpty()) return;
        String key = "csp:awd:trend:" + contestId;
        long now = System.currentTimeMillis();
        try {
            List<Object> last = redisService.lRange(key, -1, -1);
            if (last != null && !last.isEmpty()) {
                try {
                    Map<String, Object> prev = objectMapper.convertValue(
                            last.get(0), new TypeReference<Map<String, Object>>() {});
                    Object t = prev.get("t");
                    if (t == null || now - Long.parseLong(String.valueOf(t)) < 60_000L) return;
                } catch (Exception ignored) {
                    return; // 解析失败宁可少记点，也别记脏点
                }
            }
            Map<String, Object> snap = new LinkedHashMap<>();
            Map<String, Object> scores = new LinkedHashMap<>();
            for (ScoreVO vo : board) scores.put(String.valueOf(vo.getTeamId()), vo.getScore());
            snap.put("t", now);
            snap.put("scores", scores);
            redisService.lPush(key, snap);
            redisService.lTrim(key, -240, -1);
        } catch (Exception ignored) {
            // 采样失败不影响主接口
        }
    }
}
