package com.crm.auth.service;

import com.crm.auth.model.TokenBlacklistEntry;
import com.crm.auth.repository.TokenBlacklistRepository;
import com.crm.auth.security.JwtTokenService;
import com.crm.auth.security.RedisJwtBlacklistStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtTokenService jwtTokenService;
    private final ObjectProvider<RedisJwtBlacklistStore> redisJwtBlacklistStore;

    @Transactional
    public void blacklistAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        try {
            if (!jwtTokenService.isTokenValid(accessToken)) {
                return;
            }
            String jti = jwtTokenService.extractJti(accessToken);
            if (jti == null || jti.isBlank()) {
                return;
            }
            Date expiry = jwtTokenService.extractExpiration(accessToken);
            LocalDateTime expiresAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(expiry.getTime()), ZoneId.systemDefault());

            tokenBlacklistRepository.save(TokenBlacklistEntry.builder()
                    .jti(jti)
                    .expiresAt(expiresAt)
                    .revokedAt(LocalDateTime.now())
                    .build());

            long ttlMs = Math.max(1L, expiry.getTime() - System.currentTimeMillis());
            RedisJwtBlacklistStore redisStore = redisJwtBlacklistStore.getIfAvailable();
            if (redisStore != null) {
                try {
                    redisStore.blacklist(jti, Duration.ofMillis(ttlMs));
                    log.info("Blacklisted access token jti={} in DB and Redis", jti);
                } catch (Exception redisEx) {
                    log.warn("Blacklisted jti={} in DB but Redis publish failed: {}", jti, redisEx.getMessage());
                }
            } else {
                log.info("Blacklisted access token jti={} in DB (Redis blacklist disabled)", jti);
            }
        } catch (Exception ex) {
            log.warn("Could not blacklist access token: {}", ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String accessToken) {
        try {
            String jti = jwtTokenService.extractJti(accessToken);
            if (jti == null || jti.isBlank()) {
                return false;
            }

            RedisJwtBlacklistStore redisStore = redisJwtBlacklistStore.getIfAvailable();
            if (redisStore != null) {
                try {
                    if (redisStore.isBlacklisted(jti)) {
                        return true;
                    }
                } catch (Exception redisEx) {
                    log.warn("Redis blacklist check failed, falling back to DB: {}", redisEx.getMessage());
                }
            }

            return tokenBlacklistRepository.existsByJtiAndExpiresAtAfter(jti, LocalDateTime.now());
        } catch (Exception ex) {
            return false;
        }
    }

    @Scheduled(cron = "${auth.security.blacklist-cleanup-cron:0 20 3 * * *}")
    @Transactional
    public void cleanupExpired() {
        int deleted = tokenBlacklistRepository.deleteExpired(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned {} expired blacklist entries", deleted);
        }
    }
}
