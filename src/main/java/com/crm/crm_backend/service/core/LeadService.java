package com.crm.crm_backend.service.core;

import com.crm.crm_backend.dto.request.CustomerCreateDTO;
import com.crm.crm_backend.dto.request.LeadConvertDTO;
import com.crm.crm_backend.dto.request.LeadCreateDTO;
import com.crm.crm_backend.dto.request.LeadUpdateDTO;
import com.crm.crm_backend.dto.response.CustomerResponseDTO;
import com.crm.crm_backend.dto.response.LeadResponseDTO;
import com.crm.crm_backend.event.publisher.LeadEventPublisher;
import com.crm.crm_backend.exception.LeadConversionException;
import com.crm.crm_backend.exception.LeadNotFoundException;
import com.crm.crm_backend.mapper.LeadMapper;
import com.crm.crm_backend.model.entity.Lead;
import com.crm.crm_backend.model.enums.CustomerType;
import com.crm.crm_backend.model.enums.LeadStatus;
import com.crm.crm_backend.repository.LeadRepository;
import com.crm.crm_backend.util.calculator.LeadScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;
    private final CustomerService customerService;
    private final LeadScoreCalculator leadScoreCalculator;
    private final LeadEventPublisher leadEventPublisher;

    @Transactional
    public LeadResponseDTO createLead(LeadCreateDTO dto) {

        log.info("Creating lead {}", dto.getEmail());

        if (dto.getEmail() != null &&
                leadRepository.existsByEmail(dto.getEmail())) {

            throw new RuntimeException("Lead email already exists.");
        }

        Lead lead = leadMapper.toEntity(dto);

        lead.setStatus(LeadStatus.NEW);

        lead.setLeadScore(
                leadScoreCalculator.calculate(lead)
        );

        Lead saved = leadRepository.save(lead);

        leadEventPublisher.publishLeadCreated(saved);

        return leadMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public LeadResponseDTO getLeadById(Long id) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new LeadNotFoundException(
                                "Lead not found : " + id));

        return leadMapper.toResponseDTO(lead);
    }

    @Transactional(readOnly = true)
    public Page<LeadResponseDTO> getAllLeads(Pageable pageable) {

        return leadRepository.findAll(pageable)
                .map(leadMapper::toResponseDTO);
    }

    @Transactional
    public LeadResponseDTO updateLead(Long id,
                                      LeadUpdateDTO dto) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new LeadNotFoundException(
                                "Lead not found : " + id));

        leadMapper.updateEntityFromDTO(dto, lead);

        lead.setLeadScore(
                leadScoreCalculator.calculate(lead)
        );

        Lead updated = leadRepository.save(lead);

        return leadMapper.toResponseDTO(updated);
    }

    @Transactional
    public void deleteLead(Long id) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new LeadNotFoundException(
                                "Lead not found : " + id));

        leadRepository.delete(lead);

        log.info("Lead deleted {}", id);
    }

    @Transactional
    public LeadResponseDTO updateLeadStatus(Long id,
                                            LeadStatus newStatus) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new LeadNotFoundException(
                                "Lead not found : " + id));

        lead.setStatus(newStatus);

        if (newStatus == LeadStatus.QUALIFIED) {

            lead.setLeadScore(
                    leadScoreCalculator.calculate(lead));

            leadEventPublisher.publishLeadQualified(lead);
        }

        Lead updated = leadRepository.save(lead);

        return leadMapper.toResponseDTO(updated);
    }

    @Transactional
    public LeadResponseDTO convertLeadToCustomer(Long id,
                                                 LeadConvertDTO dto) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new LeadNotFoundException(
                                "Lead not found : " + id));

        if (lead.getStatus() != LeadStatus.QUALIFIED) {

            throw new LeadConversionException(
                    "Lead must be QUALIFIED before conversion.");
        }

        CustomerCreateDTO customerDTO =
                buildCustomerFromLead(lead);

        CustomerResponseDTO customer =
                customerService.createCustomer(customerDTO);

        lead.setStatus(LeadStatus.CONVERTED);

        lead.setConvertedAt(LocalDateTime.now());

        lead.setConversionReason(
                dto.getConversionReason());

        /*
         * If CustomerResponseDTO contains getId(),
         * load the Customer entity and set:
         *
         * lead.setConvertedCustomer(customerEntity);
         *
         * Otherwise leave this until CustomerService
         * returns the entity.
         */

        Lead converted = leadRepository.save(lead);

        leadEventPublisher.publishLeadConverted(converted);

        log.info("Lead {} converted.", id);

        return leadMapper.toResponseDTO(converted);
    }

    private CustomerCreateDTO buildCustomerFromLead(Lead lead) {

        CustomerCreateDTO dto =
                new CustomerCreateDTO();

        dto.setCustomerName(
                lead.getFirstName() + " " + lead.getLastName());

        dto.setOrganizationName(
                lead.getCompany());

        dto.setCustomerType(
                CustomerType.ORGANIZATION);

        dto.setEmail(
                lead.getEmail());

        dto.setPhone(
                lead.getPhone());

        dto.setIndustry(
                lead.getIndustry());

        return dto;
    }

}