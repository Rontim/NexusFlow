package com.imanistack.tenant.product.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {
    Optional<InventoryEntity> findByProductIdAndTenantId(UUID productId, UUID tenantId);

    // Pessimistic write lock to safely decrement stock during concurrent orders
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryEntity i WHERE i.productId = :productId AND i.tenantId = :tenantId")
    Optional<InventoryEntity> findByProductIdAndTenantIdWithLock(
            @Param("productId") UUID productId,
            @Param("tenantId") UUID tenantId
    );
}
