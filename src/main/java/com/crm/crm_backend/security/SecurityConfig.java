package com.crm.crm_backend.security;

import com.crm.crm_backend.security.JwtFilter.JwtAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Order(2) // Evaluated after CrmSecurityConfig
public class SecurityConfig {

    private final ObjectProvider<JwtAuthenticationFilter> legacyJwtFilter;
    private final String authMode;
    private final String allowedOrigins;

    public SecurityConfig(
            ObjectProvider<JwtAuthenticationFilter> legacyJwtFilter,
            @Value("${crm.security.auth-mode:legacy}") String authMode,
            @Value("${crm.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
            String allowedOrigins
    ) {
        this.legacyJwtFilter = legacyJwtFilter;
        this.authMode = authMode;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/**",
                                "/api/users",
                                "/api/users/**",
                                "/api/admin/**",
                                "/api/customers/invite",
                                "/api/leads",
                                "/api/leads/public",
                                "/api/payments",
                                "/api/payments/**",
                                "/api/sales-orders",
                                "/api/sales-orders/**",
                                "/api/interactions",
                                "/api/interactions/**",
                                "/api/opportunities",
                                "/api/opportunities/**",
                                "/api/sales-deals",
                                "/api/sales-deals/**",
                                "/api/quotations",
                                "/api/quotations/**",
                                "/api/invoices",
                                "/api/invoices/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/**",
                                "/api/users",
                                "/api/users/**",
                                "/api/admin/**",
                                "/api/customers/**",
                                "/api/payments",
                                "/api/payments/**",
                                "/api/sales-orders",
                                "/api/sales-orders/**",
                                "/api/interactions",
                                "/api/interactions/**",
                                "/api/opportunities",
                                "/api/opportunities/**",
                                "/api/sales-deals",
                                "/api/sales-deals/**",
                                "/api/quotations",
                                "/api/quotations/**",
                                "/api/invoices",
                                "/api/invoices/**",
                                "/api/products",
                                "/api/products/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/users/**",
                                "/api/sales-orders/**",
                                "/api/interactions/**",
                                "/api/quotations/**",
                                "/api/invoices/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/users/**",
                                "/api/payments/**",
                                "/api/sales-orders/**",
                                "/api/opportunities/**",
                                "/api/sales-deals/**",
                                "/api/invoices/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/**",
                                "/api/interactions/**"
                        ).permitAll()
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus"
                        ).permitAll()
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        if ("legacy".equalsIgnoreCase(authMode)) {
            JwtAuthenticationFilter filter = legacyJwtFilter.getIfAvailable();
            if (filter != null) {
                http.addFilterBefore(
                        filter,
                        UsernamePasswordAuthenticationFilter.class
                );
            }
        } else {
            http.oauth2ResourceServer(oauth -> oauth
                    .jwt(jwt -> {
                        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
                        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
                        jwt.jwtAuthenticationConverter(jwtConverter);
                    })
            );
        }

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-Id", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("X-Correlation-Id"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}