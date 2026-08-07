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

        BigDecimal currentRevenue = paymentRepository.sumCompletedAmount();
        if (currentRevenue == null) {
            currentRevenue = BigDecimal.ZERO;
        }
        dto.setCurrentRevenue(currentRevenue);

        BigDecimal projected = invoiceRepository.sumTotalAmountExcludingCancelled();
        if (projected == null) {
            projected = BigDecimal.ZERO;
        }
        dto.setProjectedRevenue(projected);

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

        dto.setTotalSalesOrders(salesOrderRepository.count());

        dto.setTotalInvoices(invoiceRepository.count());
        dto.setTotalPayments(paymentRepository.count());

        dto.setTotalCampaigns(campaignRepository.count());
        dto.setActiveCampaigns(
                campaignRepository.countByStatus(
                        CampaignStatus.ACTIVE));

        return dto;
    }
}
