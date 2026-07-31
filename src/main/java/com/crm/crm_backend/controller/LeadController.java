package com.crm.crm_backend.controller;

import com.crm.crm_backend.dto.request.LeadConvertDTO;
import com.crm.crm_backend.dto.request.LeadCreateDTO;
import com.crm.crm_backend.dto.request.LeadUpdateDTO;
import com.crm.crm_backend.dto.response.LeadResponseDTO;
import com.crm.crm_backend.service.core.LeadService;import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeadController {

    private final LeadService leadService;

    /**
     * Create Lead
     */
    @PostMapping
    public ResponseEntity<LeadResponseDTO> createLead(
            @Valid @RequestBody LeadCreateDTO dto) {

        LeadResponseDTO response = leadService.createLead(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get Lead by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeadResponseDTO> getLeadById(
            @PathVariable Long id) {

        return ResponseEntity.ok(leadService.getLeadById(id));
    }

    /**
     * Get All Leads (Pagination)
     */
    @GetMapping
    public ResponseEntity<Page<LeadResponseDTO>> getAllLeads(
            Pageable pageable) {

        return ResponseEntity.ok(leadService.getAllLeads(pageable));
    }

    /**
     * Update Lead
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeadResponseDTO> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadUpdateDTO dto) {

        return ResponseEntity.ok(leadService.updateLead(id, dto));
    }

    /**
     * Delete Lead
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(
            @PathVariable Long id) {

        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Convert Lead
     */
    @PatchMapping("/{id}/convert")
    public ResponseEntity<LeadResponseDTO> convertLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadConvertDTO dto)
{

        return ResponseEntity.ok(leadService.convertLeadToCustomer(id, dto));
    }
}