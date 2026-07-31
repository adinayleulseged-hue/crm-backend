package com.crm.crm_backend.service.core;


import com.crm.crm_backend.dto.request.QuotationCreateDTO;
import com.crm.crm_backend.dto.request.QuotationUpdateDTO;
import com.crm.crm_backend.dto.response.QuotationResponseDTO;
import com.crm.crm_backend.exception.QuotationNotFoundException;
import com.crm.crm_backend.mapper.QuotationMapper;
import com.crm.crm_backend.model.entity.Customer;
import com.crm.crm_backend.model.entity.Opportunity;
import com.crm.crm_backend.model.entity.Quotation;
import com.crm.crm_backend.model.enums.QuotationStatus;
import com.crm.crm_backend.repository.CustomerRepository;
import com.crm.crm_backend.repository.OpportunityRepository;
import com.crm.crm_backend.repository.QuotationRepository;
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
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final QuotationMapper quotationMapper;

    public QuotationResponseDTO createQuotation(QuotationCreateDTO dto) {

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Opportunity opportunity = opportunityRepository.findById(dto.getOpportunityId())
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));

        Quotation quotation = quotationMapper.toEntity(dto);

        quotation.setCustomer(customer);
        quotation.setOpportunity(opportunity);

        quotation.setQuotationNumber("QT-" + System.currentTimeMillis());
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setActive(true);

        Quotation saved = quotationRepository.save(quotation);

        log.info("Quotation {} created", saved.getQuotationNumber());

        return quotationMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public QuotationResponseDTO getQuotation(Long id) {

        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() ->
                        new QuotationNotFoundException("Quotation not found: " + id));

        return quotationMapper.toResponseDTO(quotation);
    }

    @Transactional(readOnly = true)
    public Page<QuotationResponseDTO> getAllQuotations(Pageable pageable) {

        return quotationRepository.findAll(pageable)
                .map(quotationMapper::toResponseDTO);
    }

    public QuotationResponseDTO updateQuotation(Long id,
                                                QuotationUpdateDTO dto) {

        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() ->
                        new QuotationNotFoundException("Quotation not found: " + id));

        quotationMapper.updateEntityFromDTO(dto, quotation);

        Quotation updated = quotationRepository.save(quotation);

        return quotationMapper.toResponseDTO(updated);
    }

    public void deleteQuotation(Long id) {

        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() ->
                        new QuotationNotFoundException("Quotation not found: " + id));

        quotationRepository.delete(quotation);

        log.info("Quotation {} deleted", id);
    }
}