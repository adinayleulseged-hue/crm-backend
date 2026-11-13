package com.crm.crm_backend.service.core;

import com.crm.crm_backend.dto.request.LoginRequestDTO;
import com.crm.crm_backend.dto.request.RegisterRequestDTO;
import com.crm.crm_backend.dto.response.LoginResponseDTO;
import com.crm.crm_backend.model.entity.User;
import com.crm.crm_backend.repository.UserRepository;
import com.crm.crm_backend.security.JwtService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ObjectProvider<JwtService> jwtServiceProvider; // Wrapped with ObjectProvider

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            ObjectProvider<JwtService> jwtServiceProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtServiceProvider = jwtServiceProvider;
    }

    // =========================
    // REGISTER
    // =========================
    public String register(RegisterRequestDTO request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getRole() == null || request.getRole().isBlank()) {
            user.setRole("USER");
        } else {
            user.setRole(request.getRole().toUpperCase());
        }

        userRepository.save(user);

        return "User registered successfully";
    }

    // =========================
    // LOGIN
    // =========================
    public LoginResponseDTO login(LoginRequestDTO request) {

        // Validate that legacy JWT service is active
        JwtService jwtService = jwtServiceProvider.getIfAvailable();
        if (jwtService == null) {
            throw new IllegalStateException("Local login is disabled because Keycloak is the active authentication provider.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository
                .findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);

        return LoginResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .message("Login successful")
                .build();
    }

    // =========================
    // CURRENT USER
    // =========================
    public com.crm.crm_backend.dto.response.UserResponseDTO getCurrentUser(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return com.crm.crm_backend.dto.response.UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}