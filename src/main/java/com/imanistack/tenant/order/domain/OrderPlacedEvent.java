package com.imanistack.tenant.order.domain;

import java.util.UUID;

public record OrderPlacedEvent(UUID orderId, UUID tenantId) {}
