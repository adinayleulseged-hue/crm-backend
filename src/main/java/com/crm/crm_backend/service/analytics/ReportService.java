package com.crm.crm_backend.service.analytics;

import com.crm.crm_backend.dto.response.ReportResponseDTO;
import com.crm.crm_backend.model.enums.CampaignStatus;
import com.crm.crm_backend.model.enums.CustomerStatus;
import com.crm.crm_backend.model.enums.InvoiceStatus;
import com.crm.crm_backend.model.enums.LeadStatus;
import com.crm.crm_backend.model.enums.OpportunityStage;
import com.crm.crm_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ForecastingService forecastingService;
    private final KpiCalculatorService kpiCalculatorService;

    public ReportResponseDTO generateReport() {

        ReportResponseDTO dto = new ReportResponseDTO();

        dto.setTotalCustomers(customerRepository.countByDeletedFalse());
        dto.setActiveCustomers(
                customerRepository.countByStatusAndDeletedFalse(CustomerStatus.ACTIVE));

        dto.setTotalLeads(leadRepository.count());
        dto.setQualifiedLeads(leadRepository.countByStatus(LeadStatus.QUALIFIED));
        dto.setConvertedLeads(leadRepository.countByStatus(LeadStatus.CONVERTED));
        dto.setLeadConversionRate(kpiCalculatorService.rate(
                dto.getConvertedLeads(), dto.getTotalLeads()));

        dto.setTotalOpportunities(opportunityRepository.count());
        dto.setWonOpportunities(opportunityRepository.countByStage(OpportunityStage.CLOSED_WON));
        dto.setLostOpportunities(opportunityRepository.countByStage(OpportunityStage.CLOSED_LOST));
        dto.setOpenOpportunities(forecastingService.countOpenOpportunities());
        dto.setPipelineValue(forecastingService.weightedOpenPipeline());
        dto.setOpportunityWinRate(kpiCalculatorService.rate(
                dto.getWonOpportunities(), dto.getTotalOpportunities()));

        dto.setTotalSalesOrders(salesOrderRepository.count());

        dto.setTotalInvoices(invoiceRepository.count());
        dto.setPaidInvoices(invoiceRepository.countByStatus(InvoiceStatus.PAID));
        dto.setOverdueInvoices(invoiceRepository.countByStatus(InvoiceStatus.OVERDUE));
        dto.setOpenInvoiceBalance(kpiCalculatorService.coalesce(invoiceRepository.sumOpenBalanceAmount()));

        dto.setTotalPayments(paymentRepository.count());
        dto.setTotalRevenue(forecastingService.currentRevenue());
        dto.setProjectedRevenue(forecastingService.projectedRevenue());

        dto.setTotalCampaigns(campaignRepository.count());
        dto.setActiveCampaigns(campaignRepository.countByStatus(CampaignStatus.ACTIVE));

        return dto;
    }
}
