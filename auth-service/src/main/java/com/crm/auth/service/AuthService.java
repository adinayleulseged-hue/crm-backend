package com.crm.auth.service;

import com.crm.auth.dto.AuthResponseDTO;
import com.crm.auth.dto.LoginRequestDTO;
import com.crm.auth.dto.RegisterRequestDTO;
import com.crm.auth.model.AuthUser;
import com.crm.auth.repository.AuthUserRepository;
import com.crm.auth.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

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

        return AuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(jwtTokenService.getExpirationMs())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
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
                .role(request.getRole())
                .enabled(true)
                .build();

        AuthUser saved = authUserRepository.save(user);
        String token = jwtTokenService.generateToken(saved);
        log.info("Registered user {} with role {}", saved.getUsername(), saved.getRole());

        return AuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(jwtTokenService.getExpirationMs())
                .username(saved.getUsername())
                .role(saved.getRole())
                .build();
    }
}
