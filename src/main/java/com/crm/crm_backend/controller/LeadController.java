package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.request.LeadConvertDTO;
import com.crm.crm_backend.dto.request.LeadCreateDTO;
import com.crm.crm_backend.dto.request.LeadUpdateDTO;
import com.crm.crm_backend.dto.response.LeadResponseDTO;
import com.crm.crm_backend.service.core.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> createLead(
            @Valid @RequestBody LeadCreateDTO dto) {

        LeadResponseDTO response = leadService.createLead(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lead created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> getLeadById(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(leadService.getLeadById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<Page<LeadResponseDTO>>> getAllLeads(
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(leadService.getAllLeads(pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadUpdateDTO dto) {

        return ResponseEntity.ok(ApiResponse.success(leadService.updateLead(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLead(
            @PathVariable Long id) {

        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.success("Lead deleted", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> updateLeadStatus(
            @PathVariable Long id,
            @RequestParam com.crm.crm_backend.model.enums.LeadStatus status) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lead status updated",
                leadService.updateLeadStatus(id, status)));
    }

    @PatchMapping("/{id}/convert")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> convertLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadConvertDTO dto) {

        return ResponseEntity.ok(ApiResponse.success(
                "Lead converted",
                leadService.convertLeadToCustomer(id, dto)));
    }
}
