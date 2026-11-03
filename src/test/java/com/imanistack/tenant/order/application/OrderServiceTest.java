package com.imanistack.tenant.order.application;

import com.imanistack.shared.infrastructure.exception.ResourceNotFoundException;
import com.imanistack.shared.infrastructure.tenant.TenantContext;
import com.imanistack.shared.infrastructure.exception.InvalidOrderException;
import com.imanistack.tenant.order.infrastructure.OrderRepository;
import com.imanistack.tenant.order.presentation.dtos.OrderDtos.*;
import com.imanistack.tenant.product.application.InventoryService;
import com.imanistack.tenant.product.infrastructure.ProductRepository;
import com.imanistack.tenant.auth.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final InventoryService inventoryService = mock(InventoryService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OrderService orderService = new OrderService(
            orderRepository,
            productRepository,
            inventoryService,
            eventPublisher,
            userRepository
    );

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void rejectsNonPositiveQuantityBeforeAccessingRepositories() {
        TenantContext.setCurrentTenant(UUID.randomUUID().toString());

        assertThrows(InvalidOrderException.class,
                () -> orderService.placeOrder(new PlaceOrderRequest(UUID.randomUUID(), UUID.randomUUID(), 0)));

        verifyNoInteractions(orderRepository, productRepository, inventoryService, userRepository, eventPublisher);
    }

    @Test
    void rejectsCustomerOutsideTenantBeforeCheckingProductOrInventory() {
        UUID tenantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId.toString());
        when(userRepository.existsByIdAndTenantIdAndRole(customerId, tenantId, "CUSTOMER")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(new PlaceOrderRequest(customerId, UUID.randomUUID(), 1)));

        verifyNoInteractions(productRepository, inventoryService, orderRepository, eventPublisher);
    }
}
