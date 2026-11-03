package com.imanistack.shared.infrastructure.exception;

public class InsufficientStockException extends NexusFlowException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
