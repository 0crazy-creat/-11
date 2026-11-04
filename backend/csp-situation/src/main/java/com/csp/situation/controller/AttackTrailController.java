package com.csp.situation.controller;

import com.csp.common.api.Result;
import com.csp.ctf.entity.Submission;
import com.csp.ctf.mapper.SubmissionMapper;
import com.csp.security.annotation.PublicApi;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 攻防轨迹（flyline）与大屏回放：
 * 从 submission 表还原"谁在什么时间攻破了哪题"，供 3D 大屏绘制飞线与历史回放。
 */
@Tag(name = "攻防轨迹与大屏回放")
@RestController
@RequestMapping("/api/situation")
@RequiredArgsConstructor
public class AttackTrailController {

    private final SubmissionMapper submissionMapper;

    /** 飞线数据：每条 = 一次成功攻破，含起止坐标（由前端按队伍/题目布局映射）。 */
    @PublicApi
    @Operation(summary = "攻防飞线（flyline）数据")
    @GetMapping("/{contestId}/flylines")
    public Result<List<Map<String, Object>>> flylines(@PathVariable Long contestId,
                                                      @RequestParam(defaultValue = "200") Integer limit) {
        int n = Math.max(1, Math.min(limit, 1000));
        List<Submission> rows = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getContestId, contestId)
                .eq(Submission::getCorrect, 1)
                .orderByDesc(Submission::getCreatedAt)
                .last("LIMIT " + n));
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Submission s : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("teamId", s.getTeamId());
            m.put("questionId", s.getQuestionId());
            m.put("userId", s.getUserId());
            m.put("score", s.getScore());
            m.put("roundNo", s.getRoundNo());
            m.put("at", s.getCreatedAt());
            m.put("from", "team-" + s.getTeamId());
            m.put("to", "target-" + s.getQuestionId());
            out.add(m);
        }
        return Result.ok(out);
    }

    /** 回放时间轴：按分钟聚合的成功攻破数，用于大屏"历史回放"。 */
    @PublicApi
    @Operation(summary = "大屏回放时间轴")
    @GetMapping("/{contestId}/replay")
    public Result<Map<String, Object>> replay(@PathVariable Long contestId,
                                              @RequestParam(required = false) String from,
                                              @RequestParam(required = false) String to) {
        LambdaQueryWrapper<Submission> w = new LambdaQueryWrapper<Submission>()
                .eq(Submission::getContestId, contestId)
                .eq(Submission::getCorrect, 1)
                .orderByAsc(Submission::getCreatedAt);
        if (from != null && !from.isBlank()) w.ge(Submission::getCreatedAt, LocalDateTime.parse(from));
        if (to != null && !to.isBlank()) w.le(Submission::getCreatedAt, LocalDateTime.parse(to));
        List<Submission> rows = submissionMapper.selectList(w.last("LIMIT 5000"));

        // 按分钟桶聚合
        TreeMap<String, Integer> buckets = new TreeMap<>();
        for (Submission s : rows) {
            if (s.getCreatedAt() == null) continue;
            String k = s.getCreatedAt().withSecond(0).withNano(0).toString();
            buckets.merge(k, 1, Integer::sum);
        }
        List<Map<String, Object>> timeline = new ArrayList<>();
        buckets.forEach((k, v) -> {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("t", k);
            e.put("count", v);
            timeline.add(e);
        });
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("contestId", contestId);
        res.put("totalSolved", rows.size());
        res.put("frames", timeline.size());
        res.put("timeline", timeline);
        return Result.ok(res);
    }
}
