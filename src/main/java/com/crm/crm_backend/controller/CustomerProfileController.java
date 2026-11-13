package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.request.CustomerProfileUpdateDTO;
import com.crm.crm_backend.dto.response.CustomerResponseDTO;
import com.crm.crm_backend.service.core.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/customers", "/api/crm/customers"})
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerService customerService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> getProfile(Authentication authentication) {
        String principalName = authentication != null ? authentication.getName() : "user";
        CustomerResponseDTO profile = customerService.getProfile(principalName);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profile));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody CustomerProfileUpdateDTO dto) {
        String principalName = authentication != null ? authentication.getName() : "user";
        CustomerResponseDTO updated = customerService.updateProfile(principalName, dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Profile updated successfully", updated));
    }
}
