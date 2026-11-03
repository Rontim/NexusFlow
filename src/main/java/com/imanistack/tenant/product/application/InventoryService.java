package com.imanistack.tenant.product.application;

import com.imanistack.shared.infrastructure.exception.InsufficientStockException;
import com.imanistack.shared.infrastructure.exception.ResourceNotFoundException;
import com.imanistack.shared.infrastructure.tenant.TenantContext;
import com.imanistack.tenant.product.infrastructure.InventoryEntity;
import com.imanistack.tenant.product.infrastructure.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public void deductStock(UUID productId, int quantityToDeduct) {
        UUID tenantId = TenantContext.getCurrentTenantId();

        InventoryEntity inventory = inventoryRepository.findByProductIdAndTenantIdWithLock(productId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));

        if (inventory.getQuantity() < quantityToDeduct) {
            throw new InsufficientStockException("Insufficient stock available. Current stock: " + inventory.getQuantity());
        }

        inventory.setQuantity(inventory.getQuantity() - quantityToDeduct);
        inventory.setUpdatedAt(OffsetDateTime.now());

        inventoryRepository.save(inventory);
    }
}
