package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.request.PricingApplyRequestDTO;
import com.crm.crm_backend.dto.request.PricingRuleRequestDTO;
import com.crm.crm_backend.dto.response.PricingApplyResponseDTO;
import com.crm.crm_backend.dto.response.PricingRuleResponseDTO;
import com.crm.crm_backend.service.core.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<ApiResponse<PricingRuleResponseDTO>> createRule(
            @Valid @RequestBody PricingRuleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pricing rule created", pricingService.createRule(dto)));
    }

    @GetMapping("/rules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<PricingRuleResponseDTO>> getRule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(pricingService.getRule(id)));
    }

    @GetMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<Page<PricingRuleResponseDTO>>> getAllRules(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(pricingService.getAllRules(pageable)));
    }

    @PutMapping("/rules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<ApiResponse<PricingRuleResponseDTO>> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody PricingRuleRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pricing rule updated",
                pricingService.updateRule(id, dto)));
    }

    @DeleteMapping("/rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        pricingService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("Pricing rule deleted", null));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<PricingApplyResponseDTO>> applyPricing(
            @Valid @RequestBody PricingApplyRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pricing applied",
                pricingService.applyPricing(dto)));
    }
}
