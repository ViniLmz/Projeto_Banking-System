package com.bank_project.account.http;

import com.bank_project.account.domain.AccountType;
import jakarta.validation.constraints.NotNull;

public record AccountUpdateRequest(

        @NotNull(message = "Account type cannot be null")
        AccountType accountType

) {
}