package com.csp.platform.controller;

import com.csp.common.api.Result;
import com.csp.ctf.mapper.ContestTeamMapper;
import com.csp.platform.mapper.ContestInfoMapper;
import com.csp.platform.mapper.QuestionMapper;
import com.csp.platform.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/** 控制台首页统计概览。 */
@Tag(name = "统计概览")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final SysUserMapper userMapper;
    private final ContestTeamMapper teamMapper;
    private final ContestInfoMapper contestMapper;
    private final QuestionMapper questionMapper;

    @Operation(summary = "首页统计卡片")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("teamCount", teamMapper.selectCount(null));
        data.put("contestCount", contestMapper.selectCount(null));
        data.put("challengeCount", questionMapper.selectCount(null));
        return Result.ok(data);
    }
}
