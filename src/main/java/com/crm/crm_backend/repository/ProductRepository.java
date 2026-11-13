package com.crm.crm_backend.repository;

import com.crm.crm_backend.model.Product;
import com.crm.crm_backend.model.ProductCategory;
import com.crm.crm_backend.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByProductCodeAndDeletedFalse(String productCode);

    Optional<Product> findBySkuAndDeletedFalse(String sku);

    Optional<Product> findByIdAndDeletedFalse(Long id);

    List<Product> findByCategoryAndDeletedFalse(ProductCategory category);

    List<Product> findByStatusAndDeletedFalse(ProductStatus status);

    Page<Product> findByDeletedFalse(Pageable pageable);

    Page<Product> findByCategoryAndStatusAndDeletedFalse(ProductCategory category, ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.stockQuantity <= p.reorderLevel AND p.reorderLevel IS NOT NULL")
    List<Product> findProductsNeedingReorder();

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.status = 'ACTIVE' AND p.stockQuantity > 0")
    List<Product> findAvailableProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deleted = false AND p.status = 'OUT_OF_STOCK'")
    long countOutOfStock();

    boolean existsByProductCodeAndDeletedFalse(String productCode);

    boolean existsBySkuAndDeletedFalse(String sku);
}
