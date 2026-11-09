package com.bank_project.auth;

public record LoginResponse(
        String accessToken,
        String tokenType
) {}