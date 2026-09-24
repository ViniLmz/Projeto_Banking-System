package com.bank_project.loan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LoanRequest(

        @NotNull(message = "Customer can't be null")
        Long customerId,

        @NotNull(message= "Account can't be null")
        Long accountId,

        @NotNull(message = "Amount can't be null")
        @Positive(message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Interest rate can't be null")
        @Positive(message = "Interest rate must be greater than 0")
            BigDecimal interestRate,

        @NotNull(message = "Installments can't be null")
        @Positive(message = "Installments must be greater than 0")
        Integer installments


) {}

