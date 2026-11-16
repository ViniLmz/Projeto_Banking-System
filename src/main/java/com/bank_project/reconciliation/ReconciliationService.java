package com.bank_project.reconciliation;



import com.bank_project.account.domain.Account;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.ledger.EntryType;
import com.bank_project.ledger.LedgerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ReconciliationService {

    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;

    public ReconciliationService(
            AccountRepository accountRepository,
            LedgerService ledgerService
    ) {
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
    }

    @Transactional(readOnly = true)
    public ReconciliationResponse reconcile(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Account not found")
                );

        if (account.getReconciliationOpeningBalance() == null
                || account.getReconciliationStartedAt() == null) {
            throw new IllegalStateException(
                    "Account reconciliation has not been initialized"
            );
        }

        BigDecimal openingBalance =
                account.getReconciliationOpeningBalance();

        BigDecimal totalCredits =
                ledgerService.sumAmountByAccountAndTypeSince(
                        accountId,
                        EntryType.CREDIT,
                        account.getReconciliationStartedAt()
                );

        BigDecimal totalDebits =
                ledgerService.sumAmountByAccountAndTypeSince(
                        accountId,
                        EntryType.DEBIT,
                        account.getReconciliationStartedAt()
                );

        BigDecimal expectedBalance = openingBalance
                .add(totalCredits)
                .subtract(totalDebits);

        BigDecimal actualBalance = account.getBalance();

        BigDecimal difference = actualBalance.subtract(expectedBalance);

        ReconciliationStatus status =
                difference.compareTo(BigDecimal.ZERO) == 0
                        ? ReconciliationStatus.RECONCILED
                        : ReconciliationStatus.DISCREPANCY;

        return new ReconciliationResponse(
                accountId,
                openingBalance,
                totalCredits,
                totalDebits,
                expectedBalance,
                actualBalance,
                difference,
                status
        );
    }
}
