package com.crm.crm_backend.service.analytics;

import com.crm.crm_backend.dto.response.DashboardKpiResponseDTO;
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
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CampaignRepository campaignRepository;

    public DashboardKpiResponseDTO getDashboardKpis() {

        DashboardKpiResponseDTO dto = new DashboardKpiResponseDTO();

        dto.setTotalCustomers(customerRepository.count());

        dto.setTotalLeads(leadRepository.count());
        dto.setNewLeads(leadRepository.countByStatus(LeadStatus.NEW));
        dto.setQualifiedLeads(
                leadRepository.countByStatus(LeadStatus.QUALIFIED));
        dto.setConvertedLeads(
                leadRepository.countByStatus(LeadStatus.CONVERTED));

        dto.setTotalOpportunities(opportunityRepository.count());

        dto.setOpenOpportunities(
                opportunityRepository.countByStage(OpportunityStage.NEW)
                        + opportunityRepository.countByStage(OpportunityStage.NEEDS_ANALYSIS)
                        + opportunityRepository.countByStage(OpportunityStage.QUALIFICATION)
                        + opportunityRepository.countByStage(OpportunityStage.PROPOSAL)
                        + opportunityRepository.countByStage(OpportunityStage.NEGOTIATION)
        );

        dto.setWonOpportunities(
                opportunityRepository.countByStage(OpportunityStage.CLOSED_WON));

        dto.setLostOpportunities(
                opportunityRepository.countByStage(OpportunityStage.CLOSED_LOST));

        dto.setTotalSalesOrders(salesOrderRepository.count());

        dto.setTotalInvoices(invoiceRepository.count());
        dto.setTotalPayments(paymentRepository.count());

        BigDecimal paymentRevenue = paymentRepository.sumCompletedAmount();
        BigDecimal invoicePaid = invoiceRepository.sumPaidAmountExcludingCancelled();
        dto.setTotalRevenue(paymentRevenue != null && paymentRevenue.compareTo(BigDecimal.ZERO) > 0
                ? paymentRevenue
                : (invoicePaid != null ? invoicePaid : BigDecimal.ZERO));

        dto.setTotalCampaigns(campaignRepository.count());

        dto.setActiveCampaigns(
                campaignRepository.countByStatus(CampaignStatus.ACTIVE));

        return dto;
    }
}
