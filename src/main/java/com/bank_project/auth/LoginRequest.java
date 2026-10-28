package com.bank_project.auth;

public record LoginRequest(
        String username,
        String password
) {
}