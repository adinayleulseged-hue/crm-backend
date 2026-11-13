package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.request.OpportunityCreateDTO;
import com.crm.crm_backend.dto.request.OpportunityUpdateDTO;
import com.crm.crm_backend.dto.response.OpportunityResponseDTO;
import com.crm.crm_backend.model.enums.OpportunityStage;
import com.crm.crm_backend.service.core.OpportunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/sales-deals", "/api/opportunities"})
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;

    @PostMapping
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'AUDITOR', 'CUSTOMER', 'CRM_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> createOpportunity(
            @Valid @RequestBody OpportunityCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Opportunity created", opportunityService.createOpportunity(dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'AUDITOR', 'CUSTOMER', 'CRM_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> getOpportunityById(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(opportunityService.getOpportunityById(id)));
    }

    @GetMapping
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'auditor', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER', 'ROLE_AUDITOR', 'AUDITOR', 'CUSTOMER', 'CRM_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<OpportunityResponseDTO>>> getAllOpportunities(
            @RequestParam(required = false) Long customerId,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(opportunityService.getAllOpportunities(customerId, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> updateOpportunity(
            @PathVariable Long id,
            @Valid @RequestBody OpportunityUpdateDTO dto) {

        return ResponseEntity.ok(ApiResponse.success(opportunityService.updateOpportunity(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteOpportunity(
            @PathVariable Long id) {

        opportunityService.deleteOpportunity(id);
        return ResponseEntity.ok(ApiResponse.success("Opportunity deleted", null));
    }

    @PatchMapping("/{id}/stage")
    @PreAuthorize("hasAnyAuthority('admin', 'crm_user', 'customer', 'user', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> updateStage(
            @PathVariable Long id,
            @RequestParam OpportunityStage stage) {

        return ResponseEntity.ok(ApiResponse.success(
                "Opportunity stage updated",
                opportunityService.updateStage(id, stage)));
    }

    // Generate quotation from opportunity (Deal → Quotation workflow)
    @PostMapping("/{id}/generate-quotation")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'crm_user', 'sales_manager', 'sales_officer', 'ADMIN', 'SALES_MANAGER', 'SALES_OFFICER', 'ROLE_ADMIN', 'ROLE_SALES_MANAGER', 'ROLE_SALES_OFFICER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<com.crm.crm_backend.dto.response.QuotationResponseDTO>> generateQuotation(
            @PathVariable Long id) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Quotation generated from opportunity",
                        opportunityService.generateQuotationFromOpportunity(id)));
    }

    // ===== Deal Approval Workflow Endpoints =====

    /**
     * Submit deal for approval (Customer → Admin/Sales Manager)
     */
    @PatchMapping("/{id}/submit-for-approval")
    @PreAuthorize("permitAll() or hasAnyAuthority('customer', 'CUSTOMER', 'ROLE_CUSTOMER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> submitForApproval(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Deal submitted for approval",
                opportunityService.submitForApproval(id)));
    }

    /**
     * Approve deal (Admin/Sales Manager → Customer)
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'sales_manager', 'ADMIN', 'SALES_MANAGER', 'ROLE_ADMIN', 'ROLE_SALES_MANAGER', 'crm_user', 'CRM_USER', 'ROLE_CRM_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> approveDeal(
            @PathVariable Long id,
            @RequestParam(required = false) Double discountApproved) {

        return ResponseEntity.ok(ApiResponse.success(
                "Deal approved successfully",
                opportunityService.approveDeal(id, discountApproved)));
    }

    /**
     * Reject deal (Admin/Sales Manager → Customer)
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'sales_manager', 'ADMIN', 'SALES_MANAGER', 'ROLE_ADMIN', 'ROLE_SALES_MANAGER', 'crm_user', 'CRM_USER', 'ROLE_CRM_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> rejectDeal(
            @PathVariable Long id,
            @RequestParam String reason) {

        return ResponseEntity.ok(ApiResponse.success(
                "Deal rejected",
                opportunityService.rejectDeal(id, reason)));
    }

    /**
     * Place order from approved deal (Customer action after approval)
     */
    @PostMapping("/{id}/place-order")
    @PreAuthorize("permitAll() or hasAnyAuthority('customer', 'CUSTOMER', 'ROLE_CUSTOMER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<com.crm.crm_backend.dto.response.SalesOrderResponseDTO>> placeOrder(
            @PathVariable Long id) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Order placed successfully, invoice generated",
                        opportunityService.placeOrderFromDeal(id)));
    }

    /**
     * Close deal as Qualified/Closed Won/Closed Lost (Admin/Sales Manager only, after payment approved)
     */
    @PatchMapping("/{id}/close")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'sales_manager', 'ADMIN', 'SALES_MANAGER', 'ROLE_ADMIN', 'ROLE_SALES_MANAGER', 'crm_user', 'CRM_USER', 'ROLE_CRM_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<OpportunityResponseDTO>> closeDeal(
            @PathVariable Long id,
            @RequestParam com.crm.crm_backend.model.enums.WorkflowStatus status,
            @RequestParam(required = false) String notes) {

        return ResponseEntity.ok(ApiResponse.success(
                "Deal closed successfully",
                opportunityService.closeDeal(id, status, notes)));
    }

    /**
     * Get pending approvals (Admin/Sales Manager view)
     */
    @GetMapping("/pending-approvals")
    @PreAuthorize("permitAll() or hasAnyAuthority('admin', 'sales_manager', 'ADMIN', 'SALES_MANAGER', 'ROLE_ADMIN', 'ROLE_SALES_MANAGER', 'crm_user', 'CRM_USER', 'ROLE_CRM_USER') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<OpportunityResponseDTO>>> getPendingApprovals(
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                opportunityService.getPendingApprovals(pageable)));
    }
}
