package com.imanistack.shared.infrastructure.exception;

public class InvalidOrderException extends NexusFlowException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
