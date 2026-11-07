package com.bank_project.account.http;

import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.domain.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        AccountType accountType,
        AccountStatus status,
        Long customerId
) {}