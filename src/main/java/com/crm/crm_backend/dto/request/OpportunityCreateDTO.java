package com.crm.crm_backend.dto.request;

import com.crm.crm_backend.model.enums.LeadSource;
import com.crm.crm_backend.model.enums.OpportunityStage;
import com.crm.crm_backend.model.enums.OpportunityType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OpportunityCreateDTO {

    private String opportunityName;
    private String title;
    private String dealName;

    private String description;
    private String subtitle;
    private String scope;

    private Long leadId;

    private Long customerId;
    private String customerName;
    private String customer;

    private OpportunityType type;
    private OpportunityStage stage;
    private String stageName;

    private LeadSource source;

    private BigDecimal expectedRevenue;
    private BigDecimal amount;
    private BigDecimal estimatedValue;
    private BigDecimal value;

    @Min(0)
    @Max(100)
    private Integer probability;

    private LocalDate expectedCloseDate;

    private String assignedTo;

    private String nextStep;

    private String notes;

    // Deal workflow fields
    private Long productId;

    private Integer quantity;

    private BigDecimal unitPrice;

    @Min(0)
    @Max(100)
    private BigDecimal discountRequested;
}

