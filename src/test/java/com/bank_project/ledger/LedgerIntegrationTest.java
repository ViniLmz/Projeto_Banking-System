
package com.bank_project.ledger;

import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.domain.AccountType;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.transfer.Transfer;
import com.bank_project.transfer.TransferRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LedgerIntegrationTest {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private CustumerRepository custumerRepository;

    @Test
    void shouldPersistDebitAndCreditEntries() {

        Custumer customer = custumerRepository.save(
                new Custumer(
                        null,
                        "Ledger Test",
                        "98765432100",
                        "ledger-test@example.com"
                )
        );

        String uniqueNumber = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        Account source = new Account(
                null,
                "9" + uniqueNumber,
                new BigDecimal("1000.00"),
                AccountType.CHECKING,
                AccountStatus.ACTIVE
        );
        source.setCustomer(customer);
        source = accountRepository.save(source);

        Account target = new Account(
                null,
                "8" + uniqueNumber,
                new BigDecimal("500.00"),
                AccountType.CHECKING,
                AccountStatus.ACTIVE
        );
        target.setCustomer(customer);
        target = accountRepository.save(target);

        Transfer transfer = transferRepository.save(
                new Transfer(
                        null,
                        source,
                        target,
                        new BigDecimal("200.00"),
                        LocalDateTime.now(),
                        "COMPLETED",
                        UUID.randomUUID().toString()
                )
        );

        ledgerService.recordTransfer(transfer);

        List<LedgerEntry> sourceEntries =
                ledgerEntryRepository
                        .findAllByAccountIdOrderByCreatedAtDesc(source.getId());

        List<LedgerEntry> targetEntries =
                ledgerEntryRepository
                        .findAllByAccountIdOrderByCreatedAtDesc(target.getId());

        assertThat(sourceEntries).hasSize(1);
        assertThat(targetEntries).hasSize(1);

        assertThat(sourceEntries.get(0).getType())
                .isEqualTo(EntryType.DEBIT);

        assertThat(targetEntries.get(0).getType())
                .isEqualTo(EntryType.CREDIT);

        assertThat(sourceEntries.get(0).getAmount())
                .isEqualByComparingTo("200.00");

        assertThat(targetEntries.get(0).getAmount())
                .isEqualByComparingTo("200.00");

        assertThat(sourceEntries.get(0).getTransfer().getId())
                .isEqualTo(transfer.getId());

        assertThat(targetEntries.get(0).getTransfer().getId())
                .isEqualTo(transfer.getId());
    }

    @Test
    void shouldRejectDuplicateLedgerEntriesForSameTransfer() {

        Custumer customer = custumerRepository.save(
                new Custumer(
                        null,
                        "Ledger Duplicate Test",
                        "98765432101",
                        "ledger-duplicate@example.com"
                )
        );

        String uniqueNumber = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        Account source = new Account(
                null,
                "9" + uniqueNumber,
                new BigDecimal("1000.00"),
                AccountType.CHECKING,
                AccountStatus.ACTIVE
        );
        source.setCustomer(customer);
        source = accountRepository.save(source);

        Account target = new Account(
                null,
                "8" + uniqueNumber,
                new BigDecimal("500.00"),
                AccountType.CHECKING,
                AccountStatus.ACTIVE
        );
        target.setCustomer(customer);
        target = accountRepository.save(target);

        Transfer transfer = transferRepository.save(
                new Transfer(
                        null,
                        source,
                        target,
                        new BigDecimal("200.00"),
                        LocalDateTime.now(),
                        "COMPLETED",
                        UUID.randomUUID().toString()
                )
        );

        ledgerService.recordTransfer(transfer);

        assertThatThrownBy(() ->
                ledgerService.recordTransfer(transfer)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ledger entries already exist");

        List<LedgerEntry> sourceEntries =
                ledgerEntryRepository
                        .findAllByAccountIdOrderByCreatedAtDesc(source.getId());

        List<LedgerEntry> targetEntries =
                ledgerEntryRepository
                        .findAllByAccountIdOrderByCreatedAtDesc(target.getId());

        assertThat(sourceEntries).hasSize(1);
        assertThat(targetEntries).hasSize(1);
    }
}
