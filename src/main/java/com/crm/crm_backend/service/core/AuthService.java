package com.crm.crm_backend.service.core;

import com.crm.crm_backend.dto.request.LoginRequestDTO;
import com.crm.crm_backend.dto.request.RegisterRequestDTO;
import com.crm.crm_backend.dto.response.AuthResponseDTO;
import com.crm.crm_backend.exception.DuplicateEntityException;
import com.crm.crm_backend.model.entity.User;
import com.crm.crm_backend.repository.UserRepository;
import com.crm.crm_backend.security.JwtService.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        String token = jwtService.generateToken(user);
        log.info("User {} logged in with role {}", user.getUsername(), user.getRole());

        return AuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getExpirationMs())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateEntityException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntityException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);
        log.info("Registered user {} with role {}", saved.getUsername(), saved.getRole());

        return AuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getExpirationMs())
                .username(saved.getUsername())
                .role(saved.getRole())
                .build();
    }
}
