package com.imanistack.tenant.product.application;

import com.imanistack.shared.infrastructure.exception.DuplicateResourceException;
import com.imanistack.shared.infrastructure.exception.ResourceNotFoundException;
import com.imanistack.shared.infrastructure.tenant.TenantContext;
import com.imanistack.tenant.product.infrastructure.*;
import com.imanistack.tenant.product.presentation.dto.ProductDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        UUID tenantId = UUID.fromString(TenantContext.getCurrentTenant());

        if (productRepository.findBySkuAndTenantId(request.sku(), tenantId).isPresent()) {
            throw new DuplicateResourceException("Product with SKU '" + request.sku() + "' already exists.");
        }

        ProductEntity product = new ProductEntity();
        product.setTenantId(tenantId);
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        ProductEntity savedProduct = productRepository.save(product);

        InventoryEntity inventory = new InventoryEntity();
        inventory.setTenantId(tenantId);
        inventory.setProductId(savedProduct.getId());
        inventory.setQuantity(request.initialStock());
        inventoryRepository.save(inventory);

        return new ProductResponse(
                savedProduct.getId(),
                savedProduct.getTenantId(),
                savedProduct.getSku(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                request.initialStock()
        );
    }
}
