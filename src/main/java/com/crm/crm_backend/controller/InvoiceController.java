package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.request.InvoiceCreateDTO;
import com.crm.crm_backend.dto.request.InvoiceUpdateDTO;
import com.crm.crm_backend.dto.response.InvoiceResponseDTO;
import com.crm.crm_backend.service.core.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER', 'SALES_OFFICER')")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> createInvoice(
            @Valid @RequestBody InvoiceCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created", invoiceService.createInvoice(dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> getInvoice(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(invoiceService.getInvoice(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER', 'SALES_OFFICER')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponseDTO>>> getAllInvoices(
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(invoiceService.getAllInvoices(pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceUpdateDTO dto) {

        return ResponseEntity.ok(ApiResponse.success(invoiceService.updateInvoice(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(
            @PathVariable Long id) {

        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted", null));
    }
}
