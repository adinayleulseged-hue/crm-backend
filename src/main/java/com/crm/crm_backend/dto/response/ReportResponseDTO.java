package com.crm.crm_backend.dto.response;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportResponseDTO {

    // Customers
    private long totalCustomers;
    private long activeCustomers;

    // Leads
    private long totalLeads;
    private long qualifiedLeads;
    private long convertedLeads;

    // Opportunities
    private long totalOpportunities;
    private long wonOpportunities;
    private long lostOpportunities;

    // Sales
    private long totalSalesOrders;

    // Finance
    private long totalInvoices;
    private long paidInvoices;
    private long totalPayments;

    // Revenue
    private BigDecimal totalRevenue;

    // Marketing
    private long totalCampaigns;
    private long activeCampaigns;
}
