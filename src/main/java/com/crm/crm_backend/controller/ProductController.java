package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.ProductDTO;
import com.crm.crm_backend.model.Product;
import com.crm.crm_backend.model.ProductCategory;
import com.crm.crm_backend.model.ProductStatus;
import com.crm.crm_backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_OFFICER', 'AUDITOR', 'FINANCE_OFFICER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<Product>>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("GET /api/products - search: {}, category: {}, status: {}, page: {}, size: {}", 
                 search, category, status, page, size);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search, pageable);
        } else {
            products = productService.getProductsByCategoryAndStatus(category, status, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_OFFICER', 'AUDITOR', 'FINANCE_OFFICER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);

        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @GetMapping("/code/{productCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_OFFICER', 'AUDITOR', 'FINANCE_OFFICER')")
    public ResponseEntity<ApiResponse<Product>> getProductByCode(@PathVariable String productCode) {
        log.info("GET /api/products/code/{}", productCode);

        Product product = productService.getProductByCode(productCode);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_OFFICER')")
    public ResponseEntity<ApiResponse<List<Product>>> getAvailableProducts() {
        log.info("GET /api/products/available");

        List<Product> products = productService.getAvailableProducts();
        return ResponseEntity.ok(ApiResponse.success("Available products retrieved successfully", products));
    }

    @GetMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsNeedingReorder() {
        log.info("GET /api/products/reorder");

        List<Product> products = productService.getProductsNeedingReorder();
        return ResponseEntity.ok(ApiResponse.success("Products needing reorder retrieved successfully", products));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'AUDITOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductStats() {
        log.info("GET /api/products/stats");

        Map<String, Object> stats = new HashMap<>();
        stats.put("outOfStockCount", productService.getOutOfStockCount());
        stats.put("reorderNeeded", productService.getProductsNeedingReorder().size());
        stats.put("availableCount", productService.getAvailableProducts().size());

        return ResponseEntity.ok(ApiResponse.success("Product statistics retrieved successfully", stats));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("POST /api/products - Creating product: {}", productDTO.getName());

        Product created = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        log.info("PUT /api/products/{} - Updating product", id);

        Product updated = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_OFFICER')")
    public ResponseEntity<ApiResponse<Product>> adjustStock(
            @PathVariable Long id,
            @RequestParam int delta) {
        log.info("PATCH /api/products/{}/stock - Adjusting stock by: {}", id, delta);

        Product updated = productService.adjustStock(id, delta);
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{}", id);

        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", (Void)null));
    }
}
