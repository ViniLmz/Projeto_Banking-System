package com.bank_project.account.http;

import com.bank_project.account.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AccountRequest(

        @NotBlank(message = "Account number cannot be blank")
        String accountNumber,

        @NotNull(message = "Account type cannot be null")
        AccountType accountType,

        @Positive(message = "Customer ID must be positive")
        Long customerId

) {}