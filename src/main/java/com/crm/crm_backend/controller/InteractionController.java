package com.crm.crm_backend.controller;

import com.crm.crm_backend.dto.request.InteractionCreateDTO;
import com.crm.crm_backend.dto.request.InteractionUpdateDTO;
import com.crm.crm_backend.dto.response.InteractionResponseDTO;
import com.crm.crm_backend.service.core.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<InteractionResponseDTO> createInteraction(
            @Valid @RequestBody InteractionCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interactionService.createInteraction(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<InteractionResponseDTO> getInteraction(
            @PathVariable Long id) {

        return ResponseEntity.ok(interactionService.getInteraction(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<Page<InteractionResponseDTO>> getAllInteractions(
            Pageable pageable) {

        return ResponseEntity.ok(interactionService.getAllInteractions(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<InteractionResponseDTO> updateInteraction(
            @PathVariable Long id,
            @Valid @RequestBody InteractionUpdateDTO dto) {

        return ResponseEntity.ok(
                interactionService.updateInteraction(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInteraction(
            @PathVariable Long id) {

        interactionService.deleteInteraction(id);

        return ResponseEntity.noContent().build();
    }
}
