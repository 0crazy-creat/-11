package com.csp.awd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AWD 视图用名称解析：把排行榜/靶机里的雪花 ID 补成可读名称。
 *
 * <p>csp-awd 不依赖 csp-ctf，故按只读 SQL 直接取 {@code contest_team}
 * （报名快照名，回退全局 {@code team.name}）与 {@code question.title}。</p>
 */
@Component
@RequiredArgsConstructor
public class AwdNameResolver {

    private final JdbcTemplate jdbcTemplate;

    /** 赛内有效战队（已审核通过）：teamId -> 名称。退赛/删除的历史 ID 不会出现在结果里。 */
    public Map<Long, String> teamNames(Long contestId, Collection<Long> teamIds) {
        Map<Long, String> out = new HashMap<>();
        List<Long> ids = clean(teamIds);
        if (contestId == null || ids.isEmpty()) return out;
        List<Object> args = new ArrayList<>(ids.size() + 1);
        args.add(contestId);
        args.addAll(ids);
        jdbcTemplate.query(
                "SELECT ct.id, COALESCE(NULLIF(ct.name, ''), t.name) AS name "
                        + "FROM contest_team ct LEFT JOIN team t ON t.id = ct.team_id "
                        + "WHERE ct.contest_id = ? AND ct.deleted = 0 AND ct.status = 'APPROVED' "
                        + "AND ct.id IN (" + placeholders(ids.size()) + ")",
                rs -> {
                    Long id = rs.getLong("id");
                    String name = rs.getString("name");
                    out.put(id, name == null || name.isBlank() ? "战队" + id : name);
                },
                args.toArray());
        return out;
    }

    /** 题目名称：questionId -> title。 */
    public Map<Long, String> questionTitles(Collection<Long> questionIds) {
        Map<Long, String> out = new HashMap<>();
        List<Long> ids = clean(questionIds);
        if (ids.isEmpty()) return out;
        jdbcTemplate.query(
                "SELECT id, title FROM question WHERE deleted = 0 AND id IN (" + placeholders(ids.size()) + ")",
                rs -> {
                    out.put(rs.getLong("id"), rs.getString("title"));
                },
                ids.toArray());
        return out;
    }

    private static List<Long> clean(Collection<Long> ids) {
        if (ids == null) return List.of();
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private static String placeholders(int n) {
        return String.join(",", java.util.Collections.nCopies(n, "?"));
    }
}
