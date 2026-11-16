package com.bank_project.ledger;

import com.bank_project.ledger.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {


    @Query("""
    SELECT COALESCE(SUM(l.amount), 0)
    FROM LedgerEntry l
    WHERE l.account.id = :accountId
    AND l.type = :type
    AND l.createdAt >= :startDate
""")
    BigDecimal sumAmountByAccountAndTypeSince(
            @Param("accountId") Long accountId,
            @Param("type") EntryType type,
            @Param("startDate") LocalDateTime startDate
    );

    boolean existsByTransferId(Long transferId);
    List<LedgerEntry> findAllByAccountIdOrderByCreatedAtDesc(Long accountId);
}