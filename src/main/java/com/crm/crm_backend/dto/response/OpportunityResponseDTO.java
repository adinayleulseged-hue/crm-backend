package com.crm.crm_backend.dto.response;


import com.crm.crm_backend.model.enums.LeadSource;
import com.crm.crm_backend.model.enums.OpportunityStage;
import com.crm.crm_backend.model.enums.OpportunityType;
import com.crm.crm_backend.model.enums.WorkflowStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OpportunityResponseDTO {

    private Long id;

    private String opportunityNumber;

    private String opportunityName;

    private String description;

    private Long leadId;

    private Long customerId;

    private OpportunityStage stage;

    private OpportunityType type;

    private LeadSource source;

    private BigDecimal expectedRevenue;

    private Integer probability;

    private LocalDate expectedCloseDate;

    private LocalDate actualCloseDate;

    private String assignedTo;

    private String nextStep;

    private String notes;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Workflow fields
    private WorkflowStatus workflowStatus;

    private Long approvedBy;

    private LocalDateTime approvedAt;

    private Long rejectedBy;

    private LocalDateTime rejectedAt;

    private String rejectionReason;

    private Long orderId;

    private Long invoiceId;

    private Long closedBy;

    private LocalDateTime closedAt;

    private String closeNotes;

    private Boolean customerCanClose;

    private BigDecimal discountRequested;

    private BigDecimal discountApproved;

    private Long productId;

    private Integer quantity;

    private BigDecimal unitPrice;
}
