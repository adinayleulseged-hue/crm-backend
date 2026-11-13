package com.crm.crm_backend.controller;

import com.crm.crm_backend.dto.request.LoginRequestDTO;
import com.crm.crm_backend.dto.request.RegisterRequestDTO;
import com.crm.crm_backend.dto.response.LoginResponseDTO;
import com.crm.crm_backend.service.core.AuthService;
import com.crm.crm_backend.notification.EmailService.EmailService;
import com.crm.crm_backend.model.entity.User;
import com.crm.crm_backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }


    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        return ResponseEntity.ok(
                authService.register(request)
        );
    }

    // =========================================================
    // ADMIN: CREATE USER WITH AUTO-GENERATED PASSWORD
    // =========================================================

    @PostMapping("/admin/create-user")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> createUserWithEmail(
            @RequestBody Map<String, String> request) {
        log.info("========== CREATE USER REQUEST RECEIVED ==========");
        log.info("Request body: {}", request);
        
        try {
            String username = request.get("username");
            String email = request.get("email");
            String role = request.get("role");

            log.info("Username: {}, Email: {}, Role: {}", username, email, role);

            if (username == null || username.trim().isEmpty()) {
                log.error("Username is required but was empty");
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Username is required")
                );
            }

            // Check if user already exists
            if (userRepository.findByUsername(username).isPresent()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Username already exists")
                );
            }

            if (email != null && !email.trim().isEmpty() && userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Email already exists")
                );
            }

            // Generate temporary password (8 characters)
            String tempPassword = generateTemporaryPassword();

            // Set default email if not provided
            String userEmail = (email != null && !email.trim().isEmpty()) 
                ? email.trim() 
                : username.trim() + "@insa.gov.et";

            // Create user
            User user = new User();
            user.setUsername(username.trim());
            user.setEmail(userEmail);
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setRole(role != null ? role : "SALES_OFFICER");

            User savedUser = userRepository.save(user);

            // Try to send email
            try {
                String emailBody = String.format(
                    "Dear %s,\n\n" +
                    "Your account has been created in the INSA CRM system.\n\n" +
                    "Login Details:\n" +
                    "- Username: %s\n" +
                    "- Email: %s\n" +
                    "- Temporary Password: %s\n" +
                    "- Role: %s\n\n" +
                    "Please login at: http://localhost:3000/role-login\n\n" +
                    "For security reasons, please change your password after your first login.\n\n" +
                    "Best regards,\n" +
                    "INSA CRM Team",
                    username.trim(),
                    username.trim(),
                    userEmail,
                    tempPassword,
                    savedUser.getRole()
                );
                
                boolean emailSent = emailService.send(
                    userEmail,
                    "INSA CRM - Your Account Has Been Created",
                    emailBody
                );

                // Success - email sent
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("userId", savedUser.getId());
                response.put("username", savedUser.getUsername());
                response.put("email", savedUser.getEmail());
                response.put("role", savedUser.getRole());
                response.put("emailSent", emailSent);
                
                response.put("temporaryPassword", tempPassword);
                if (emailSent) {
                    response.put("message", "User created successfully. Credentials sent via email.");
                } else {
                    response.put("message", "User created successfully");
                    response.put("note", "Email service not configured. Share this password manually.");
                }

                return ResponseEntity.ok(response);

            } catch (Exception emailError) {
                log.error("Failed to send email for user creation: {}", emailError.getMessage());

                // Success but email failed - return password
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("userId", savedUser.getId());
                response.put("username", savedUser.getUsername());
                response.put("email", savedUser.getEmail());
                response.put("role", savedUser.getRole());
                response.put("temporaryPassword", tempPassword);
                response.put("emailSent", false);
                response.put("message", "User created successfully");
                response.put("note", "Email service error. Share this password manually: " + emailError.getMessage());

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to create user: " + e.getMessage())
            );
        }
    }

    private String generateTemporaryPassword() {
        // Generate 8-character alphanumeric password
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }


    // =========================================================
    // ME
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<com.crm.crm_backend.common.ApiResponse<com.crm.crm_backend.dto.response.UserResponseDTO>> getCurrentUser(
            org.springframework.security.core.Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        com.crm.crm_backend.dto.response.UserResponseDTO user = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(
                com.crm.crm_backend.common.ApiResponse.success("Current user profile", user)
        );
    }
}