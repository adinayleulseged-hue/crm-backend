package com.crm.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@ConditionalOnProperty(name = "gateway.security.jwt.blacklist-enabled", havingValue = "true")
@ConditionalOnBean(ReactiveStringRedisTemplate.class)
public class ReactiveJwtBlacklistStore {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final boolean failClosed;

    public ReactiveJwtBlacklistStore(
            ReactiveStringRedisTemplate redisTemplate,
            @Value("${gateway.security.jwt.blacklist-fail-closed:true}") boolean failClosed) {
        this.redisTemplate = redisTemplate;
        this.failClosed = failClosed;
    }

    public Mono<Boolean> isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return Mono.just(false);
        }
        return redisTemplate.hasKey(JwtBlacklistKeys.key(jti))
                .map(Boolean.TRUE::equals)
                .onErrorResume(ex -> failClosed
                        ? Mono.error(ex)
                        : Mono.just(false));
    }
}
