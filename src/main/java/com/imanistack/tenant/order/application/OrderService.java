package com.imanistack.tenant.order.application;

import com.imanistack.shared.infrastructure.exception.ResourceNotFoundException;
import com.imanistack.shared.infrastructure.tenant.TenantContext;
import com.imanistack.tenant.auth.domain.UserType;
import com.imanistack.tenant.order.domain.OrderPlacedEvent;
import com.imanistack.tenant.order.domain.OrderStatus;
import com.imanistack.tenant.order.infrastructure.OrderEntity;
import com.imanistack.tenant.order.infrastructure.OrderItemEntity;
import com.imanistack.tenant.order.infrastructure.OrderRepository;
import com.imanistack.tenant.order.presentation.dtos.OrderDtos.*;
import com.imanistack.tenant.product.application.InventoryService;
import com.imanistack.tenant.product.infrastructure.InventoryEntity;
import com.imanistack.tenant.product.infrastructure.InventoryRepository;
import com.imanistack.tenant.product.infrastructure.ProductEntity;
import com.imanistack.tenant.product.infrastructure.ProductRepository;
import com.imanistack.tenant.auth.infrastructure.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        InventoryService inventoryService,
                        ApplicationEventPublisher eventPublisher,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        UUID tenantId = TenantContext.getCurrentTenantId();

        if (!userRepository.existsByIdAndTenantIdAndRole(request.customerId(), tenantId, UserType.CUSTOMER)) {
            throw new ResourceNotFoundException("Customer not found in the current tenant: " + request.customerId());
        }

        ProductEntity product = productRepository.findByIdAndTenantId(request.productId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in the current tenant: " + request.productId()));

        inventoryService.deductStock(request.productId(), request.quantity());

        OrderEntity order = new OrderEntity();
        order.setTenantId(tenantId);
        order.setCustomerId(request.customerId());
        order.setStatus(OrderStatus.RESERVED);
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(request.quantity())));

        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(request.productId());
        item.setQuantity(request.quantity());
        item.setUnitPrice(product.getPrice());
        order.addItem(item);

        OrderEntity savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderPlacedEvent(savedOrder.getId(), tenantId));

        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        UUID tenantId = UUID.fromString(TenantContext.getCurrentTenant());

        OrderEntity order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus currentStatus = order.getStatus();

        // Validate state transitions
        validateStateTransition(currentStatus, newStatus);

        // If cancelling, restore inventory back to stock
        if (newStatus == OrderStatus.CANCELLED && currentStatus != OrderStatus.CANCELLED) {
            restoreInventory(order, tenantId);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(java.time.OffsetDateTime.now());
        OrderEntity updatedOrder = orderRepository.save(order);

        return mapToResponse(updatedOrder);
    }

    private void validateStateTransition(OrderStatus current, OrderStatus target) {
        if (current == OrderStatus.FULFILLED || current == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot modify an order that is already " + current);
        }
        if (current == OrderStatus.RESERVED && target == OrderStatus.FULFILLED) {
            throw new IllegalStateException("Cannot fulfill an order before payment (PAID status required).");
        }
    }

    private void restoreInventory(OrderEntity order, UUID tenantId) {
        for (var item : order.getItems()) {
            InventoryEntity inventory = inventoryRepository.findByProductIdAndTenantIdWithLock(item.getProductId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + item.getProductId()));

            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);
        }
    }

    private OrderResponse mapToResponse(OrderEntity order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getTenantId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreatedAt()
        );
    }

}
