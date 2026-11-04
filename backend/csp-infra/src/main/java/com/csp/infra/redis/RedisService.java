package com.csp.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Redis 通用操作封装：缓存、计数器、排行榜、分布式锁。 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 纯字符串通道：flag/token 等裸字符串，绕过 JSON 序列化器。 */
    private final StringRedisTemplate stringRedisTemplate;

    // ---------- 基础 ----------
    public void set(String key, Object value) { redisTemplate.opsForValue().set(key, value); }

    public void set(String key, Object value, Duration ttl) { redisTemplate.opsForValue().set(key, value, ttl); }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) { return (T) redisTemplate.opsForValue().get(key); }

    // ---------- 纯字符串（flag / token 等，绕过 JSON 序列化）----------
    public void setString(String key, String value) { stringRedisTemplate.opsForValue().set(key, value); }

    public void setString(String key, String value, Duration ttl) { stringRedisTemplate.opsForValue().set(key, value, ttl); }

    public String getString(String key) { return stringRedisTemplate.opsForValue().get(key); }

    public Long incrementString(String key) { return stringRedisTemplate.opsForValue().increment(key); }

    public void hSetString(String key, String field, String value) { stringRedisTemplate.opsForHash().put(key, field, value); }

    public String hGetString(String key, String field) {
        Object v = stringRedisTemplate.opsForHash().get(key, field);
        return v == null ? null : v.toString();
    }

    public Boolean delete(String key) { return redisTemplate.delete(key); }

    public Long delete(Collection<String> keys) { return redisTemplate.delete(keys); }

    public Boolean hasKey(String key) { return redisTemplate.hasKey(key); }

    public Boolean expire(String key, Duration ttl) { return redisTemplate.expire(key, ttl); }

    // ---------- 计数 ----------
    public Long increment(String key, long delta) { return redisTemplate.opsForValue().increment(key, delta); }

    public Long increment(String key) { return increment(key, 1); }

    // ---------- Hash ----------
    public void hSet(String key, String field, Object value) { redisTemplate.opsForHash().put(key, field, value); }

    public void hSetAll(String key, Map<String, Object> map) { redisTemplate.opsForHash().putAll(key, map); }

    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field) { return (T) redisTemplate.opsForHash().get(key, field); }

    public Map<Object, Object> hGetAll(String key) { return redisTemplate.opsForHash().entries(key); }

    public Long hDelete(String key, Object... fields) { return redisTemplate.opsForHash().delete(key, fields); }

    // ---------- ZSet 排行榜 ----------
    public Boolean zAdd(String key, Object value, double score) { return redisTemplate.opsForZSet().add(key, value, score); }

    public Double zIncrement(String key, Object value, double delta) { return redisTemplate.opsForZSet().incrementScore(key, value, delta); }

    public Double zScore(String key, Object value) { return redisTemplate.opsForZSet().score(key, value); }

    /** 取 TopN（分数从高到低）。 */
    public Set<ZSetOperations.TypedTuple<Object>> zTop(String key, long topN) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, topN - 1);
    }

    public Set<ZSetOperations.TypedTuple<Object>> zRange(String key, long start, long end, boolean desc) {
        return desc ? redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end)
                    : redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    public Long zRank(String key, Object value, boolean desc) {
        Long r = desc ? redisTemplate.opsForZSet().reverseRank(key, value) : redisTemplate.opsForZSet().rank(key, value);
        return r == null ? null : r + 1;
    }

    public Long zSize(String key) { return redisTemplate.opsForZSet().zCard(key); }

    public Long zRemove(String key, Object... values) { return redisTemplate.opsForZSet().remove(key, values); }

    // ---------- 分布式锁 ----------
    /** 简单分布式锁：SET NX EX。返回是否获取成功。 */
    public boolean tryLock(String key, String owner, Duration ttl) {
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, owner, ttl);
        return Boolean.TRUE.equals(ok);
    }

    public void unlock(String key) { redisTemplate.delete(key); }

    // ---------- 限流 ----------
    /** 固定窗口限流：返回 true 表示允许通过。 */
    public boolean allowRequest(String key, int limit, Duration window) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) redisTemplate.expire(key, window);
        return count != null && count <= limit;
    }

    // ---------- List ----------
    public Long lPush(String key, Object value) { return redisTemplate.opsForList().leftPush(key, value); }

    public List<Object> lRange(String key, long start, long end) { return redisTemplate.opsForList().range(key, start, end); }
}
