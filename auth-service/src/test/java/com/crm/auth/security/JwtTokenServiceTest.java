package com.crm.auth.security;

import com.crm.auth.model.AuthUser;
import com.crm.auth.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        ReflectionTestUtils.setField(jwtTokenService, "secret",
                "Y3JtLWp3dC1zZWNyZXQtaW5zYS1lcnAtcGhhc2UtMS1rZXktMjAyNg==");
        ReflectionTestUtils.setField(jwtTokenService, "expirationMs", 900_000L);
        ReflectionTestUtils.setField(jwtTokenService, "refreshExpirationMs", 604_800_000L);
    }

    @Test
    void generateToken_includesJtiAndIsValid() {
        AuthUser user = AuthUser.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        String token = jwtTokenService.generateToken(user);

        assertThat(jwtTokenService.isTokenValid(token)).isTrue();
        assertThat(jwtTokenService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtTokenService.extractRole(token)).isEqualTo("ADMIN");
        assertThat(jwtTokenService.extractJti(token)).isNotBlank();
    }
}
