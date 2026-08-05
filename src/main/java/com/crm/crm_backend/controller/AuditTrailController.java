package com.crm.crm_backend.controller;

import com.crm.crm_backend.dto.response.AuditTrailResponseDTO;
import com.crm.crm_backend.model.enums.AuditAction;
import com.crm.crm_backend.model.enums.AuditModule;
import com.crm.crm_backend.service.core.AuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-trails")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuditTrailController {

    private final AuditTrailService auditTrailService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    public List<AuditTrailResponseDTO> getAllAuditTrails() {
        return auditTrailService.getAllAuditTrails();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    public AuditTrailResponseDTO getAuditTrailById(@PathVariable Long id) {
        return auditTrailService.getAuditTrailById(id);
    }

    @GetMapping("/module/{module}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    public List<AuditTrailResponseDTO> getByModule(
            @PathVariable AuditModule module) {

        return auditTrailService.getByModule(module);
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    public List<AuditTrailResponseDTO> getByAction(
            @PathVariable AuditAction action) {

        return auditTrailService.getByAction(action);
    }

    @GetMapping("/user/{performedBy}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    public List<AuditTrailResponseDTO> getByPerformedBy(
            @PathVariable String performedBy) {

        return auditTrailService.getByPerformedBy(performedBy);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    public List<AuditTrailResponseDTO> getByDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {

        return auditTrailService.getByDateRange(start, end);
    }
}
