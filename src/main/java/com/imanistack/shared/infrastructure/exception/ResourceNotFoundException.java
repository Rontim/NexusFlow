package com.imanistack.shared.infrastructure.exception;

public class ResourceNotFoundException extends NexusFlowException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
