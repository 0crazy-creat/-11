package com.csp.ctf.service;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.ctf.entity.Contest;
import com.csp.ctf.mapper.ContestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** 赛事状态与时间窗校验（报名/答题共用的守卫逻辑）。 */
@Component
@RequiredArgsConstructor
public class ContestGuard {

    private final ContestMapper contestMapper;

    public Contest requireContest(Long contestId) {
        if (contestId == null) throw new BizException(ResultCode.BAD_REQUEST, "赛事ID不能为空");
        Contest c = contestMapper.selectById(contestId);
        if (c == null) throw BizException.notFound("赛事");
        return c;
    }

    /**
     * 报名窗口校验：仅赛前（SIGNUP）且处于报名时间区间。
     * 开赛后不再接受报名；补报名开关 allowLate 只放宽 signupEnd，不放宽赛事状态。
     */
    public Contest requireSignupOpen(Long contestId) {
        Contest c = requireContest(contestId);
        String st = c.getStatus();
        if ("RUNNING".equals(st)) {
            throw new BizException(ResultCode.CONFLICT, "比赛已开始，报名已截止");
        }
        if (!"SIGNUP".equals(st)) {
            throw new BizException(ResultCode.CONFLICT, "当前赛事状态不允许报名：" + st);
        }
        LocalDateTime now = LocalDateTime.now();
        boolean late = c.getAllowLate() != null && c.getAllowLate() == 1;
        if (c.getSignupEnd() != null && now.isAfter(c.getSignupEnd()) && !late) {
            throw new BizException(ResultCode.CONFLICT, "报名已截止");
        }
        if (c.getSignupStart() != null && now.isBefore(c.getSignupStart())) {
            throw new BizException(ResultCode.CONFLICT, "报名尚未开始");
        }
        return c;
    }

    /** 答题窗口校验：RUNNING 且处于比赛时间区间。 */
    public Contest requireAnswering(Long contestId) {
        Contest c = requireContest(contestId);
        if (!"RUNNING".equals(c.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "赛事未在进行中：" + c.getStatus());
        }
        LocalDateTime now = LocalDateTime.now();
        if (c.getStartAt() != null && now.isBefore(c.getStartAt())) {
            throw new BizException(ResultCode.CONFLICT, "比赛尚未开始");
        }
        if (c.getEndAt() != null && now.isAfter(c.getEndAt())) {
            throw new BizException(ResultCode.CONFLICT, "比赛已结束");
        }
        return c;
    }
}
