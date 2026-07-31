package com.crm.crm_backend.dto.response;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardKpiResponseDTO {

    // Customers
    private long totalCustomers;

    // Leads
    private long totalLeads;
    private long newLeads;
    private long qualifiedLeads;
    private long convertedLeads;

    // Opportunities
    private long totalOpportunities;
    private long openOpportunities;
    private long wonOpportunities;
    private long lostOpportunities;

    // Sales
    private long totalSalesOrders;

    // Finance
    private long totalInvoices;
    private long totalPayments;

    // Revenue
    private BigDecimal totalRevenue;

    // Marketing
    private long totalCampaigns;
    private long activeCampaigns;
}