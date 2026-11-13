
package com.bank_project.account;

import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.audit.AuditLogService;
import com.bank_project.exception.*;
import com.bank_project.ledger.LedgerService;
import com.bank_project.transfer.Transfer;
import com.bank_project.transfer.TransferRepository;
import com.bank_project.transfer.TransferRequest;
import com.bank_project.transfer.TransferService;
import com.bank_project.user.CurrentUserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final String IDEMPOTENCY_KEY = "transfer-test-123";

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void shouldRejectNullOrEmptyIdempotencyKey() {

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("500.00")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(null, request)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer("   ", request)
        );

        verifyNoInteractions(
                transferRepository,
                accountRepository,
                currentUserService,
                ledgerService,
                auditLogService
        );
    }

    @Test
    void shouldThrowConflictWhenIdempotencyKeyIsUsedWithDifferentData() {

        Account sourceAccount = createAccount(1L, "5000.00");
        Account targetAccount = createAccount(2L, "1000.00");

        Transfer existingTransfer = new Transfer(
                1L,
                sourceAccount,
                targetAccount,
                new BigDecimal("1000.00"),
                LocalDateTime.now(),
                "COMPLETED",
                IDEMPOTENCY_KEY
        );

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("5000.00")
        );

        when(currentUserService.isAdmin()).thenReturn(true);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sourceAccount));

        when(transferRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(existingTransfer));

        assertThrows(
                IdempotencyKeyConflictException.class,
                () -> transferService.transfer(IDEMPOTENCY_KEY, request)
        );

        verify(accountRepository, never()).findAllByIdsWithLock(any());
        verify(accountRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
        verifyNoInteractions(ledgerService, auditLogService);
    }

    @Test
    void shouldReturnExistingTransferWhenIdempotencyKeyAlreadyExists() {

        Account sourceAccount = createAccount(1L, "5000.00");
        Account targetAccount = createAccount(2L, "1000.00");

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("1000.00")
        );

        Transfer existingTransfer = new Transfer(
                1L,
                sourceAccount,
                targetAccount,
                new BigDecimal("1000.00"),
                LocalDateTime.now(),
                "COMPLETED",
                IDEMPOTENCY_KEY
        );

        when(currentUserService.isAdmin()).thenReturn(true);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sourceAccount));

        when(transferRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(existingTransfer));

        Transfer result = transferService.transfer(
                IDEMPOTENCY_KEY,
                request
        );

        assertEquals(existingTransfer, result);

        verify(accountRepository, never()).findAllByIdsWithLock(any());
        verify(accountRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
        verifyNoInteractions(ledgerService, auditLogService);
    }

    @Test
    void testTransferInsufficientBalance() {

        Account sourceAccount = createAccount(1L, "500.00");
        Account targetAccount = createAccount(2L, "1000.00");

        mockAdminTransfer(sourceAccount, targetAccount);

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("5000.00")
        );

        assertThrows(
                InsufficientBalanceException.class,
                () -> transferService.transfer(IDEMPOTENCY_KEY, request)
        );

        assertEquals(
                new BigDecimal("500.00"),
                sourceAccount.getBalance()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                targetAccount.getBalance()
        );

        verify(transferRepository, never()).save(any());
        verifyNoInteractions(ledgerService, auditLogService);
    }

    @Test
    void testTransferBlockedTargetAccount() {

        Account sourceAccount = createAccount(1L, "5000.00");
        Account targetAccount = createAccount(2L, "1000.00");

        targetAccount.setStatus(AccountStatus.BLOCKED);

        mockAdminTransfer(sourceAccount, targetAccount);

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("500.00")
        );

        assertThrows(
                BlockedAccountException.class,
                () -> transferService.transfer(IDEMPOTENCY_KEY, request)
        );

        verify(transferRepository, never()).save(any());
        verifyNoInteractions(ledgerService, auditLogService);
    }

    @Test
    void testTransferSource2Source() {

        Account sourceAccount = createAccount(1L, "5000.00");

        mockAdminTransfer(sourceAccount, sourceAccount);

        TransferRequest request = new TransferRequest(
                1L,
                1L,
                new BigDecimal("500.00")
        );

        assertThrows(
                SameAccountTransferException.class,
                () -> transferService.transfer(IDEMPOTENCY_KEY, request)
        );

        verify(transferRepository, never()).save(any());
        verifyNoInteractions(ledgerService, auditLogService);
    }

    @Test
    void testTransferSourceAccountNotFound() {

        when(currentUserService.isAdmin()).thenReturn(true);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.empty());

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("500.00")
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> transferService.transfer(IDEMPOTENCY_KEY, request)
        );

        verify(accountRepository, never()).findAllByIdsWithLock(any());
        verify(transferRepository, never()).save(any());
        verifyNoInteractions(ledgerService, auditLogService);
    }

    @Test
    void testTransferTargetAccountNotFound() {

        Account sourceAccount = createAccount(1L, "5000.00");

        when(currentUserService.isAdmin()).thenReturn(true);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findAllByIdsWithLock(List.of(1L, 2L)))
                .thenReturn(List.of(sourceAccount));

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("500.00")
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> transferService.transfer(IDEMPOTENCY_KEY, request)
        );

        verify(transferRepository, never()).save(any());
        verifyNoInteractions(ledgerService, auditLogService);
    }

    @Test
    void testTransferBlockedAccount() {

        Account sourceAccount = createAccount(1L, "5000.00");
        Account targetAccount = createAccount(2L, "1000.00");

        sourceAccount.setStatus(AccountStatus.BLOCKED);

        mockAdminTransfer(sourceAccount, targetAccount);

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("500.00")
        );

        assertThrows(
                BlockedAccountException.class,
                () -> transferService.transfer(IDEMPOTENCY_KEY, request)
        );

        verify(transferRepository, never()).save(any());
        verifyNoInteractions(ledgerService, auditLogService);
    }

    @Test
    void testTransferSaved() {

        Account sourceAccount = createAccount(1L, "5000.00");
        Account targetAccount = createAccount(2L, "1000.00");

        mockAdminTransfer(sourceAccount, targetAccount);
        mockSavedTransfer();

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("500.00")
        );

        transferService.transfer(IDEMPOTENCY_KEY, request);

        verify(transferRepository).save(any(Transfer.class));
        verify(ledgerService).recordTransfer(any(Transfer.class));

        verify(auditLogService).log(
                eq("TRANSFER"),
                eq("TRANSFER"),
                eq("99"),
                contains("Transfer from account")
        );
    }

    @Test
    void testTransferSavedWithCorrectData() {

        Account sourceAccount = createAccount(1L, "5000.00");
        Account targetAccount = createAccount(2L, "1000.00");

        mockAdminTransfer(sourceAccount, targetAccount);
        mockSavedTransfer();

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("500.00")
        );

        transferService.transfer(IDEMPOTENCY_KEY, request);

        ArgumentCaptor<Transfer> captor =
                ArgumentCaptor.forClass(Transfer.class);

        verify(transferRepository).save(captor.capture());

        Transfer savedTransfer = captor.getValue();

        assertEquals(sourceAccount, savedTransfer.getSourceAccount());
        assertEquals(targetAccount, savedTransfer.getTargetAccount());

        assertEquals(
                new BigDecimal("500.00"),
                savedTransfer.getAmount()
        );
    }

    @Test
    void testTransferSavedWithCompletedStatus() {

        Account sourceAccount = createAccount(1L, "5000.00");
        Account targetAccount = createAccount(2L, "1000.00");

        mockAdminTransfer(sourceAccount, targetAccount);
        mockSavedTransfer();

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("500.00")
        );

        transferService.transfer(IDEMPOTENCY_KEY, request);

        ArgumentCaptor<Transfer> captor =
                ArgumentCaptor.forClass(Transfer.class);

        verify(transferRepository).save(captor.capture());

        Transfer savedTransfer = captor.getValue();

        assertEquals("COMPLETED", savedTransfer.getStatus());
    }

    @Test
    void testTransferSavedWithDate() {

        Account sourceAccount = createAccount(1L, "5000.00");
        Account targetAccount = createAccount(2L, "1000.00");

        mockAdminTransfer(sourceAccount, targetAccount);
        mockSavedTransfer();

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("500.00")
        );

        transferService.transfer(IDEMPOTENCY_KEY, request);

        ArgumentCaptor<Transfer> captor =
                ArgumentCaptor.forClass(Transfer.class);

        verify(transferRepository).save(captor.capture());

        Transfer savedTransfer = captor.getValue();

        assertNotNull(savedTransfer.getDate());
    }

    @Test
    void testFindAll() {

        Transfer transfer1 = new Transfer();
        Transfer transfer2 = new Transfer();

        when(currentUserService.isAdmin()).thenReturn(true);

        when(transferRepository.findAll())
                .thenReturn(List.of(transfer1, transfer2));

        List<Transfer> transfers = transferService.findAll();

        assertEquals(2, transfers.size());
        assertEquals(transfer1, transfers.get(0));
        assertEquals(transfer2, transfers.get(1));
    }

    @Test
    void testFindAllEmptyTransfer() {

        when(currentUserService.isAdmin()).thenReturn(true);

        when(transferRepository.findAll())
                .thenReturn(List.of());

        List<Transfer> transfers = transferService.findAll();

        assertThat(transfers).isEmpty();
    }

    @Test
    void testFindAllIsCallEmpty() {

        when(currentUserService.isAdmin()).thenReturn(true);

        when(transferRepository.findAll())
                .thenReturn(List.of());

        transferService.findAll();

        verify(transferRepository, times(1)).findAll();
    }

    private Account createAccount(Long id, String balance) {

        Account account = new Account();

        account.setId(id);
        account.setBalance(new BigDecimal(balance));
        account.setStatus(AccountStatus.ACTIVE);

        return account;
    }

    private void mockAdminTransfer(Account source, Account target) {

        when(currentUserService.isAdmin()).thenReturn(true);

        when(accountRepository.findById(source.getId()))
                .thenReturn(Optional.of(source));

        List<Account> lockedAccounts;

        if (source.getId().equals(target.getId())) {
            lockedAccounts = List.of(source);
        } else {
            lockedAccounts = List.of(source, target);
        }

        when(accountRepository.findAllByIdsWithLock(
                List.of(source.getId(), target.getId())
        )).thenReturn(lockedAccounts);
    }

    private void mockSavedTransfer() {

        when(transferRepository.save(any(Transfer.class)))
                .thenAnswer(invocation -> {

                    Transfer transfer = invocation.getArgument(0);

                    return new Transfer(
                            99L,
                            transfer.getSourceAccount(),
                            transfer.getTargetAccount(),
                            transfer.getAmount(),
                            transfer.getDate(),
                            transfer.getStatus(),
                            IDEMPOTENCY_KEY
                    );
                });
    }
}
