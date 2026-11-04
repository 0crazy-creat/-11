package com.csp.platform.controller;

import com.csp.common.api.Result;
import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.platform.entity.SysUser;
import com.csp.platform.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.csp.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 对外业务接口（需求 110 段）：
 * 以手机号（用户主索引）查询该用户在平台内的业务数据。
 * 具备：权限控制、参数校验、频次限制（由全局 RateLimitFilter 承担）、数据脱敏、字段白名单。
 */
@Slf4j
@Tag(name = "对外业务接口")
@RestController
@RequestMapping("/api/open")
@RequiredArgsConstructor
public class OpenApiController {

    private final SysUserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "按手机号查询用户业务数据（脱敏）")
    @RequirePermission("user:manage")
    @GetMapping("/user-by-phone")
    public Result<Map<String, Object>> byPhone(@RequestParam String phone) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BizException(ResultCode.BAD_REQUEST, "手机号格式不正确");
        }
        SysUser u = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getPhone, phone).last("LIMIT 1"));
        if (u == null) throw new BizException(ResultCode.NOT_FOUND, "未找到该手机号对应账号");

        Map<String, Object> m = new LinkedHashMap<>();
        // 数据脱敏：仅暴露授权字段
        m.put("userId", String.valueOf(u.getId()));
        m.put("username", u.getUsername());
        m.put("nickname", u.getNickname());
        m.put("phone", maskPhone(u.getPhone()));
        m.put("email", maskEmail(u.getEmail()));
        m.put("orgName", u.getOrgName());
        m.put("className", u.getClassName());
        m.put("status", u.getStatus());
        m.put("lastLoginAt", u.getLastLoginAt());

        // 业务数据：参与赛事 + 提交统计
        m.put("contests", queryList(
                "SELECT t.name AS teamName, c.name AS contestName, c.type AS contestType "
                        + "FROM contest_team_member m JOIN contest_team t ON t.id=m.team_id "
                        + "JOIN contest c ON c.id=t.contest_id WHERE m.user_id=? AND t.deleted=0 LIMIT 20",
                u.getId()));
        m.put("submissionStats", jdbcTemplate.query(
                "SELECT COUNT(*) AS total, SUM(CASE WHEN correct=1 THEN 1 ELSE 0 END) AS solved "
                        + "FROM submission WHERE user_id=? AND deleted=0",
                rs -> {
                    Map<String, Object> x = new LinkedHashMap<>();
                    if (rs.next()) {
                        x.put("total", rs.getInt("total"));
                        x.put("solved", rs.getInt("solved"));
                    }
                    return x;
                }, u.getId()));
        log.info("[OPEN-API] 手机号查询 userId={} phone={}", u.getId(), maskPhone(phone));
        return Result.ok(m);
    }

    private List<Map<String, Object>> queryList(String sql, Object... args) {
        return jdbcTemplate.query(sql, (rs, i) -> {
            Map<String, Object> x = new LinkedHashMap<>();
            x.put("teamName", rs.getString("teamName"));
            x.put("contestName", rs.getString("contestName"));
            x.put("contestType", rs.getString("contestType"));
            return x;
        }, args);
    }

    static String maskPhone(String p) {
        if (p == null || p.length() < 7) return p;
        return p.substring(0, 3) + "****" + p.substring(p.length() - 4);
    }

    static String maskEmail(String e) {
        if (e == null || !e.contains("@")) return e;
        int at = e.indexOf('@');
        String name = e.substring(0, at);
        String keep = name.length() <= 2 ? name.substring(0, 1) : name.substring(0, 2);
        return keep + "***" + e.substring(at);
    }
}
