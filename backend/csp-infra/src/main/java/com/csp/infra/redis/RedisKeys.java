package com.csp.infra.redis;

/** Redis Key 命名规范集中管理，避免散落魔法字符串。 */
public final class RedisKeys {
    private static final String P = "csp:";
    private RedisKeys() {}

    /** 登录会话：csp:session:{userId}:{deviceId} */
    public static String session(Long userId, String deviceId) { return P + "session:" + userId + ":" + deviceId; }

    /** 第三方 Token 映射：csp:oauth:{provider}:{thirdToken} */
    public static String oauthToken(String provider, String token) { return P + "oauth:" + provider + ":" + token; }

    /** 排行榜有序集合：csp:rank:{contestId} */
    public static String rankBoard(Long contestId) { return P + "rank:" + contestId; }

    /** 首杀记录：csp:firstblood:{contestId} */
    public static String firstBlood(Long contestId) { return P + "firstblood:" + contestId; }

    /** 接口限流：csp:ratelimit:{key} */
    public static String rateLimit(String key) { return P + "ratelimit:" + key; }

    /** 热点赛事缓存：csp:contest:{contestId} */
    public static String contest(Long contestId) { return P + "contest:" + contestId; }

    /** Flag 校验缓存：csp:flag:{contestId}:{teamId}:{questionId} */
    public static String flag(Long contestId, Long teamId, Long questionId) {
        return P + "flag:" + contestId + ":" + teamId + ":" + questionId;
    }

    /** 提交去重：csp:submit:lock:{contestId}:{userId}:{questionId} */
    public static String submitLock(Long contestId, Long userId, Long questionId) {
        return P + "submit:lock:" + contestId + ":" + userId + ":" + questionId;
    }
}
