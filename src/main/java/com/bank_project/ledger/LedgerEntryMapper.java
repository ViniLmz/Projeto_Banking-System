package com.bank_project.ledger;


import org.springframework.stereotype.Component;

@Component
public class LedgerEntryMapper {

    public LedgerEntryResponse toResponse(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getAccount().getId(),
                entry.getAccount().getAccountNumber(),
                entry.getTransfer().getId(),
                entry.getType(),
                entry.getAmount(),
                entry.getCreatedAt()
        );
    }
}
