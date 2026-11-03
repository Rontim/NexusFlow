package com.imanistack.tenant.order;

import com.imanistack.AbstractIntegrationTest;
import com.imanistack.shared.infrastructure.tenant.TenantContext;
import com.imanistack.tenant.auth.domain.UserType;
import com.imanistack.tenant.auth.infrastructure.TenantEntity;
import com.imanistack.tenant.auth.infrastructure.TenantRepository;
import com.imanistack.tenant.auth.infrastructure.UserEntity;
import com.imanistack.tenant.auth.infrastructure.UserRepository;
import com.imanistack.tenant.order.application.OrderService;
import com.imanistack.tenant.order.presentation.dtos.OrderDtos;
import com.imanistack.tenant.product.infrastructure.InventoryEntity;
import com.imanistack.tenant.product.infrastructure.InventoryRepository;
import com.imanistack.tenant.product.infrastructure.ProductEntity;
import com.imanistack.tenant.product.infrastructure.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderConcurrencyTest extends AbstractIntegrationTest {
    @Autowired private OrderService orderService;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private InventoryRepository inventoryRepository;

    private UUID tenantId;
    private UUID customerId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        TenantEntity tenant = new TenantEntity();
        tenant.setName("Concurrency Inc");
        tenant.setSubdomain("concurrency");
        tenant = tenantRepository.save(tenant);
        tenantId = tenant.getId();

        UserEntity user = new UserEntity();
        user.setTenantId(tenantId);
        user.setEmail("buyer@concurrency.com");
        user.setPasswordHash("hash");
        user.setRole(UserType.CUSTOMER);
        user = userRepository.save(user);
        customerId = user.getId();

        ProductEntity product = new ProductEntity();
        product.setTenantId(tenantId);
        product.setSku("FLASH-001");
        product.setName("Limited Edition Watch");
        product.setPrice(new BigDecimal("299.99"));
        product = productRepository.save(product);
        productId = product.getId();

        InventoryEntity inventory = new InventoryEntity();
        inventory.setTenantId(tenantId);
        inventory.setProductId(productId);
        inventory.setQuantity(3);
        inventoryRepository.save(inventory);
    }

    @Test
    @DisplayName("High Concurrency Test: Only 3 out of 10 concurrent requests should succeed when stock is 3")
    void testConcurrentOrderPlacement() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger failedOrders = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    latch.await(); // Wait for all threads to align
                    TenantContext.setCurrentTenant(tenantId.toString());

                    orderService.placeOrder(new OrderDtos.PlaceOrderRequest(customerId, productId, 1));
                    successfulOrders.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("[TEST ERROR] Thread failed with: " + e.getClass().getName() + " - " + e.getMessage());
                    e.printStackTrace();
                    failedOrders.incrementAndGet();
                } finally {
                    TenantContext.clear();
                }
            });
        }

        // Release all threads simultaneously
        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);

        // Assertions
        assertEquals(3, successfulOrders.get(), "Exactly 3 orders should have succeeded");
        assertEquals(7, failedOrders.get(), "Exactly 7 orders should have failed due to stock out");

        TenantContext.setCurrentTenant(tenantId.toString());
        InventoryEntity finalInventory = inventoryRepository.findByProductIdAndTenantId(productId, tenantId).orElseThrow();
        assertEquals(0, finalInventory.getQuantity(), "Remaining stock should be exactly 0");
    }
}
