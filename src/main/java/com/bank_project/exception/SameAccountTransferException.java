package com.bank_project.exception;

public class SameAccountTransferException extends   RuntimeException {

    public SameAccountTransferException (String message)
    {
        super (message);
    }
}