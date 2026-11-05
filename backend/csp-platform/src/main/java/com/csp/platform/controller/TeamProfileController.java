package com.csp.platform.controller;

import com.csp.common.api.Result;
import com.csp.common.security.LoginUser;
import com.csp.platform.dto.TeamProfileVO;
import com.csp.platform.service.TeamProfileService;
import com.csp.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 战队主页（全局战队维度）：队伍信息、参赛历史、获奖记录、各场成绩、题解集合、队员能力画像。
 * <p>与赛事内的 {@code /api/situation/teams/{id}}（按参赛记录 ID）区分：这里按 {@code team.id} 聚合跨赛事数据。</p>
 */
@Tag(name = "战队主页")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamProfileController {

    private final TeamProfileService teamProfileService;

    @Operation(summary = "战队主页聚合数据")
    @GetMapping("/{id}/profile")
    public Result<TeamProfileVO> profile(@PathVariable Long id) {
        LoginUser u = UserContext.get();
        return Result.ok(teamProfileService.profile(id, u == null ? null : u.getUserId()));
    }
}
