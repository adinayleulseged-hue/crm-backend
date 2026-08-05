package com.crm.crm_backend.controller;

import com.crm.crm_backend.dto.request.SalesOrderCreateDTO;
import com.crm.crm_backend.dto.request.SalesOrderUpdateDTO;
import com.crm.crm_backend.dto.response.SalesOrderResponseDTO;
import com.crm.crm_backend.service.core.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<SalesOrderResponseDTO> createSalesOrder(
            @Valid @RequestBody SalesOrderCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(salesOrderService.createSalesOrder(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'FINANCE_OFFICER', 'EMPLOYEE')")
    public ResponseEntity<SalesOrderResponseDTO> getSalesOrderById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                salesOrderService.getSalesOrder(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER', 'FINANCE_OFFICER')")
    public ResponseEntity<Page<SalesOrderResponseDTO>> getAllSalesOrders(
            Pageable pageable) {

        return ResponseEntity.ok(
                salesOrderService.getAllSalesOrders(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<SalesOrderResponseDTO> updateSalesOrder(
            @PathVariable Long id,
            @Valid @RequestBody SalesOrderUpdateDTO dto) {

        return ResponseEntity.ok(
                salesOrderService.updateSalesOrder(id, dto));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_OFFICER')")
    public ResponseEntity<SalesOrderResponseDTO> confirmSalesOrder(
            @PathVariable Long id) {

        return ResponseEntity.ok(salesOrderService.confirmSalesOrder(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSalesOrder(
            @PathVariable Long id) {

        salesOrderService.deleteSalesOrder(id);

        return ResponseEntity.noContent().build();
    }
}
