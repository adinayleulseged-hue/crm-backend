package com.crm.crm_backend.controller;

import com.crm.crm_backend.dto.request.CustomerInvitationDTO;
import com.crm.crm_backend.model.entity.Customer;
import com.crm.crm_backend.model.entity.User;
import com.crm.crm_backend.model.enums.CustomerStatus;
import com.crm.crm_backend.model.enums.CustomerType;
import com.crm.crm_backend.repository.CustomerRepository;
import com.crm.crm_backend.repository.UserRepository;
import com.crm.crm_backend.notification.EmailService.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerInvitationController {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Admin creates customer and sends invitation email
     * POST /api/customers/invite
     */
    @PostMapping("/invite")
    public ResponseEntity<?> inviteCustomer(@RequestBody CustomerInvitationDTO dto) {
        log.info("Received invitation request for: {}", dto.getEmail());
        
        try {
            // Validate input
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                log.error("Email is required but was empty");
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Email is required")
                );
            }

            if (dto.getCompanyName() == null || dto.getCompanyName().trim().isEmpty()) {
                log.error("Company name is required but was empty");
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Company name is required")
                );
            }

            // Check if customer already exists
            if (customerRepository.findByEmail(dto.getEmail()).isPresent()) {
                log.warn("Customer already exists with email: {}", dto.getEmail());
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Customer with this email already exists")
                );
            }

            // Check if user already exists
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                log.warn("User already exists with email: {}", dto.getEmail());
                return ResponseEntity.badRequest().body(
                    Map.of("error", "User with this email already exists")
                );
            }

            // Generate temporary password (6 characters)
            String tempPassword = generateTemporaryPassword();
            log.info("Generated temporary password for: {}", dto.getEmail());
            
            // Create Customer record
            Customer customer = new Customer();
            customer.setCustomerNumber(generateCustomerNumber());
            customer.setCompanyName(dto.getCompanyName());
            customer.setContactName(dto.getContactName() != null ? dto.getContactName() : "");
            customer.setEmail(dto.getEmail());
            customer.setPhone(dto.getPhone() != null ? dto.getPhone() : "");
            customer.setCustomerType(CustomerType.valueOf(dto.getCustomerType()));
            customer.setStatus(CustomerStatus.ACTIVE);
            customer.setDataClassification("CONFIDENTIAL"); // Account created via admin invitation
            
            Customer savedCustomer = customerRepository.save(customer);
            log.info("Customer created with ID: {}", savedCustomer.getId());

            // Create auth User with CUSTOMER role
            User user = new User();
            user.setUsername(dto.getEmail()); // Use email as username
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setRole("CUSTOMER");
            // Note: User table doesn't have customerId field
            // The link is maintained through email matching
            
            userRepository.save(user);
            log.info("Auth user created for customer: {}", dto.getEmail());

            // Try to send email invitation
            boolean emailSent = false;
            try {
                String emailBody = String.format(
                    "Dear %s,\n\n" +
                    "Your customer account has been created in the INSA CRM system.\n\n" +
                    "Login Details:\n" +
                    "- Email: %s\n" +
                    "- Temporary Password: %s\n\n" +
                    "Please login at: http://localhost:3000/customer-login\n\n" +
                    "For security reasons, please change your password after your first login.\n\n" +
                    "Best regards,\n" +
                    "INSA CRM Team",
                    dto.getContactName() != null ? dto.getContactName() : "Customer",
                    dto.getEmail(),
                    tempPassword
                );
                
                emailSent = emailService.send(
                    dto.getEmail(),
                    "Welcome to INSA CRM - Your Account Has Been Created",
                    emailBody
                );
                
                if (emailSent) {
                    log.info("Email sent successfully to: {}", dto.getEmail());
                } else {
                    log.warn("Email not sent to: {}", dto.getEmail());
                }
            } catch (Exception emailError) {
                log.warn("Failed to send email to {}: {}", dto.getEmail(), emailError.getMessage());
                // Continue anyway - we'll return the password
            }

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("customerId", savedCustomer.getId());
            response.put("customerNumber", savedCustomer.getCustomerNumber());
            response.put("email", dto.getEmail());
            response.put("emailSent", emailSent);
            
            if (emailSent) {
                response.put("message", "Customer invited successfully. Credentials sent via email.");
            } else {
                response.put("message", "Customer invited successfully");
                response.put("temporaryPassword", tempPassword);
                response.put("note", "Email service not configured. Share this password with customer manually.");
            }

            log.info("Invitation successful for: {}", dto.getEmail());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid customer type: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                Map.of("error", "Invalid customer type: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Failed to invite customer: ", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to invite customer: " + e.getMessage())
            );
        }
    }

    private String generateTemporaryPassword() {
        // Generate 6-character alphanumeric password
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }

    private String generateCustomerNumber() {
        return "CUST-" + System.currentTimeMillis();
    }
}
