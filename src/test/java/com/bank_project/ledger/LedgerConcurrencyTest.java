
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class LedgerConcurrencyTest {

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
    void shouldPreventDuplicateEntriesAtDatabaseLevel() {

        TestData data = createTransfer();

        ledgerService.recordTransfer(data.transfer());

        LedgerEntry duplicateDebit = new LedgerEntry(
                data.source(),
                data.transfer(),
                EntryType.DEBIT,
                data.transfer().getAmount()
        );

        assertThatThrownBy(() ->
                ledgerEntryRepository.saveAndFlush(duplicateDebit)
        ).isInstanceOf(DataIntegrityViolationException.class);

        List<LedgerEntry> sourceEntries =
                ledgerEntryRepository
                        .findAllByAccountIdOrderByCreatedAtDesc(
                                data.source().getId()
                        );

        List<LedgerEntry> targetEntries =
                ledgerEntryRepository
                        .findAllByAccountIdOrderByCreatedAtDesc(
                                data.target().getId()
                        );

        assertThat(sourceEntries).hasSize(1);
        assertThat(targetEntries).hasSize(1);
    }

    @Test
    void shouldPreventDuplicateEntriesDuringConcurrentTransfers()
            throws Exception {

        TestData data = createTransfer();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {

            var task = (java.util.concurrent.Callable<Attempt>) () -> {

                ready.countDown();

                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Concurrent test timed out waiting to start"
                    );
                }

                try {
                    Transfer transfer = transferRepository
                            .findById(data.transfer().getId())
                            .orElseThrow();

                    ledgerService.recordTransfer(transfer);

                    return new Attempt(true, null);

                } catch (RuntimeException exception) {

                    return new Attempt(false, exception);
                }
            };

            Future<Attempt> first = executor.submit(task);
            Future<Attempt> second = executor.submit(task);

            assertThat(ready.await(10, TimeUnit.SECONDS))
                    .isTrue();

            start.countDown();

            Attempt firstResult = first.get(30, TimeUnit.SECONDS);
            Attempt secondResult = second.get(30, TimeUnit.SECONDS);

            List<Attempt> results = List.of(
                    firstResult,
                    secondResult
            );

            assertThat(results.stream()
                    .filter(Attempt::success)
                    .count())
                    .isEqualTo(1);

            Attempt failedAttempt = results.stream()
                    .filter(result -> !result.success())
                    .findFirst()
                    .orElseThrow();

            assertThat(failedAttempt.exception())
                    .isInstanceOfAny(
                            IllegalStateException.class,
                            DataIntegrityViolationException.class,
                            org.springframework.dao.CannotAcquireLockException.class
                    );

        } finally {
            executor.shutdownNow();
        }

        List<LedgerEntry> sourceEntries =
                ledgerEntryRepository
                        .findAllByAccountIdOrderByCreatedAtDesc(
                                data.source().getId()
                        );

        List<LedgerEntry> targetEntries =
                ledgerEntryRepository
                        .findAllByAccountIdOrderByCreatedAtDesc(
                                data.target().getId()
                        );

        assertThat(sourceEntries).hasSize(1);
        assertThat(targetEntries).hasSize(1);

        assertThat(sourceEntries.get(0).getType())
                .isEqualTo(EntryType.DEBIT);

        assertThat(targetEntries.get(0).getType())
                .isEqualTo(EntryType.CREDIT);
    }

    private TestData createTransfer() {

        String uniqueNumber = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        String cpf = String.format(
                "%011d",
                ThreadLocalRandom.current().nextLong(
                        1_000_000_000L,
                        9_999_999_999L
                )
        );

        Custumer customer = custumerRepository.saveAndFlush(
                new Custumer(
                        null,
                        "Ledger Concurrency Test",
                        cpf,
                        "ledger-" + uniqueNumber + "@example.com"
                )
        );

        Account source = new Account(
                null,
                "9" + uniqueNumber,
                new BigDecimal("1000.00"),
                AccountType.CHECKING,
                AccountStatus.ACTIVE
        );

        source.setCustomer(customer);
        source = accountRepository.saveAndFlush(source);

        Account target = new Account(
                null,
                "8" + uniqueNumber,
                new BigDecimal("500.00"),
                AccountType.CHECKING,
                AccountStatus.ACTIVE
        );

        target.setCustomer(customer);
        target = accountRepository.saveAndFlush(target);

        Transfer transfer = transferRepository.saveAndFlush(
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

        return new TestData(source, target, transfer);
    }

    private record TestData(
            Account source,
            Account target,
            Transfer transfer
    ) {}

    private record Attempt(
            boolean success,
            RuntimeException exception
    ) {}
}
