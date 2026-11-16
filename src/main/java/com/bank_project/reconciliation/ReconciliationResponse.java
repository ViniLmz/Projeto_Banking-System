package com.bank_project.reconciliation;



import java.math.BigDecimal;

public record ReconciliationResponse(
        Long accountId,
        BigDecimal openingBalance,
        BigDecimal totalCredits,
        BigDecimal totalDebits,
        BigDecimal expectedBalance,
        BigDecimal actualBalance,
        BigDecimal difference,
        ReconciliationStatus status
) {
}
