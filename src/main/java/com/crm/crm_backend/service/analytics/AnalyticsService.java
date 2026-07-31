package com.crm.crm_backend.service.analytics;


import com.crm.crm_backend.dto.response.SalesForecastResponseDTO;
import com.crm.crm_backend.model.enums.CampaignStatus;
import com.crm.crm_backend.model.enums.LeadStatus;
import com.crm.crm_backend.model.enums.OpportunityStage;
import com.crm.crm_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CampaignRepository campaignRepository;

    public SalesForecastResponseDTO getAnalytics() {

        SalesForecastResponseDTO dto =
                new SalesForecastResponseDTO();

        // Revenue (placeholder until revenue calculations are added)
        dto.setCurrentRevenue(BigDecimal.ZERO);
        dto.setProjectedRevenue(BigDecimal.ZERO);

        // Leads
        long totalLeads = leadRepository.count();
        long convertedLeads =
                leadRepository.countByStatus(LeadStatus.CONVERTED);

        dto.setTotalLeads(totalLeads);
        dto.setConvertedLeads(convertedLeads);

        if (totalLeads > 0) {
            dto.setLeadConversionRate(
                    (convertedLeads * 100.0) / totalLeads);
        } else {
            dto.setLeadConversionRate(0.0);
        }

        // Opportunities
        long totalOpp = opportunityRepository.count();
        long wonOpp =
                opportunityRepository.countByStage(
                        OpportunityStage.CLOSED_WON);

        dto.setTotalOpportunities(totalOpp);
        dto.setWonOpportunities(wonOpp);

        if (totalOpp > 0) {
            dto.setOpportunityWinRate(
                    (wonOpp * 100.0) / totalOpp);
        } else {
            dto.setOpportunityWinRate(0.0);
        }

        // Sales
        dto.setTotalSalesOrders(salesOrderRepository.count());

        // Finance
        dto.setTotalInvoices(invoiceRepository.count());
        dto.setTotalPayments(paymentRepository.count());

        // Marketing
        dto.setTotalCampaigns(campaignRepository.count());
        dto.setActiveCampaigns(
                campaignRepository.countByStatus(
                        CampaignStatus.ACTIVE));

        return dto;
    }
}