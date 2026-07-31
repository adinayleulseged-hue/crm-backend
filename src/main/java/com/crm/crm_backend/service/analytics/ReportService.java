package com.crm.crm_backend.service.analytics;


import com.crm.crm_backend.dto.response.ReportResponseDTO;
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
public class ReportService {

    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CampaignRepository campaignRepository;

    public ReportResponseDTO generateReport() {

        ReportResponseDTO dto = new ReportResponseDTO();

        // Customers
        dto.setTotalCustomers(customerRepository.count());

        // If you have CustomerStatus.ACTIVE, replace this with countByStatus(...)
        dto.setActiveCustomers(customerRepository.count());

        // Leads
        dto.setTotalLeads(leadRepository.count());
        dto.setQualifiedLeads(
                leadRepository.countByStatus(LeadStatus.QUALIFIED));
        dto.setConvertedLeads(
                leadRepository.countByStatus(LeadStatus.CONVERTED));

        // Opportunities
        dto.setTotalOpportunities(opportunityRepository.count());
        dto.setWonOpportunities(
                opportunityRepository.countByStage(OpportunityStage.CLOSED_WON));
        dto.setLostOpportunities(
                opportunityRepository.countByStage(OpportunityStage.CLOSED_LOST));

        // Sales
        dto.setTotalSalesOrders(salesOrderRepository.count());

        // Finance
        dto.setTotalInvoices(invoiceRepository.count());

        // Replace later with actual paid invoice count
        dto.setPaidInvoices(invoiceRepository.count());

        dto.setTotalPayments(paymentRepository.count());

        // Replace later with SUM(invoice.totalAmount)
        dto.setTotalRevenue(BigDecimal.ZERO);

        // Campaigns
        dto.setTotalCampaigns(campaignRepository.count());
        dto.setActiveCampaigns(
                campaignRepository.countByStatus(CampaignStatus.ACTIVE));

        return dto;
    }
}