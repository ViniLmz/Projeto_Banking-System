package com.bank_project.loan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanResponse(
        Long id,
        Long customerId,
        Long accountId,
        BigDecimal amount,
        BigDecimal interestRate,
        Integer installments,
        String status,
        LocalDateTime createdAt
) {}