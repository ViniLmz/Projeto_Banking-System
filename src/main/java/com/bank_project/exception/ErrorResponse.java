package com.bank_project.exception;

public record ErrorResponse(
        int status,
        String message
) {}