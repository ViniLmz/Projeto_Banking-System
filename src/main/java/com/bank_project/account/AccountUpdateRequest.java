package com.bank_project.account;

import jakarta.validation.constraints.NotNull;

public record AccountUpdateRequest(

        @NotNull(message = "Account type cannot be null")
        AccountType accountType

) {
}