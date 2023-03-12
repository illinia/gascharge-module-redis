package com.gascharge.taemin.redis.access;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDao {
    private final RedisTemplate<String, String> redisTemplate;

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void set(String key, String data) {
        redisTemplate.opsForValue().set(key, data);
    }

    public boolean hasKey(String key) {
        if (key == null) {
            log.warn("key 가 null 입니다.");
            return false;
        }
        return redisTemplate.hasKey(key);
    }

    public boolean del(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
}
