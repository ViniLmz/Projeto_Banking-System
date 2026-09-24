package com.bank_project.exception;

public class InvalidLoanStatusException extends RuntimeException {

    public InvalidLoanStatusException(String message) {
        super(message);
    }
}