package com.imanistack.shared.infrastructure.exception;

public abstract class NexusFlowException extends RuntimeException{
    protected NexusFlowException(String message) {
        super(message);
    }
}
