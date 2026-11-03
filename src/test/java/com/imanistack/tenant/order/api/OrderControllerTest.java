package com.imanistack.tenant.order.api;

import com.imanistack.tenant.order.application.OrderService;
import com.imanistack.tenant.order.presentation.OrderController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderControllerTest {
    private final OrderService orderService = mock(OrderService.class);
    private final OrderController controller = new OrderController(orderService);

    @Test
    void createsOrderForAuthenticatedUser() {

    }
}
