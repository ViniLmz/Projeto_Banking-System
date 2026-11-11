
package com.bank_project.ledger;

import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.domain.AccountType;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.transfer.Transfer;
import com.bank_project.user.CurrentUserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private LedgerEntryMapper ledgerEntryMapper;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void shouldRecordDebitAndCreditWhenTransferOccurs() {

        Account source = new Account(
                1L, "100001", new BigDecimal("1000.00"),
                AccountType.CHECKING, AccountStatus.ACTIVE
        );

        Account target = new Account(
                2L, "100002", new BigDecimal("500.00"),
                AccountType.CHECKING, AccountStatus.ACTIVE
        );

        BigDecimal amount = new BigDecimal("200.00");

        Transfer transfer = new Transfer(
                null, source, target, amount,
                LocalDateTime.now(), "COMPLETED", "ledger-test-key"
        );

        ledgerService.recordTransfer(transfer);

        ArgumentCaptor<Iterable<LedgerEntry>> captor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(ledgerEntryRepository).saveAll(captor.capture());

        List<LedgerEntry> entries = new ArrayList<>();
        captor.getValue().forEach(entries::add);

        assertThat(entries).hasSize(2);

        LedgerEntry debit = entries.stream()
                .filter(e -> e.getType() == EntryType.DEBIT)
                .findFirst().orElseThrow();

        LedgerEntry credit = entries.stream()
                .filter(e -> e.getType() == EntryType.CREDIT)
                .findFirst().orElseThrow();

        assertThat(debit.getAccount().getId()).isEqualTo(1L);
        assertThat(credit.getAccount().getId()).isEqualTo(2L);

        assertThat(debit.getAmount()).isEqualByComparingTo(amount);
        assertThat(credit.getAmount()).isEqualByComparingTo(amount);

        assertThat(debit.getTransfer()).isSameAs(transfer);
        assertThat(credit.getTransfer()).isSameAs(transfer);
    }
}
