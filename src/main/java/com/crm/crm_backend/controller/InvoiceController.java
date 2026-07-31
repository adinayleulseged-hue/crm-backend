package com.crm.crm_backend.controller;


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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(
            @Valid @RequestBody InvoiceCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoice(
            @PathVariable Long id) {

        return ResponseEntity.ok(invoiceService.getInvoice(id));
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponseDTO>> getAllInvoices(
            Pageable pageable) {

        return ResponseEntity.ok(invoiceService.getAllInvoices(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceUpdateDTO dto) {

        return ResponseEntity.ok(
                invoiceService.updateInvoice(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(
            @PathVariable Long id) {

        invoiceService.deleteInvoice(id);

        return ResponseEntity.noContent().build();
    }
}