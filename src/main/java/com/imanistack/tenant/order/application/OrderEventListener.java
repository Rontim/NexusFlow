package com.imanistack.tenant.order.application;

import com.imanistack.tenant.order.domain.OrderPlacedEvent;
import com.imanistack.tenant.order.infrastructure.AuditLogEntity;
import com.imanistack.tenant.order.infrastructure.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventListener {
    private final AuditLogRepository auditLogRepository;

    public OrderEventListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Listens to OrderPlacedEvent asynchronously AFTER the database transaction commits successfully.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {

        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setTenantId(event.tenantId());
        auditLog.setEventType("ORDER_PLACED");
        auditLog.setPayload("Successfully reserved inventory and placed order: " + event.orderId());

        auditLogRepository.save(auditLog);

    }
}
