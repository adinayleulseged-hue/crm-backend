package com.crm.crm_backend.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuotationController {

    private final QuotationService quotationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<QuotationResponseDTO> createQuotation(
            @Valid @RequestBody QuotationCreateDTO dto) {

        return new ResponseEntity<>(
                quotationService.createQuotation(dto),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<QuotationResponseDTO> getQuotation(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                quotationService.getQuotation(id)
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<Page<QuotationResponseDTO>> getAllQuotations(
            Pageable pageable) {

        return ResponseEntity.ok(
                quotationService.getAllQuotations(pageable)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<QuotationResponseDTO> updateQuotation(
            @PathVariable Long id,
            @Valid @RequestBody QuotationUpdateDTO dto) {

        return ResponseEntity.ok(
                quotationService.updateQuotation(id, dto)
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<QuotationResponseDTO> changeStatus(
            @PathVariable Long id,
            @RequestParam QuotationStatus status) {

        return ResponseEntity.ok(quotationService.changeStatus(id, status));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<SalesOrderResponseDTO> acceptQuotation(
            @PathVariable Long id) {

        return ResponseEntity.ok(quotationService.acceptQuotation(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuotation(
            @PathVariable Long id) {

        quotationService.deleteQuotation(id);

        return ResponseEntity.noContent().build();
    }
}
