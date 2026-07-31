package com.crm.crm_backend.controller;


import com.crm.crm_backend.dto.request.OpportunityCreateDTO;
import com.crm.crm_backend.dto.request.OpportunityUpdateDTO;
import com.crm.crm_backend.dto.response.OpportunityResponseDTO;
import com.crm.crm_backend.model.enums.OpportunityStage;
import com.crm.crm_backend.service.core.OpportunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OpportunityController {

    private final OpportunityService opportunityService;

    @PostMapping
    public ResponseEntity<OpportunityResponseDTO> createOpportunity(
            @Valid @RequestBody OpportunityCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(opportunityService.createOpportunity(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpportunityResponseDTO> getOpportunityById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                opportunityService.getOpportunityById(id));
    }

    @GetMapping
    public ResponseEntity<Page<OpportunityResponseDTO>> getAllOpportunities(
            Pageable pageable) {

        return ResponseEntity.ok(
                opportunityService.getAllOpportunities(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OpportunityResponseDTO> updateOpportunity(
            @PathVariable Long id,
            @Valid @RequestBody OpportunityUpdateDTO dto) {

        return ResponseEntity.ok(
                opportunityService.updateOpportunity(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOpportunity(
            @PathVariable Long id) {

        opportunityService.deleteOpportunity(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stage")
    public ResponseEntity<OpportunityResponseDTO> updateStage(
            @PathVariable Long id,
            @RequestParam OpportunityStage stage) {

        return ResponseEntity.ok(
                opportunityService.updateStage(id, stage));
    }
}