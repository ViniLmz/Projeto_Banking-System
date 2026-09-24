package com.bank_project.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AccountRequest(

        @NotBlank(message = "Account number cannot be blank")
        String accountNumber,

        @NotNull(message = "Balance cannot be null")
        @PositiveOrZero(message = "Balance cannot be negative")
        BigDecimal balance,


        @NotNull(message = "Account type cannot be null")
        AccountType accountType,

        @NotNull(message = "Status cannot be null")
        AccountStatus status,

        @NotNull(message = "Customer ID cannot be null")
        Long customerId
) {}