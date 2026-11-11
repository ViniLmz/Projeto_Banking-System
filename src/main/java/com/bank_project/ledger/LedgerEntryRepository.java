package com.bank_project.ledger;

import com.bank_project.ledger.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    boolean existsByTransferId(Long transferId);
    List<LedgerEntry> findAllByAccountIdOrderByCreatedAtDesc(Long accountId);
}