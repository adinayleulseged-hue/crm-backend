package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.response.ReportResponseDTO;
import com.crm.crm_backend.service.analytics.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/export.csv")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'fms_user', 'customer', 'user', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER') or isAuthenticated()")
    public ResponseEntity<String> exportReportCsv() {
        String csv = reportService.exportReportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pipeline-revenue-report.csv\"")
                .contentType(new MediaType("text", "csv"))
                .body(csv);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'fms_user', 'customer', 'user', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> generateReport() {

        return ResponseEntity.ok(ApiResponse.success(reportService.generateReport()));
    }
}
