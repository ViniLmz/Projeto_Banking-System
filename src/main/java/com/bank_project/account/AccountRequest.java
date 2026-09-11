package com.bank_project.account;

import java.math.BigDecimal;

public record AccountRequest(
        String accountNumber,
        BigDecimal balance,
        String accountType,
        String status,
        Long customerId
) {}