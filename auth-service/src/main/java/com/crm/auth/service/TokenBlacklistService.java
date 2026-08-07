package com.crm.auth.service;

import com.crm.auth.model.TokenBlacklistEntry;
import com.crm.auth.repository.TokenBlacklistRepository;
import com.crm.auth.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            log.info("Blacklisted access token jti={}", jti);
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
