package com.imanistack.shared.infrastructure.exception;

public class InvalidCredentialsException extends NexusFlowException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
