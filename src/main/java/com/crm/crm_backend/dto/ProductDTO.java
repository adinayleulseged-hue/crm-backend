package com.crm.crm_backend.dto;

import com.crm.crm_backend.model.ProductCategory;
import com.crm.crm_backend.model.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @NotNull(message = "Category is required")
    private ProductCategory category;

    private String description;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    private String sku;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotNull(message = "Status is required")
    private ProductStatus status;

    private Integer reorderLevel;

    private String supplierName;

    private String supplierContact;

    private Integer warrantyMonths;

    private String tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long version;
}
