package com.crm.crm_backend.service;

import com.crm.crm_backend.dto.ProductDTO;
import com.crm.crm_backend.exception.ResourceNotFoundException;
import com.crm.crm_backend.model.Product;
import com.crm.crm_backend.model.ProductCategory;
import com.crm.crm_backend.model.ProductStatus;
import com.crm.crm_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByDeletedFalse(pageable);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll().stream()
                .filter(p -> !p.getDeleted())
                .toList();
    }

    public Page<Product> searchProducts(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return productRepository.findByDeletedFalse(pageable);
        }
        return productRepository.searchProducts(search, pageable);
    }

    public Page<Product> getProductsByCategoryAndStatus(ProductCategory category, ProductStatus status, Pageable pageable) {
        if (category != null && status != null) {
            return productRepository.findByCategoryAndStatusAndDeletedFalse(category, status, pageable);
        }
        return productRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Product getProductByCode(String productCode) {
        return productRepository.findByProductCodeAndDeletedFalse(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with code: " + productCode));
    }

    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        return productRepository.findBySkuAndDeletedFalse(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
    }

    public Product createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());

        // Check for duplicate product code
        if (productDTO.getProductCode() != null && productRepository.existsByProductCodeAndDeletedFalse(productDTO.getProductCode())) {
            throw new IllegalArgumentException("Product code already exists: " + productDTO.getProductCode());
        }

        // Check for duplicate SKU
        if (productDTO.getSku() != null && productRepository.existsBySkuAndDeletedFalse(productDTO.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + productDTO.getSku());
        }

        Product product = new Product();
        product.setProductCode(productDTO.getProductCode());
        product.setName(productDTO.getName());
        product.setCategory(productDTO.getCategory());
        product.setDescription(productDTO.getDescription());
        product.setUnitPrice(productDTO.getUnitPrice());
        product.setSku(productDTO.getSku());
        product.setStockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : 0);
        product.setStatus(productDTO.getStatus() != null ? productDTO.getStatus() : ProductStatus.ACTIVE);
        product.setReorderLevel(productDTO.getReorderLevel());
        product.setSupplierName(productDTO.getSupplierName());
        product.setSupplierContact(productDTO.getSupplierContact());
        product.setWarrantyMonths(productDTO.getWarrantyMonths());
        product.setTags(productDTO.getTags());

        Product saved = productRepository.save(product);
        log.info("Product created successfully with ID: {}", saved.getId());
        return saved;
    }

    public Product updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);

        Product product = getProductById(id);

        if (productDTO.getName() != null) {
            product.setName(productDTO.getName());
        }
        if (productDTO.getCategory() != null) {
            product.setCategory(productDTO.getCategory());
        }
        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }
        if (productDTO.getUnitPrice() != null) {
            product.setUnitPrice(productDTO.getUnitPrice());
        }
        if (productDTO.getStockQuantity() != null) {
            product.setStockQuantity(productDTO.getStockQuantity());
        }
        if (productDTO.getStatus() != null) {
            product.setStatus(productDTO.getStatus());
        }
        if (productDTO.getReorderLevel() != null) {
            product.setReorderLevel(productDTO.getReorderLevel());
        }
        if (productDTO.getSupplierName() != null) {
            product.setSupplierName(productDTO.getSupplierName());
        }
        if (productDTO.getSupplierContact() != null) {
            product.setSupplierContact(productDTO.getSupplierContact());
        }
        if (productDTO.getWarrantyMonths() != null) {
            product.setWarrantyMonths(productDTO.getWarrantyMonths());
        }
        if (productDTO.getTags() != null) {
            product.setTags(productDTO.getTags());
        }

        Product updated = productRepository.save(product);
        log.info("Product updated successfully: {}", id);
        return updated;
    }

    public Product adjustStock(Long id, int delta) {
        log.info("Adjusting stock for product ID: {} by {}", id, delta);

        Product product = getProductById(id);
        product.adjustStock(delta);

        Product updated = productRepository.save(product);
        log.info("Stock adjusted. New quantity: {}", updated.getStockQuantity());
        return updated;
    }

    public void deleteProduct(Long id) {
        log.info("Soft deleting product with ID: {}", id);

        Product product = getProductById(id);
        product.setDeleted(true);
        product.setDeletedAt(java.time.LocalDateTime.now());
        product.setStatus(ProductStatus.DISCONTINUED);
        
        productRepository.save(product);

        log.info("Product soft deleted successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsNeedingReorder() {
        return productRepository.findProductsNeedingReorder();
    }

    @Transactional(readOnly = true)
    public List<Product> getAvailableProducts() {
        return productRepository.findAvailableProducts();
    }

    @Transactional(readOnly = true)
    public long getOutOfStockCount() {
        return productRepository.countOutOfStock();
    }
}
