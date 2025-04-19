package org.buaa.project.toolkit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * redis 计数器
 */
@Component
@RequiredArgsConstructor
public class RedisCount {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * hash自增
     */
    public void hIncr(String key, String field, Integer cnt) {
        stringRedisTemplate.opsForHash().increment(key, field, cnt);
    }

    /**
     * 返回计数值
     */
    public Integer hGet(String key, String field) {
        return Optional.ofNullable(stringRedisTemplate.opsForHash().get(key, field))
                .map(value -> Integer.parseInt(value.toString()))
                .orElse(0);
    }

    /**
     * zset自增
     */
    public void zIncr(String key, String member, Integer cnt) {
        stringRedisTemplate.opsForZSet().incrementScore(key, member, cnt);
    }

}
