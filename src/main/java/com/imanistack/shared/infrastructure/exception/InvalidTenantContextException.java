package com.imanistack.shared.infrastructure.exception;

public class InvalidTenantContextException extends RuntimeException {
    public InvalidTenantContextException(String message) {
        super(message);
    }
}
