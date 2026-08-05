package com.crm.crm_backend.controller;

import com.crm.crm_backend.dto.response.DashboardKpiResponseDTO;
import com.crm.crm_backend.service.analytics.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'FINANCE_OFFICER')")
    public ResponseEntity<DashboardKpiResponseDTO> getDashboard() {

        return ResponseEntity.ok(
                dashboardService.getDashboardKpis()
        );
    }
}
