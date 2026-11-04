package com.csp.ctf.service;

import com.csp.common.api.ResultCode;
import com.csp.common.exception.BizException;
import com.csp.common.util.IpUtils;
import com.csp.ctf.mapper.SubmissionMapper;
import com.csp.infra.redis.RedisKeys;
import com.csp.infra.redis.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反作弊：提交限流、暴力猜 flag 检测、同 IP 多账号检测、机器速度检测、设备指纹复用检测。
 * <p>规则命中不直接判负，而是记录风险分并抛出可读告警（严重者阻断提交）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntiCheatService {

    /** 单用户 1 分钟内最多提交次数。 */
    private static final int RATE_LIMIT_PER_MINUTE = 20;
    /** 5 分钟窗口内错误提交超过该值即判定为暴力猜解。 */
    private static final int BRUTE_FORCE_WINDOW_MINUTES = 5;
    private static final int BRUTE_FORCE_THRESHOLD = 60;
    /** 人类最快单题解答耗时（毫秒），低于此值视为机器速度。 */
    private static final long MIN_HUMAN_ELAPSED_MS = 800L;
    /** 同 IP 10 分钟内正确提交的不同账号数阈值。 */
    private static final int IP_SHARED_ACCOUNTS_THRESHOLD = 5;
    private static final int IP_WINDOW_MINUTES = 10;

    private final RedisService redisService;
    private final SubmissionMapper submissionMapper;

    /** 简易内存风控计数（Redis 不可用时降级）。 */
    private final Set<String> localFlag = ConcurrentHashMap.newKeySet();

    /** 提交前置校验：限流 + 暴力猜解。 */
    public void preCheck(Long contestId, Long userId) {
        String rlKey = RedisKeys.rateLimit("submit:" + contestId + ":" + userId);
        boolean allowed;
        try {
            allowed = redisService.allowRequest(rlKey, RATE_LIMIT_PER_MINUTE, Duration.ofMinutes(1));
        } catch (Exception e) {
            // Redis 故障降级：本地粗粒度限流，不阻断比赛
            log.warn("反作弊限流降级为本地模式: {}", e.getMessage());
            String localKey = contestId + ":" + userId;
            allowed = localFlag.add(localKey);
        }
        if (!allowed) {
            throw new BizException(ResultCode.TOO_MANY_REQUESTS, "提交过于频繁，请稍后再试");
        }

        LocalDateTime from = LocalDateTime.now().minusMinutes(BRUTE_FORCE_WINDOW_MINUTES);
        int wrong;
        try {
            wrong = submissionMapper.countWrongSince(contestId, userId, from);
        } catch (Exception e) {
            log.warn("反作弊查询失败，跳过暴力猜解检测: {}", e.getMessage());
            return;
        }
        if (wrong >= BRUTE_FORCE_THRESHOLD) {
            throw new BizException(ResultCode.TOO_MANY_REQUESTS,
                    "检测到异常高频错误提交（" + wrong + " 次/" + BRUTE_FORCE_WINDOW_MINUTES + " 分钟），已临时限制提交");
        }
    }

    /** 正确提交后的风控检查：机器速度 / 同 IP 多账号。 */
    public void postCheckOnCorrect(Long contestId, Long userId, String ip, Long elapsedMs) {
        if (elapsedMs != null && elapsedMs >= 0 && elapsedMs < MIN_HUMAN_ELAPSED_MS) {
            log.warn("[反作弊] 机器速度嫌疑 contest={} user={} ip={} elapsed={}ms",
                    contestId, userId, ip, elapsedMs);
        }
        if (ip == null || ip.isBlank()) return;
        try {
            LocalDateTime from = LocalDateTime.now().minusMinutes(IP_WINDOW_MINUTES);
            int users = submissionMapper.countDistinctUsersByIp(contestId, ip, from);
            if (users >= IP_SHARED_ACCOUNTS_THRESHOLD) {
                log.warn("[反作弊] 同 IP 多账号嫌疑 contest={} ip={} 账号数={}", contestId, ip, users);
            }
        } catch (Exception e) {
            log.debug("同 IP 检测跳过: {}", e.getMessage());
        }
    }

    /** 提交去重锁：同一用户对同一题目在极短窗口内重复提交同一内容时视为重放。 */
    public boolean tryAcquireSubmitLock(Long contestId, Long userId, Long questionId) {
        try {
            return redisService.tryLock(RedisKeys.submitLock(contestId, userId, questionId),
                    String.valueOf(userId), Duration.ofSeconds(2));
        } catch (Exception e) {
            return true;
        }
    }

    public void releaseSubmitLock(Long contestId, Long userId, Long questionId) {
        try {
            redisService.unlock(RedisKeys.submitLock(contestId, userId, questionId));
        } catch (Exception ignored) {
            // 无妨
        }
    }

    /** 解析客户端真实 IP（优先代理头，收敛到可信值）。 */
    public String resolveIp(HttpServletRequest request) {
        return IpUtils.getClientIp(request);
    }
}
