package com.crm.crm_backend.security;

import com.crm.crm_backend.controller.LeadController;
import com.crm.crm_backend.dto.response.LeadResponseDTO;
import com.crm.crm_backend.security.JwtFilter.JwtAuthenticationFilter;
import com.crm.crm_backend.security.JwtService.JwtService;
import com.crm.crm_backend.security.SecurityConfig.SecurityConfig;
import com.crm.crm_backend.service.core.LeadService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LeadController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class})
@TestPropertySource(properties = {
        "crm.security.jwt.secret=Y3JtLWp3dC1zZWNyZXQtaW5zYS1lcnAtcGhhc2UtMS1rZXktMjAyNg==",
        "crm.security.jwt.expiration-ms=86400000"
})
class JwtProtectedApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeadService leadService;

    @Value("${crm.security.jwt.secret}")
    private String jwtSecret;

    @Test
    void protectedEndpointRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/leads/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpointAcceptsValidJwt() throws Exception {
        LeadResponseDTO dto = new LeadResponseDTO();
        dto.setId(1L);
        dto.setFirstName("Ada");
        dto.setLastName("Lovelace");
        when(leadService.getLeadById(eq(1L))).thenReturn(dto);

        String token = issueToken("admin", "ADMIN");

        mockMvc.perform(get("/api/leads/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Ada"));
    }

    private String issueToken(String username, String role) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("userId", 1L)
                .issuedAt(new Date(now))
                .expiration(new Date(now + 3_600_000))
                .signWith(key)
                .compact();
    }
}
