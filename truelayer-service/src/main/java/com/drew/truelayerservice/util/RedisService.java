package com.drew.truelayerservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheToken(String userId, String accessToken, long expirationTime) {
        String key = "token:" + userId;
        log.info("Starting Redis Cache Token for user: {}", userId);

        // Delete any existing token for the user
        if (redisTemplate.hasKey(key)) {
            log.info("Existing token found for user: {}. Deleting...", userId);
            redisTemplate.delete(key);
        }

        // Cache the new access token
        redisTemplate.opsForValue().set(key, accessToken, expirationTime, TimeUnit.SECONDS);
        log.info("New token cached for user: {}", userId);
    }

    public String getCachedToken(String userId) {
        log.info("Starting Redis Get Cached Token");
        return redisTemplate.opsForValue().get("token:" + userId);
    }
}
