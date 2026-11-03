package com.imanistack.shared.infrastructure.exception;

public class DuplicateResourceException extends NexusFlowException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
