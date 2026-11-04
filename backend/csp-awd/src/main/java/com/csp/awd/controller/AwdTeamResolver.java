package com.csp.awd.controller;

import com.csp.common.security.LoginUser;
import com.csp.security.core.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户所属战队解析。
 *
 * <p>直接从 contest_team_member 关联 contest_team 查询（按赛事过滤），
 * 不依赖 LoginUser.orgCode 注入，避免与赛事模块循环依赖。</p>
 */
@Component
@RequiredArgsConstructor
class AwdTeamResolver {

    private final JdbcTemplate jdbcTemplate;

    /** 解析当前登录用户在指定赛事下的战队ID；无则返回 null。 */
    Long currentTeamId(Long contestId) {
        LoginUser u = UserContext.get();
        if (u == null || u.getUserId() == null) return null;
        return teamIdOf(u.getUserId(), contestId);
    }

    /** 解析指定用户在指定赛事下的战队ID；无则返回 null。 */
    Long teamIdOf(Long userId, Long contestId) {
        if (userId == null) return null;
        try {
            // 1) 先在成员表中查（含队长与普通队员）
            String sqlMember = "SELECT m.team_id FROM contest_team_member m "
                    + "JOIN contest_team t ON t.id = m.team_id "
                    + "WHERE m.user_id = ? AND t.contest_id = ? AND t.deleted = 0 AND m.deleted = 0 "
                    + "ORDER BY m.id DESC LIMIT 1";
            Long byMember = jdbcTemplate.query(sqlMember, rs -> rs.next() ? rs.getLong(1) : null, userId, contestId);
            if (byMember != null) return byMember;

            // 2) 兜底：成员表尚未落库时，按队长身份解析（建队即报名，队长必然属于该队）
            String sqlCaptain = "SELECT t.id FROM contest_team t "
                    + "WHERE t.captain_id = ? AND t.contest_id = ? AND t.deleted = 0 "
                    + "ORDER BY t.id DESC LIMIT 1";
            return jdbcTemplate.query(sqlCaptain, rs -> rs.next() ? rs.getLong(1) : null, userId, contestId);
        } catch (Exception e) {
            return null;
        }
    }
}
