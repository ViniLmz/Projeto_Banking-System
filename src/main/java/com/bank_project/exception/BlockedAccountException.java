package com.bank_project.exception;

public class BlockedAccountException extends RuntimeException {

    public BlockedAccountException(String message) {
        super(message);
    }
}