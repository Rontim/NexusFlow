package com.imanistack.tenant.product.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Optional<ProductEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<ProductEntity> findBySkuAndTenantId(String sku, UUID tenantId);
}
