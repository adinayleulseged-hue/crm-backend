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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<CampaignResponseDTO> createCampaign(
            @Valid @RequestBody CampaignCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.createCampaign(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponseDTO> getCampaign(
            @PathVariable Long id) {

        return ResponseEntity.ok(campaignService.getCampaign(id));
    }

    @GetMapping
    public ResponseEntity<Page<CampaignResponseDTO>> getAllCampaigns(
            Pageable pageable) {

        return ResponseEntity.ok(campaignService.getAllCampaigns(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponseDTO> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignUpdateDTO dto) {

        return ResponseEntity.ok(
                campaignService.updateCampaign(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(
            @PathVariable Long id) {

        campaignService.deleteCampaign(id);

        return ResponseEntity.noContent().build();
    }
}

