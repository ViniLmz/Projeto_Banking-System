package com.bank_project.exception;

public class CpfAlreadyExistsException extends RuntimeException {

    public CpfAlreadyExistsException() {
        super("CPF already registered");
    }
}