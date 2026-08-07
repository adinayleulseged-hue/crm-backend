package com.crm.auth.service;

import com.crm.auth.dto.AuthResponseDTO;
import com.crm.auth.dto.LoginRequestDTO;
import com.crm.auth.dto.RegisterRequestDTO;
import com.crm.auth.dto.UserProfileDTO;
import com.crm.auth.model.AuthUser;
import com.crm.auth.model.RefreshToken;
import com.crm.auth.model.UserRole;
import com.crm.auth.repository.AuthUserRepository;
import com.crm.auth.repository.RefreshTokenRepository;
import com.crm.auth.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final Set<UserRole> SELF_REGISTER_ROLES = EnumSet.of(
            UserRole.EMPLOYEE,
            UserRole.SALES_OFFICER,
            UserRole.FINANCE_OFFICER
    );

    private final AuthUserRepository authUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        AuthUser user = authUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        log.info("User {} logged in with role {}", user.getUsername(), user.getRole());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        UserRole requestedRole = request.getRole() != null ? request.getRole() : UserRole.EMPLOYEE;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean adminCaller = auth != null
                && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!adminCaller && !SELF_REGISTER_ROLES.contains(requestedRole)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Public registration only allows EMPLOYEE, SALES_OFFICER, or FINANCE_OFFICER. "
                            + "ADMIN (and other elevated roles) require an authenticated ADMIN.");
        }

        if (authUserRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists: " + request.getUsername());
        }
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists: " + request.getEmail());
        }

        AuthUser user = AuthUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(requestedRole)
                .territoryId(request.getTerritoryId())
                .enabled(true)
                .build();

        AuthUser saved = authUserRepository.save(user);
        log.info("Registered user {} with role {} territoryId={} (adminCaller={})",
                saved.getUsername(), saved.getRole(), saved.getTerritoryId(), adminCaller);

        return issueTokens(saved);
    }

    @Transactional
    public AuthResponseDTO refresh(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (stored.isRevoked() || stored.isExpired()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        AuthUser user = stored.getUser();
        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User disabled");
        }

        stored.setRevoked(true);
        RefreshToken replacement = createRefreshToken(user);
        stored.setReplacedBy(replacement.getToken());
        refreshTokenRepository.save(stored);
        refreshTokenRepository.save(replacement);

        String accessToken = jwtTokenService.generateToken(user);
        log.info("Refreshed tokens for user {}", user.getUsername());
        return toAuthResponse(user, accessToken, replacement.getToken());
    }

    @Transactional
    public void logout(String accessToken, String refreshTokenValue) {
        if (accessToken != null && !accessToken.isBlank()) {
            tokenBlacklistService.blacklistAccessToken(accessToken);
        }

        if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                refreshTokenRepository.revokeAllForUser(token.getUser().getId());
                log.info("Logged out user {} (revoked refresh tokens)", token.getUser().getUsername());
            });
        } else if (accessToken != null && !accessToken.isBlank()) {
            try {
                String username = jwtTokenService.extractUsername(accessToken);
                authUserRepository.findByUsername(username).ifPresent(user -> {
                    refreshTokenRepository.revokeAllForUser(user.getId());
                    log.info("Logged out user {} (revoked all refresh tokens)", username);
                });
            } catch (Exception ignored) {
                // access token may already be invalid; still ok
            }
        }
    }

    @Transactional(readOnly = true)
    public UserProfileDTO currentUser(String username) {
        AuthUser user = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return toProfile(user);
    }

    @Transactional(readOnly = true)
    public List<UserProfileDTO> listUsers(UserRole role, Long territoryId) {
        List<AuthUser> users;
        if (role != null && territoryId != null) {
            users = authUserRepository.findByRoleAndTerritoryIdAndEnabledTrue(role, territoryId);
        } else if (role != null) {
            users = authUserRepository.findByRoleAndEnabledTrue(role);
        } else if (territoryId != null) {
            users = authUserRepository.findByTerritoryIdAndEnabledTrue(territoryId);
        } else {
            users = authUserRepository.findByEnabledTrue();
        }
        return users.stream().map(this::toProfile).toList();
    }

    private UserProfileDTO toProfile(AuthUser user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .territoryId(user.getTerritoryId())
                .build();
    }

    private AuthResponseDTO issueTokens(AuthUser user) {
        String accessToken = jwtTokenService.generateToken(user);
        RefreshToken refreshToken = createRefreshToken(user);
        refreshTokenRepository.save(refreshToken);
        return toAuthResponse(user, accessToken, refreshToken.getToken());
    }

    private RefreshToken createRefreshToken(AuthUser user) {
        return RefreshToken.builder()
                .token(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""))
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenService.getRefreshExpirationMs() / 1000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AuthResponseDTO toAuthResponse(AuthUser user, String accessToken, String refreshToken) {
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtTokenService.getExpirationMs())
                .refreshExpiresInMs(jwtTokenService.getRefreshExpirationMs())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
