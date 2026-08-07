package com.crm.crm_backend.service.analytics;

import com.crm.crm_backend.dto.response.ReportResponseDTO;
import com.crm.crm_backend.model.enums.CampaignStatus;
import com.crm.crm_backend.model.enums.InvoiceStatus;
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

        dto.setTotalCustomers(customerRepository.count());
        dto.setActiveCustomers(customerRepository.count());

        dto.setTotalLeads(leadRepository.count());
        dto.setQualifiedLeads(
                leadRepository.countByStatus(LeadStatus.QUALIFIED));
        dto.setConvertedLeads(
                leadRepository.countByStatus(LeadStatus.CONVERTED));

        dto.setTotalOpportunities(opportunityRepository.count());
        dto.setWonOpportunities(
                opportunityRepository.countByStage(OpportunityStage.CLOSED_WON));
        dto.setLostOpportunities(
                opportunityRepository.countByStage(OpportunityStage.CLOSED_LOST));

        dto.setTotalSalesOrders(salesOrderRepository.count());

        dto.setTotalInvoices(invoiceRepository.count());
        dto.setPaidInvoices(invoiceRepository.countByStatus(InvoiceStatus.PAID));

        dto.setTotalPayments(paymentRepository.count());

        BigDecimal revenue = paymentRepository.sumCompletedAmount();
        dto.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        dto.setTotalCampaigns(campaignRepository.count());
        dto.setActiveCampaigns(
                campaignRepository.countByStatus(CampaignStatus.ACTIVE));

        return dto;
    }
}
