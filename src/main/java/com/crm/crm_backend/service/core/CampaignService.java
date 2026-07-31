package com.crm.crm_backend.service.core;

import com.crm.crm_backend.dto.request.CampaignCreateDTO;
import com.crm.crm_backend.dto.request.CampaignUpdateDTO;
import com.crm.crm_backend.dto.response.CampaignResponseDTO;
import com.crm.crm_backend.mapper.CampaignMapper;
import com.crm.crm_backend.model.entity.Campaign;
import com.crm.crm_backend.model.enums.CampaignStatus;
import com.crm.crm_backend.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;

    public CampaignResponseDTO createCampaign(CampaignCreateDTO dto) {

        Campaign campaign = campaignMapper.toEntity(dto);

        campaign.setCampaignNumber("CMP-" + System.currentTimeMillis());
        campaign.setStatus(CampaignStatus.PLANNED);
        campaign.setActualRevenue(java.math.BigDecimal.ZERO);
        campaign.setActualLeads(0);

        Campaign saved = campaignRepository.save(campaign);

        return campaignMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public CampaignResponseDTO getCampaign(Long id) {

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        return campaignMapper.toResponseDTO(campaign);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponseDTO> getAllCampaigns(Pageable pageable) {

        return campaignRepository.findAll(pageable)
                .map(campaignMapper::toResponseDTO);
    }

    public CampaignResponseDTO updateCampaign(Long id,
                                              CampaignUpdateDTO dto) {

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        campaignMapper.updateEntityFromDTO(dto, campaign);

        Campaign updated = campaignRepository.save(campaign);

        return campaignMapper.toResponseDTO(updated);
    }

    public void deleteCampaign(Long id) {

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        campaignRepository.delete(campaign);

        log.info("Campaign {} deleted", id);
    }
}
