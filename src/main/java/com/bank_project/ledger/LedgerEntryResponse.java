package com.bank_project.ledger;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LedgerEntryResponse(
        Long id,
        Long accountId,
        String accountNumber,
        Long transferId,
        EntryType type,
        BigDecimal amount,
        LocalDateTime createdAt
) {}
