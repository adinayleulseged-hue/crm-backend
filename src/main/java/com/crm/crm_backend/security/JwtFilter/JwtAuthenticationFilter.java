package com.crm.crm_backend.security.JwtFilter;

import com.crm.crm_backend.security.JwtBlacklist.RedisJwtBlacklistChecker;
import com.crm.crm_backend.security.JwtService.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates JWTs issued by the shared Auth Service using claim-based authorities
 * (no local user store lookup). Optionally rejects blacklisted jti via Redis.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectProvider<RedisJwtBlacklistChecker> blacklistChecker;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(token)
                    && !isBlacklisted(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);

                if (username != null && role != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBlacklisted(String token) {
        RedisJwtBlacklistChecker checker = blacklistChecker.getIfAvailable();
        if (checker == null) {
            return false;
        }
        String jti = jwtService.extractJti(token);
        return checker.isBlacklisted(jti);
    }
}
