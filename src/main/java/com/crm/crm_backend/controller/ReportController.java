package com.crm.crm_backend.controller;


import com.crm.crm_backend.dto.response.ReportResponseDTO;
import com.crm.crm_backend.service.analytics.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<ReportResponseDTO> generateReport() {

        return ResponseEntity.ok(
                reportService.generateReport()
        );
    }
}