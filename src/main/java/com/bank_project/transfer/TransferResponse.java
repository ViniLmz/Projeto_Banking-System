package com.bank_project.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long id,
        Long sourceAccountId,
        Long targetAccountId,
        BigDecimal amount,
        LocalDateTime date,
        String status
) {}