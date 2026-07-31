package com.crm.crm_backend.config;

import com.crm.crm_backend.model.entity.User;
import com.crm.crm_backend.model.enums.UserRole;
import com.crm.crm_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedUser("admin", "admin@crm.local", "admin123", UserRole.ADMIN);
        seedUser("sales", "sales@crm.local", "sales123", UserRole.SALES_OFFICER);
        seedUser("finance", "finance@crm.local", "finance123", UserRole.FINANCE_OFFICER);
        seedUser("employee", "employee@crm.local", "employee123", UserRole.EMPLOYEE);
    }

    private void seedUser(String username, String email, String rawPassword, UserRole role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .build());

        log.info("Seeded CRM user '{}' with role {}", username, role);
    }
}
