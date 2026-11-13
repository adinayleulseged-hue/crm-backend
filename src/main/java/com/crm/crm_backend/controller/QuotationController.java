package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.request.QuotationCreateDTO;
import com.crm.crm_backend.dto.request.QuotationUpdateDTO;
import com.crm.crm_backend.dto.response.QuotationResponseDTO;
import com.crm.crm_backend.dto.response.SalesOrderResponseDTO;
import com.crm.crm_backend.model.enums.QuotationStatus;
import com.crm.crm_backend.service.core.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @PostMapping
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<QuotationResponseDTO>> createQuotation(
            @Valid @RequestBody QuotationCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation created", quotationService.createQuotation(dto)));
    }

    @GetMapping("/export.csv")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<String> exportQuotations(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) QuotationStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long opportunityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryTo) {

        String csv = quotationService.exportQuotationsCsv(
                q, status, customerId, opportunityId, issueFrom, issueTo, expiryFrom, expiryTo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quotations.csv\"")
                .contentType(new MediaType("text", "csv"))
                .body(csv);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<QuotationResponseDTO>> getQuotation(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(quotationService.getQuotation(id)));
    }

    @GetMapping
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<QuotationResponseDTO>>> getAllQuotations(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) QuotationStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long opportunityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryTo,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                quotationService.searchQuotations(
                        q, status, customerId, opportunityId, issueFrom, issueTo, expiryFrom, expiryTo, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<QuotationResponseDTO>> updateQuotation(
            @PathVariable Long id,
            @Valid @RequestBody QuotationUpdateDTO dto) {

        return ResponseEntity.ok(ApiResponse.success(quotationService.updateQuotation(id, dto)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<QuotationResponseDTO>> changeStatus(
            @PathVariable Long id,
            @RequestParam QuotationStatus status) {

        return ResponseEntity.ok(ApiResponse.success(
                "Quotation status updated",
                quotationService.changeStatus(id, status)));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<SalesOrderResponseDTO>> acceptQuotation(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Quotation accepted",
                quotationService.acceptQuotation(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteQuotation(
            @PathVariable Long id) {

        quotationService.deleteQuotation(id);
        return ResponseEntity.ok(ApiResponse.success("Quotation deleted", null));
    }
}
