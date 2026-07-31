package com.crm.crm_backend.dto.request;


import com.crm.crm_backend.model.enums.QuotationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class QuotationUpdateDTO {

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private BigDecimal subtotal;

    private BigDecimal discount;

    private BigDecimal tax;

    private BigDecimal totalAmount;

    private QuotationStatus status;

    private String notes;

    private Boolean active;
}