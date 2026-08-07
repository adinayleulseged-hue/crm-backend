package com.crm.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@ConditionalOnProperty(name = "auth.security.redis-blacklist.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisJwtBlacklistStore {

    private final StringRedisTemplate redisTemplate;
    private final boolean failClosed;

    public RedisJwtBlacklistStore(
            StringRedisTemplate redisTemplate,
            @Value("${auth.security.redis-blacklist.fail-closed:true}") boolean failClosed) {
        this.redisTemplate = redisTemplate;
        this.failClosed = failClosed;
    }

    public void blacklist(String jti, Duration ttl) {
        if (jti == null || jti.isBlank() || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redisTemplate.opsForValue().set(JwtBlacklistKeys.key(jti), "revoked", ttl);
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(JwtBlacklistKeys.key(jti)));
        } catch (Exception ex) {
            if (failClosed) {
                throw ex;
            }
            return false;
        }
    }
}
