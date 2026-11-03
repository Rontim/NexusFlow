package com.imanistack.tenant.product.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class ProductDtos {
    public record CreateProductRequest(
            @NotBlank(message = "SKU is required") String sku,
            @NotBlank(message = "Product name is required") String name,
            String description,
            @NotNull(message = "Price is required") @Min(value = 0, message = "Price cannot be negative") BigDecimal price,
            @Min(value = 0, message = "Initial stock cannot be negative") int initialStock
    ) {}

    public record ProductResponse(
            UUID id,
            UUID tenantId,
            String sku,
            String name,
            String description,
            BigDecimal price,
            int stockQuantity
    ) {}
}
