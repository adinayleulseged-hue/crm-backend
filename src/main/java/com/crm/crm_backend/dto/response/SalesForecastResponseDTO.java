package com.crm.crm_backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesForecastResponseDTO {

    // Revenue
    private BigDecimal currentRevenue;

    private BigDecimal projectedRevenue;

    // Leads
    private long totalLeads;

    private long convertedLeads;

    private double leadConversionRate;

    // Opportunities
    private long totalOpportunities;

    private long wonOpportunities;

    private double opportunityWinRate;

    // Sales
    private long totalSalesOrders;

    // Finance
    private long totalInvoices;

    private long totalPayments;

    // Marketing
    private long totalCampaigns;

    private long activeCampaigns;
}
