package com.crm.crm_backend.security;

import com.crm.crm_backend.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
@ConditionalOnProperty(
        name = "crm.security.auth-mode",
        havingValue = "legacy"
)
public class JwtService {

    @Value("${crm.security.jwt.secret}")
    private String secret;

    @Value("${crm.security.jwt.expiration-ms}")
    private long expirationMs;


    // =========================================================
    // GENERATE JWT TOKEN
    // =========================================================

    public String generateToken(User user) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + expirationMs
        );

        return Jwts.builder()
                .subject(user.getUsername())

                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())

                .issuedAt(now)
                .expiration(expiration)

                .signWith(getSigningKey())
                .compact();
    }


    // =========================================================
    // EXTRACT USERNAME
    // =========================================================

    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }


    // =========================================================
    // EXTRACT ROLE
    // =========================================================

    public String extractRole(String token) {

        return extractClaim(
                token,
                claims -> claims.get(
                        "role",
                        String.class
                )
        );
    }


    // =========================================================
    // EXTRACT USER ID
    // =========================================================

    public Long extractUserId(String token) {

        return extractClaim(
                token,
                claims -> {

                    Object value = claims.get("userId");

                    if (value instanceof Number number) {
                        return number.longValue();
                    }

                    if (value != null) {
                        return Long.valueOf(
                                value.toString()
                        );
                    }

                    return null;
                }
        );
    }


    // =========================================================
    // EXTRACT JTI
    // =========================================================

    public String extractJti(String token) {

        return extractClaim(
                token,
                claims -> {

                    String jti = claims.getId();

                    if (jti == null || jti.isBlank()) {

                        Object claim =
                                claims.get("jti");

                        return claim != null
                                ? String.valueOf(claim)
                                : null;
                    }

                    return jti;
                }
        );
    }


    // =========================================================
    // VALIDATE TOKEN
    // =========================================================

    public boolean isTokenValid(String token) {

        try {

            return !isTokenExpired(token);

        } catch (Exception ex) {

            return false;
        }
    }


    // =========================================================
    // EXPIRATION
    // =========================================================

    public long getExpirationMs() {

        return expirationMs;
    }


    private boolean isTokenExpired(String token) {

        Date expiration =
                extractClaim(
                        token,
                        Claims::getExpiration
                );

        return expiration.before(new Date());
    }


    // =========================================================
    // EXTRACT CLAIM
    // =========================================================

    private <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {

        Claims claims =
                Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

        return resolver.apply(claims);
    }


    // =========================================================
    // SIGNING KEY
    // =========================================================

    private SecretKey getSigningKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}