package com.gascharge.taemin.redis.access;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

import static com.gascharge.taemin.redis.util.JsonUtil.getJson;
import static com.gascharge.taemin.redis.util.JsonUtil.setJson;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisJson {

    private final RedisDao redisDao;

    public Object get(String key, Type javaType) {
        String stringResult = redisDao.get(key);
        log.debug("RedisJson get key : {} ### classType : {} ### stringResult = {}", key, javaType, stringResult);

        try {
            Object json = getJson(stringResult, javaType);
            log.debug("RedisJson getJson result : {}", json);
            return json;
        } catch (Exception e) {
            throw new IllegalStateException("key : " + key + " type : " + javaType + " 을 역직렬화하는데 실패했습니다.", e);
        }
    }

    public boolean set(String key, Object data) {
        log.debug("RedisJson set key : {}, data : {}", key, data);
        try {
            String value = setJson(data);
            log.debug("RedisJson json : {}", value);
            redisDao.set(key, value);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }
}
