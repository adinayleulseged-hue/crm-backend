package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
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
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'fms_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<SalesOrderResponseDTO>> createSalesOrder(
            @Valid @RequestBody SalesOrderCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sales order created", salesOrderService.createSalesOrder(dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'fms_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<SalesOrderResponseDTO>> getSalesOrderById(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(salesOrderService.getSalesOrder(id)));
    }

    @GetMapping
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'fms_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<SalesOrderResponseDTO>>> getAllSalesOrders(
            @RequestParam(required = false) Long customerId,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(salesOrderService.getAllSalesOrders(customerId, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'fms_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<SalesOrderResponseDTO>> updateSalesOrder(
            @PathVariable Long id,
            @Valid @RequestBody SalesOrderUpdateDTO dto) {

        return ResponseEntity.ok(ApiResponse.success(salesOrderService.updateSalesOrder(id, dto)));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'fms_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<SalesOrderResponseDTO>> confirmSalesOrder(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Sales order confirmed",
                salesOrderService.confirmSalesOrder(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'fms_user', 'customer', 'user', 'auditor', 'sales_manager', 'sales_officer', 'ADMIN', 'AUDITOR', 'CUSTOMER', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteSalesOrder(
            @PathVariable Long id) {

        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Sales order deleted", null));
    }

    // Generate invoice from sales order (SalesOrder → Invoice workflow)
    @PostMapping("/{id}/generate-invoice")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'fms_user', 'sales_manager', 'sales_officer', 'finance_officer', 'ADMIN', 'SALES_MANAGER', 'SALES_OFFICER', 'FINANCE_OFFICER', 'ROLE_ADMIN', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER', 'ROLE_FINANCE_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<com.crm.crm_backend.dto.response.InvoiceResponseDTO>> generateInvoice(
            @PathVariable Long id) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Invoice generated from sales order",
                        salesOrderService.generateInvoiceFromOrder(id)));
    }
}
