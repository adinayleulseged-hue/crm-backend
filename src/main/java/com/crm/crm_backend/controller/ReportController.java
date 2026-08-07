package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.response.ReportResponseDTO;
import com.crm.crm_backend.service.analytics.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'FINANCE_OFFICER')")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> generateReport() {

        return ResponseEntity.ok(ApiResponse.success(reportService.generateReport()));
    }
}
