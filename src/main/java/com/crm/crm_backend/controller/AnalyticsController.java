package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.response.SalesForecastResponseDTO;
import com.crm.crm_backend.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'FINANCE_OFFICER')")
    public ResponseEntity<ApiResponse<SalesForecastResponseDTO>> getAnalytics() {

        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAnalytics()));
    }
}
