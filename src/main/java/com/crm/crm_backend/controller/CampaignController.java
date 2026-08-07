package com.crm.crm_backend.controller;

import com.crm.crm_backend.dto.request.CampaignCreateDTO;
import com.crm.crm_backend.dto.request.CampaignUpdateDTO;
import com.crm.crm_backend.dto.response.CampaignResponseDTO;
import com.crm.crm_backend.service.core.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<CampaignResponseDTO> createCampaign(
            @Valid @RequestBody CampaignCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.createCampaign(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<CampaignResponseDTO> getCampaign(
            @PathVariable Long id) {

        return ResponseEntity.ok(campaignService.getCampaign(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<Page<CampaignResponseDTO>> getAllCampaigns(
            Pageable pageable) {

        return ResponseEntity.ok(campaignService.getAllCampaigns(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<CampaignResponseDTO> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignUpdateDTO dto) {

        return ResponseEntity.ok(
                campaignService.updateCampaign(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCampaign(
            @PathVariable Long id) {

        campaignService.deleteCampaign(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<CampaignResponseDTO> activateCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.activateCampaign(id));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<CampaignResponseDTO> completeCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.completeCampaign(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<CampaignResponseDTO> cancelCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.cancelCampaign(id));
    }
}
