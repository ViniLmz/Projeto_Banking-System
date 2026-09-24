package com.bank_project.exception;

public class AccountNumberAlreadyExistsException extends RuntimeException {

    public AccountNumberAlreadyExistsException(String message) {
        super(message);

    }

}
