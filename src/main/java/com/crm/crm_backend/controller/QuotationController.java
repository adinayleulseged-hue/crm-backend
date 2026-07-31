package com.crm.crm_backend.controller;


import com.crm.crm_backend.dto.request.QuotationCreateDTO;
import com.crm.crm_backend.dto.request.QuotationUpdateDTO;
import com.crm.crm_backend.dto.response.QuotationResponseDTO;
import com.crm.crm_backend.service.core.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuotationController {

    private final QuotationService quotationService;

    @PostMapping
    public ResponseEntity<QuotationResponseDTO> createQuotation(
            @Valid @RequestBody QuotationCreateDTO dto) {

        return new ResponseEntity<>(
                quotationService.createQuotation(dto),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuotationResponseDTO> getQuotation(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                quotationService.getQuotation(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<QuotationResponseDTO>> getAllQuotations(
            Pageable pageable) {

        return ResponseEntity.ok(
                quotationService.getAllQuotations(pageable)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuotationResponseDTO> updateQuotation(
            @PathVariable Long id,
            @Valid @RequestBody QuotationUpdateDTO dto) {

        return ResponseEntity.ok(
                quotationService.updateQuotation(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuotation(
            @PathVariable Long id) {

        quotationService.deleteQuotation(id);

        return ResponseEntity.noContent().build();
    }
}
