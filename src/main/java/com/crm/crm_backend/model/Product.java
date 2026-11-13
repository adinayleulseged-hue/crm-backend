package com.crm.crm_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", unique = true, nullable = false, length = 50)
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Product category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ProductCategory category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "sku", unique = true, length = 50)
    private String sku;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @NotNull(message = "Product status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "supplier_contact", length = 100)
    private String supplierContact;

    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    @Column(name = "tags", length = 500)
    private String tags; // Comma-separated tags

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Business methods
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean needsReorder() {
        return reorderLevel != null && stockQuantity != null && stockQuantity <= reorderLevel;
    }

    public void adjustStock(int delta) {
        this.stockQuantity = Math.max(0, this.stockQuantity + delta);
        if (this.stockQuantity == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (productCode == null || productCode.trim().isEmpty()) {
            productCode = generateProductCode();
        }
        if (sku == null || sku.trim().isEmpty()) {
            sku = "SKU-" + System.currentTimeMillis();
        }
    }

    private String generateProductCode() {
        return "PRD-" + java.time.Year.now().getValue() + "-" + 
               String.format("%05d", (int)(Math.random() * 99999));
    }
}
