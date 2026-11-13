package com.crm.crm_backend.service.core;

import com.crm.crm_backend.dto.request.OpportunityCreateDTO;
import com.crm.crm_backend.dto.response.OpportunityResponseDTO;
import com.crm.crm_backend.event.publisher.OpportunityEventPublisher;
import com.crm.crm_backend.mapper.OpportunityMapper;
import com.crm.crm_backend.model.entity.Opportunity;
import com.crm.crm_backend.model.enums.OpportunityStage;
import com.crm.crm_backend.model.enums.OpportunityType;
import com.crm.crm_backend.repository.ActivityLogRepository;
import com.crm.crm_backend.repository.CustomerRepository;
import com.crm.crm_backend.repository.LeadRepository;
import com.crm.crm_backend.repository.OpportunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private OpportunityMapper opportunityMapper;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private OpportunityEventPublisher opportunityEventPublisher;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private CampaignService campaignService;

    @Mock
    private QuotationService quotationService;

    @InjectMocks
    private OpportunityService opportunityService;

    private Opportunity opportunity;

    @BeforeEach
    void setUp() {

        opportunity = new Opportunity();

        opportunity.setId(10L);

        opportunity.setStage(
                OpportunityStage.PROPOSAL
        );

        opportunity.setExpectedCloseDate(
                LocalDate.now().plusDays(5)
        );
    }

    // ============================================================
    // CREATE - PAST EXPECTED CLOSE DATE
    // ============================================================

    @Test
    void createOpportunityShouldRejectPastExpectedCloseDate() {

        OpportunityCreateDTO dto =
                new OpportunityCreateDTO();

        dto.setLeadId(1L);

        dto.setCustomerId(2L);

        dto.setType(
                OpportunityType.NEW_BUSINESS
        );

        dto.setProbability(80);

        dto.setExpectedCloseDate(
                LocalDate.now().minusDays(1)
        );

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> opportunityService.createOpportunity(dto)
                );

        assertEquals(
                "Expected close date cannot be in the past.",
                ex.getMessage()
        );

        verify(
                opportunityRepository,
                never()
        ).save(any());
    }

    // ============================================================
    // UPDATE STAGE - CLOSED WON
    // ============================================================

    @Test
    void updateStageShouldSetActualCloseDateForClosedStages() {

        opportunity.setStage(
                OpportunityStage.PROPOSAL
        );

        when(
                opportunityRepository.findById(10L)
        ).thenReturn(
                Optional.of(opportunity)
        );

        when(
                opportunityRepository.save(
                        any(Opportunity.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        when(
                opportunityMapper.toResponseDTO(
                        any(Opportunity.class)
                )
        ).thenReturn(
                new OpportunityResponseDTO()
        );

        opportunityService.updateStage(
                10L,
                OpportunityStage.CLOSED_WON
        );

        assertEquals(
                OpportunityStage.CLOSED_WON,
                opportunity.getStage()
        );

        assertEquals(
                LocalDate.now(),
                opportunity.getActualCloseDate()
        );

        verify(
                opportunityRepository
        ).save(opportunity);

        verify(
                opportunityEventPublisher
        ).publishOpportunityStageChanged(
                opportunity,
                OpportunityStage.CLOSED_WON
        );
    }

    // ============================================================
    // UPDATE STAGE - INVALID TRANSITION
    // ============================================================

    @Test
    void updateStageShouldRejectInvalidStageTransition() {

        opportunity.setStage(
                OpportunityStage.CLOSED_WON
        );

        when(
                opportunityRepository.findById(10L)
        ).thenReturn(
                Optional.of(opportunity)
        );

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () -> opportunityService.updateStage(
                                10L,
                                OpportunityStage.NEW
                        )
                );

        assertEquals(
                "Opportunity cannot transition from CLOSED_WON to NEW",
                ex.getMessage()
        );

        verify(
                opportunityRepository,
                never()
        ).save(any(Opportunity.class));

        verify(
                opportunityEventPublisher,
                never()
        ).publishOpportunityStageChanged(
                any(Opportunity.class),
                any(OpportunityStage.class)
        );
    }
}