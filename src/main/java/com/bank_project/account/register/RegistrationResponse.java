
package com.bank_project.account.register;

public record RegistrationResponse(
        String message,
        String username,
        String accountNumber
) {}
