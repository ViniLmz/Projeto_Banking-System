package com.bank_project.exception;

public class IdempotencyKeyConflictException extends RuntimeException {
    public IdempotencyKeyConflictException(String message) {
        super(message);
    }
}
