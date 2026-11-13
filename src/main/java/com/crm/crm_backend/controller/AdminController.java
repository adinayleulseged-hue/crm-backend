package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.response.AdminStatsResponseDTO;
import com.crm.crm_backend.repository.AuditTrailRepository;
import com.crm.crm_backend.repository.CustomerRepository;
import com.crm.crm_backend.repository.LeadRepository;
import com.crm.crm_backend.repository.SalesOrderRepository;
import com.crm.crm_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final AuditTrailRepository auditTrailRepository;

    @Value("${crm.security.auth-mode:legacy}")
    private String authMode;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponseDTO>> getAdminStats() {
        AdminStatsResponseDTO stats = AdminStatsResponseDTO.builder()
                .totalUsers(userRepository.count())
                .totalCustomers(customerRepository.count())
                .totalLeads(leadRepository.count())
                .totalOrders(salesOrderRepository.count())
                .totalAuditLogs(auditTrailRepository.count())
                .databaseStatus("CONNECTED (PostgreSQL 17)")
                .authMode(authMode.toUpperCase())
                .systemVersion("INSA ERP v1.0.0-PROD")
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
