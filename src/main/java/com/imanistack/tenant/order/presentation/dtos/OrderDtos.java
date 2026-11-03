package com.imanistack.tenant.order.presentation.dtos;

import com.imanistack.tenant.order.domain.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDtos {
    public record PlaceOrderRequest(
        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Product ID is required")
        UUID productId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
    ) {}

    public record OrderItemResponse(
            UUID id,
            UUID productId,
            int quantity,
            BigDecimal unitPrice
    ) {}

    public record OrderResponse(
            UUID id,
            UUID tenantId,
            UUID customerId,
            OrderStatus status,
            BigDecimal totalAmount,
            List<OrderItemResponse> items,
            OffsetDateTime createdAt
    ) {}
}
