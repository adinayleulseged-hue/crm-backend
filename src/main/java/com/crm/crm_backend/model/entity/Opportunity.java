package com.crm.crm_backend.model.entity;

import com.crm.crm_backend.model.Product;
import com.crm.crm_backend.model.enums.LeadSource;
import com.crm.crm_backend.model.enums.OpportunityStage;
import com.crm.crm_backend.model.enums.OpportunityType;
import com.crm.crm_backend.model.enums.WorkflowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "opportunity_number", unique = true, nullable = false)
    private String opportunityNumber;

    @Column(name = "opportunity_name", nullable = false)
    private String opportunityName;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityStage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityType type;

    @Enumerated(EnumType.STRING)
    private LeadSource source;

    @Column(name = "expected_revenue", precision = 15, scale = 2)
    private BigDecimal expectedRevenue;

    @Column(name = "probability")
    private Integer probability;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(name = "actual_close_date")
    private LocalDate actualCloseDate;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(length = 1000)
    private String nextStep;

    @Column(length = 2000)
    private String notes;

    @Builder.Default
    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "quotation_generated")
    @Builder.Default
    private Boolean quotationGenerated = false;

    // ===== Workflow fields =====
    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_status", length = 50)
    @Builder.Default
    private WorkflowStatus workflowStatus = WorkflowStatus.DRAFT;

    // Approval tracking
    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // Linked entities
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private SalesOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    // Closure tracking
    @Column(name = "closed_by")
    private Long closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "close_notes", length = 1000)
    private String closeNotes;

    // Permissions
    @Column(name = "customer_can_close")
    @Builder.Default
    private Boolean customerCanClose = false;

    // Discount fields
    @Column(name = "discount_requested", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountRequested = BigDecimal.ZERO;

    @Column(name = "discount_approved", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountApproved = BigDecimal.ZERO;

    // Product details
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (stage == null) {
            stage = OpportunityStage.NEW;
        }

        if (active == null) {
            active = true;
        }

        if (workflowStatus == null) {
            workflowStatus = WorkflowStatus.DRAFT;
        }

        if (customerCanClose == null) {
            customerCanClose = false;
        }

        if (discountRequested == null) {
            discountRequested = BigDecimal.ZERO;
        }

        if (discountApproved == null) {
            discountApproved = BigDecimal.ZERO;
        }

        if (quantity == null) {
            quantity = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}