
package com.bank_project.account;

import com.bank_project.account.aplication.AccountService;
import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.domain.AccountType;
import com.bank_project.account.http.AccountRequest;
import com.bank_project.account.http.AccountUpdateRequest;
import com.bank_project.account.http.TransactionRequest;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.audit.AuditLogService;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.AccountNumberAlreadyExistsException;
import com.bank_project.exception.BlockedAccountException;
import com.bank_project.exception.InsufficientBalanceException;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.user.AppUser;
import com.bank_project.user.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CustumerRepository custumerRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AppUser appUser;

    @InjectMocks
    private AccountService accountService;


    // DEPOSIT TESTS

    @Test
    void shouldDepositSuccessfully() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Account account = createAccount(AccountStatus.ACTIVE, "1000.00");

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.deposit(1L, request);

        assertEquals(new BigDecimal("1500.00"), result.getBalance());

        verify(accountRepository).save(account);

        verify(auditLogService).log(
                "ACCOUNT_DEPOSIT",
                "ACCOUNT",
                "1",
                "Deposit of 500.00 performed. New balance: 1500.00"
        );
    }

    @Test
    void shouldNotDepositIntoBlockedAccount() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Account account = createAccount(AccountStatus.BLOCKED, "1000.00");

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(
                BlockedAccountException.class,
                () -> accountService.deposit(1L, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void shouldNotDepositWhenAccountDoesNotBelongToCustomer() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.empty());

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.deposit(1L, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void shouldSaveCorrectBalanceAfterDeposit() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Account account = createAccount(AccountStatus.ACTIVE, "1000.00");

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("4000.00"));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        accountService.deposit(1L, request);

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(captor.capture());

        assertEquals(
                new BigDecimal("5000.00"),
                captor.getValue().getBalance()
        );

        verify(auditLogService).log(
                "ACCOUNT_DEPOSIT",
                "ACCOUNT",
                "1",
                "Deposit of 4000.00 performed. New balance: 5000.00"
        );
    }

    @Test
    void shouldNotCompleteDepositWhenAuditFails() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Account account = createAccount(AccountStatus.ACTIVE, "1000.00");

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new RuntimeException("Audit failure"))
                .when(auditLogService)
                .log(any(), any(), any(), any());

        assertThrows(
                RuntimeException.class,
                () -> accountService.deposit(1L, request)
        );

        verify(accountRepository).save(account);

        verify(auditLogService).log(
                "ACCOUNT_DEPOSIT",
                "ACCOUNT",
                "1",
                "Deposit of 500.00 performed. New balance: 1500.00"
        );
    }


    // WITHDRAW TESTS

    @Test
    void shouldWithdrawSuccessfully() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Account account = createAccount(AccountStatus.ACTIVE, "1000.00");

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.withdraw(1L, request);

        assertEquals(new BigDecimal("500.00"), result.getBalance());

        verify(accountRepository).save(account);

        verify(auditLogService).log(
                "ACCOUNT_WITHDRAWAL",
                "ACCOUNT",
                "1",
                "Withdrawal of 500.00 performed. New balance: 500.00"
        );
    }

    @Test
    void shouldNotWithdrawFromBlockedAccount() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Account account = createAccount(AccountStatus.BLOCKED, "1000.00");

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(
                BlockedAccountException.class,
                () -> accountService.withdraw(1L, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void shouldNotWithdrawWithInsufficientBalance() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Account account = createAccount(AccountStatus.ACTIVE, "500.00");

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("1000.00"));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.withdraw(1L, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void shouldSaveCorrectBalanceAfterWithdraw() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Account account = createAccount(AccountStatus.ACTIVE, "5000.00");

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("1000.00"));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        accountService.withdraw(1L, request);

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(captor.capture());

        assertEquals(
                new BigDecimal("4000.00"),
                captor.getValue().getBalance()
        );

        verify(auditLogService).log(
                "ACCOUNT_WITHDRAWAL",
                "ACCOUNT",
                "1",
                "Withdrawal of 1000.00 performed. New balance: 4000.00"
        );
    }

    @Test
    void shouldNotWithdrawWhenAccountDoesNotBelongToCustomer() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.empty());

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.withdraw(1L, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void shouldNotCompleteWithdrawalWhenAuditFails() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Account account = createAccount(AccountStatus.ACTIVE, "1000.00");

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new RuntimeException("Audit failure"))
                .when(auditLogService)
                .log(any(), any(), any(), any());

        assertThrows(
                RuntimeException.class,
                () -> accountService.withdraw(1L, request)
        );

        verify(accountRepository).save(account);

        verify(auditLogService).log(
                "ACCOUNT_WITHDRAWAL",
                "ACCOUNT",
                "1",
                "Withdrawal of 500.00 performed. New balance: 500.00"
        );
    }



    @Test
    void shouldFindAllAccountsAsAdmin() {

        when(currentUserService.isAdmin()).thenReturn(true);

        List<Account> accounts = List.of(
                createAccount(AccountStatus.ACTIVE, "5000.00"),
                createAccount(AccountStatus.ACTIVE, "3000.00")
        );

        when(accountRepository.findAll()).thenReturn(accounts);

        List<Account> result = accountService.findAll();

        assertEquals(2, result.size());
        assertEquals(accounts, result);

        verify(accountRepository).findAll();
        verify(accountRepository, never()).findAllByCustomerId(anyLong());
    }

    @Test
    void shouldFindOnlyAuthenticatedCustomerAccounts() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        List<Account> accounts = List.of(
                createAccount(AccountStatus.ACTIVE, "5000.00")
        );

        when(accountRepository.findAllByCustomerId(1L))
                .thenReturn(accounts);

        List<Account> result = accountService.findAll();

        assertEquals(accounts, result);

        verify(accountRepository).findAllByCustomerId(1L);
        verify(accountRepository, never()).findAll();
    }

    @Test
    void shouldFindAccountByIdAsAdmin() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Account account = createAccount(AccountStatus.ACTIVE, "5000.00");

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        Optional<Account> result = accountService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(account, result.get());

        verify(accountRepository).findById(1L);
    }

    @Test
    void shouldFindAccountByIdForAuthenticatedCustomer() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Account account = createAccount(AccountStatus.ACTIVE, "5000.00");

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        Optional<Account> result = accountService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(account, result.get());

        verify(accountRepository).findByIdAndCustomerId(1L, 1L);
    }

    @Test
    void shouldNotFindAnotherCustomersAccount() {

        authenticateAsCustomer();
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.empty());

        Optional<Account> result = accountService.findById(1L);

        assertTrue(result.isEmpty());
    }


    // SAVE TESTS

    @Test
    void shouldSaveAccountForAuthenticatedCustomer() {

        authenticateAsCustomer();

        Custumer customer = new Custumer();
        customer.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(appUser);
        when(appUser.getCustomer()).thenReturn(customer);

        when(accountRepository.existsByAccountNumber("1234"))
                .thenReturn(false);

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> {
                    Account account = invocation.getArgument(0);
                    account.setId(1L);
                    return account;
                });

        AccountRequest request = new AccountRequest(
                "1234",
                AccountType.SAVINGS,
                null
        );

        Account result = accountService.save(request);

        assertEquals(1L, result.getId());
        assertEquals("1234", result.getAccountNumber());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(customer, result.getCustomer());

        verify(accountRepository).save(any(Account.class));
        verify(custumerRepository, never()).findById(anyLong());

        verify(auditLogService).log(
                "ACCOUNT_CREATED",
                "ACCOUNT",
                "1",
                "Account created: 1234"
        );
    }

    @Test
    void shouldSaveAccountForCustomerAsAdmin() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Custumer customer = new Custumer();
        customer.setId(2L);

        when(accountRepository.existsByAccountNumber("5678"))
                .thenReturn(false);

        when(custumerRepository.findById(2L))
                .thenReturn(Optional.of(customer));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> {
                    Account account = invocation.getArgument(0);
                    account.setId(1L);
                    return account;
                });

        AccountRequest request = new AccountRequest(
                "5678",
                AccountType.CHECKING,
                2L
        );

        Account result = accountService.save(request);

        assertEquals(1L, result.getId());
        assertEquals("5678", result.getAccountNumber());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals(AccountType.CHECKING, result.getAccountType());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(customer, result.getCustomer());

        verify(custumerRepository).findById(2L);
        verify(accountRepository).save(any(Account.class));
        verify(currentUserService, never()).getCurrentUser();

        verify(auditLogService).log(
                "ACCOUNT_CREATED",
                "ACCOUNT",
                "1",
                "Account created: 5678"
        );
    }

    @Test
    void shouldNotSaveAccountAsAdminWithoutCustomerId() {

        when(currentUserService.isAdmin()).thenReturn(true);

        AccountRequest request = new AccountRequest(
                "5678",
                AccountType.CHECKING,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.save(request)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(custumerRepository, never()).findById(anyLong());
        verifyNoInteractions(auditLogService);
    }

    @Test
    void shouldNotSaveAccountWhenAdminCustomerDoesNotExist() {

        when(currentUserService.isAdmin()).thenReturn(true);

        when(accountRepository.existsByAccountNumber("5678"))
                .thenReturn(false);

        when(custumerRepository.findById(2L))
                .thenReturn(Optional.empty());

        AccountRequest request = new AccountRequest(
                "5678",
                AccountType.CHECKING,
                2L
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.save(request)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void shouldNotSaveAccountWithDuplicatedAccountNumber() {

        when(accountRepository.existsByAccountNumber("1234"))
                .thenReturn(true);

        AccountRequest request = new AccountRequest(
                "1234",
                AccountType.SAVINGS,
                null
        );

        assertThrows(
                AccountNumberAlreadyExistsException.class,
                () -> accountService.save(request)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(currentUserService, never()).getCurrentUser();
        verify(custumerRepository, never()).findById(anyLong());
        verifyNoInteractions(auditLogService);
    }



    @Test
    void shouldUpdateAccountType() {

        Account account = createAccount(AccountStatus.ACTIVE, "5000.00");

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        AccountUpdateRequest request =
                new AccountUpdateRequest(AccountType.SAVINGS);

        Account result = accountService.update(1L, request);

        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals(account, result);

        verify(accountRepository).findById(1L);

        verify(auditLogService).log(
                "ACCOUNT_UPDATED",
                "ACCOUNT",
                "1",
                "Account type updated to: SAVINGS"
        );
    }

    @Test
    void shouldNotUpdateAccountWhenNotFound() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.empty());

        AccountUpdateRequest request =
                new AccountUpdateRequest(AccountType.SAVINGS);

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.update(1L, request)
        );

        verifyNoInteractions(auditLogService);
    }



    @Test
    void shouldDeleteAccount() {

        Account account = createAccount(AccountStatus.ACTIVE, "5000.00");

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        accountService.delete(1L);

        verify(accountRepository).findById(1L);
        verify(accountRepository).delete(account);

        verify(auditLogService).log(
                "ACCOUNT_DELETED",
                "ACCOUNT",
                "1",
                "Account deleted: null"
        );
    }

    @Test
    void shouldNotDeleteAccountWhenNotFound() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.delete(1L)
        );

        verify(accountRepository, never()).delete(any(Account.class));
        verifyNoInteractions(auditLogService);
    }



    @Test
    void shouldUpdateAccountStatus() {

        Account account = createAccount(AccountStatus.BLOCKED, "5000.00");

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        Account result = accountService.updateStatus(
                1L,
                AccountStatus.ACTIVE
        );

        assertEquals(AccountStatus.ACTIVE, result.getStatus());

        verify(auditLogService).log(
                "ACCOUNT_STATUS_UPDATED",
                "ACCOUNT",
                "1",
                "Account status updated to: ACTIVE"
        );
    }

    @Test
    void shouldNotUpdateStatusWhenAccountNotFound() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.updateStatus(1L, AccountStatus.ACTIVE)
        );

        verifyNoInteractions(auditLogService);
    }



    @Test
    void shouldNotCreateAccountWhenAuditFails() {

        authenticateAsCustomer();

        Custumer customer = new Custumer();
        customer.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(appUser);
        when(appUser.getCustomer()).thenReturn(customer);

        when(accountRepository.existsByAccountNumber("9999"))
                .thenReturn(false);

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> {
                    Account account = invocation.getArgument(0);
                    account.setId(1L);
                    return account;
                });

        doThrow(new RuntimeException("Audit failure"))
                .when(auditLogService)
                .log(any(), any(), any(), any());

        AccountRequest request = new AccountRequest(
                "9999",
                AccountType.CHECKING,
                null
        );

        assertThrows(
                RuntimeException.class,
                () -> accountService.save(request)
        );
    }

    @Test
    void shouldNotUpdateAccountWhenAuditFails() {

        Account account = createAccount(AccountStatus.ACTIVE, "5000.00");

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        doThrow(new RuntimeException("Audit failure"))
                .when(auditLogService)
                .log(any(), any(), any(), any());

        AccountUpdateRequest request =
                new AccountUpdateRequest(AccountType.SAVINGS);

        assertThrows(
                RuntimeException.class,
                () -> accountService.update(1L, request)
        );
    }

    @Test
    void shouldNotUpdateStatusWhenAuditFails() {

        Account account = createAccount(AccountStatus.BLOCKED, "5000.00");

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        doThrow(new RuntimeException("Audit failure"))
                .when(auditLogService)
                .log(any(), any(), any(), any());

        assertThrows(
                RuntimeException.class,
                () -> accountService.updateStatus(1L, AccountStatus.ACTIVE)
        );
    }

    @Test
    void shouldNotDeleteAccountWhenAuditFails() {

        Account account = createAccount(AccountStatus.ACTIVE, "5000.00");

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        doThrow(new RuntimeException("Audit failure"))
                .when(auditLogService)
                .log(any(), any(), any(), any());

        assertThrows(
                RuntimeException.class,
                () -> accountService.delete(1L)
        );

        verify(accountRepository, never()).delete(any(Account.class));
    }


    // HELPERS

    private void authenticateAsCustomer() {

        when(currentUserService.isAdmin()).thenReturn(false);
    }

    private Account createAccount(AccountStatus status, String balance) {

        Account account = new Account();

        account.setId(1L);
        account.setBalance(new BigDecimal(balance));
        account.setStatus(status);

        return account;
    }
}
