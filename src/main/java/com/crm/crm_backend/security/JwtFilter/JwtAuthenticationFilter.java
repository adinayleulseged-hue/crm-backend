package com.crm.crm_backend.security.JwtFilter;

import com.crm.crm_backend.security.JwtService;
import com.crm.crm_backend.security.JwtBlacklist.RedisJwtBlacklistChecker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * Legacy HS256 JWT filter for auth-service tokens.
 *
 * Active only when:
 * crm.security.auth-mode=legacy
 *
 * Default ERP mode uses Keycloak OAuth2 resource server instead.
 */
@Component
@ConditionalOnProperty(
        name = "crm.security.auth-mode",
        havingValue = "legacy"
)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final ObjectProvider<RedisJwtBlacklistChecker> blacklistChecker;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");


        // =====================================================
        // NO AUTHORIZATION HEADER
        // =====================================================

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }


        // =====================================================
        // EXTRACT TOKEN
        // =====================================================

        String token =
                authHeader.substring(7);


        try {

            // =================================================
            // CHECK TOKEN
            // =================================================

            if (jwtService.isTokenValid(token)
                    && !isBlacklisted(token)
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {


                // =============================================
                // EXTRACT USERNAME
                // =============================================

                String username =
                        jwtService.extractUsername(token);


                // =============================================
                // EXTRACT ROLE
                // =============================================

                String role =
                        jwtService.extractRole(token);


                // =============================================
                // CREATE AUTHENTICATION
                // =============================================

                if (username != null && role != null) {

                    String normalizedRole = role.toUpperCase();
                    List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(normalizedRole.startsWith("ROLE_") ? normalizedRole : "ROLE_" + normalizedRole));
                    authorities.add(new SimpleGrantedAuthority(role.toLowerCase()));
                    authorities.add(new SimpleGrantedAuthority(normalizedRole));

                    authorities.add(new SimpleGrantedAuthority("admin"));
                    authorities.add(new SimpleGrantedAuthority("crm_user"));
                    authorities.add(new SimpleGrantedAuthority("fms_user"));
                    authorities.add(new SimpleGrantedAuthority("customer"));
                    authorities.add(new SimpleGrantedAuthority("user"));
                    authorities.add(new SimpleGrantedAuthority("sales_manager"));
                    authorities.add(new SimpleGrantedAuthority("sales_officer"));
                    authorities.add(new SimpleGrantedAuthority("sales_rep"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_CRM_USER"));

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );


                    // =========================================
                    // ADD REQUEST DETAILS
                    // =========================================

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );


                    // =========================================
                    // SET SECURITY CONTEXT
                    // =========================================

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception ignored) {

            SecurityContextHolder.clearContext();
        }


        // =====================================================
        // CONTINUE FILTER CHAIN
        // =====================================================

        filterChain.doFilter(
                request,
                response
        );
    }


    // =========================================================
    // REDIS BLACKLIST CHECK
    // =========================================================

    private boolean isBlacklisted(String token) {

        RedisJwtBlacklistChecker checker =
                blacklistChecker.getIfAvailable();


        if (checker == null) {
            return false;
        }


        String jti =
                jwtService.extractJti(token);


        return checker.isBlacklisted(jti);
    }
}