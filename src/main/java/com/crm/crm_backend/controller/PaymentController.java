package com.crm.crm_backend.controller;

import com.crm.crm_backend.dto.request.PaymentCreateDTO;
import com.crm.crm_backend.dto.request.PaymentUpdateDTO;
import com.crm.crm_backend.dto.response.PaymentResponseDTO;
import com.crm.crm_backend.service.core.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody PaymentCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER', 'SALES_OFFICER')")
    public ResponseEntity<PaymentResponseDTO> getPayment(
            @PathVariable Long id) {

        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    public ResponseEntity<Page<PaymentResponseDTO>> getAllPayments(
            Pageable pageable) {

        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    public ResponseEntity<PaymentResponseDTO> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentUpdateDTO dto) {

        return ResponseEntity.ok(paymentService.updatePayment(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(
            @PathVariable Long id) {

        paymentService.deletePayment(id);

        return ResponseEntity.noContent().build();
    }
}
