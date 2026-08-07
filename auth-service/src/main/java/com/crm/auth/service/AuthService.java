package com.crm.auth.service;

import com.crm.auth.dto.AuthResponseDTO;
import com.crm.auth.dto.LoginRequestDTO;
import com.crm.auth.dto.RegisterRequestDTO;
import com.crm.auth.dto.UserProfileDTO;
import com.crm.auth.model.AuthUser;
import com.crm.auth.model.UserRole;
import com.crm.auth.repository.AuthUserRepository;
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

import java.util.EnumSet;
import java.util.Set;

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
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        AuthUser user = authUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String token = jwtTokenService.generateToken(user);
        log.info("User {} logged in with role {}", user.getUsername(), user.getRole());

        return toAuthResponse(user, token);
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
                .enabled(true)
                .build();

        AuthUser saved = authUserRepository.save(user);
        String token = jwtTokenService.generateToken(saved);
        log.info("Registered user {} with role {} (adminCaller={})",
                saved.getUsername(), saved.getRole(), adminCaller);

        return toAuthResponse(saved, token);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO currentUser(String username) {
        AuthUser user = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }

    private AuthResponseDTO toAuthResponse(AuthUser user, String token) {
        return AuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(jwtTokenService.getExpirationMs())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
