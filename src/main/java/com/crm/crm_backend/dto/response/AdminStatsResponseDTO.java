package com.crm.crm_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponseDTO {
    private long totalUsers;
    private long totalCustomers;
    private long totalLeads;
    private long totalOrders;
    private long totalAuditLogs;
    private String databaseStatus;
    private String authMode;
    private String systemVersion;
}
