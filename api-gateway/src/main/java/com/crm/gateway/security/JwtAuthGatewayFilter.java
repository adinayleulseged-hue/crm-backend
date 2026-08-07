package com.crm.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Edge JWT validation plus shared Redis jti blacklist check.
 * Enable with gateway.security.jwt.enabled=true.
 */
@Component
@ConditionalOnProperty(name = "gateway.security.jwt.enabled", havingValue = "true")
public class JwtAuthGatewayFilter implements WebFilter, Ordered {

    private final SecretKey signingKey;
    private final ReactiveJwtBlacklistStore blacklistStore;

    public JwtAuthGatewayFilter(
            @Value("${gateway.security.jwt.secret}") String secret,
            ObjectProvider<ReactiveJwtBlacklistStore> blacklistStore) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.blacklistStore = blacklistStore.getIfAvailable();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())
                || isPublic(path)) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = auth.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String jti = claims.getId();
            if ((jti == null || jti.isBlank()) && claims.get("jti") != null) {
                jti = String.valueOf(claims.get("jti"));
            }

            if (blacklistStore == null || jti == null || jti.isBlank()) {
                return chain.filter(exchange);
            }

            return blacklistStore.isBlacklisted(jti)
                    .flatMap(revoked -> {
                        if (Boolean.TRUE.equals(revoked)) {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    })
                    .onErrorResume(ex -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        } catch (Exception ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublic(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
