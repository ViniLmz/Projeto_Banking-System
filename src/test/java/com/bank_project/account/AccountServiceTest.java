package com.bank_project.account;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustumerRepository custumerRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AppUser appUser;

    @InjectMocks
    private AccountService accountService;



    @Test
    void shouldDepositSuccessfully() {

        authenticateAsCustomer();

        Account account = createAccount(
                AccountStatus.ACTIVE,
                "1000.00"
        );

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.deposit(1L, request);

        assertEquals(new BigDecimal("1500.00"), result.getBalance());

        verify(accountRepository).save(account);
    }

    @Test
    void shouldNotDepositIntoBlockedAccount() {

        authenticateAsCustomer();

        Account account = createAccount(
                AccountStatus.BLOCKED,
                "1000.00"
        );

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(
                BlockedAccountException.class,
                () -> accountService.deposit(1L, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldNotDepositWhenAccountDoesNotBelongToCustomer() {

        authenticateAsCustomer();

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.empty());

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.deposit(1L, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldSaveCorrectBalanceAfterDeposit() {

        authenticateAsCustomer();

        Account account = createAccount(
                AccountStatus.ACTIVE,
                "1000.00"
        );

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("4000.00"));

        accountService.deposit(1L, request);

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(captor.capture());

        assertEquals(
                new BigDecimal("5000.00"),
                captor.getValue().getBalance()
        );
    }



    @Test
    void shouldWithdrawSuccessfully() {

        authenticateAsCustomer();

        Account account = createAccount(
                AccountStatus.ACTIVE,
                "1000.00"
        );

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.withdraw(1L, request);

        assertEquals(new BigDecimal("500.00"), result.getBalance());

        verify(accountRepository).save(account);
    }

    @Test
    void shouldNotWithdrawFromBlockedAccount() {

        authenticateAsCustomer();

        Account account = createAccount(
                AccountStatus.BLOCKED,
                "1000.00"
        );

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(
                BlockedAccountException.class,
                () -> accountService.withdraw(1L, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldNotWithdrawWithInsufficientBalance() {

        authenticateAsCustomer();

        Account account = createAccount(
                AccountStatus.ACTIVE,
                "500.00"
        );

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("1000.00"));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.withdraw(1L, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldSaveCorrectBalanceAfterWithdraw() {

        authenticateAsCustomer();

        Account account = createAccount(
                AccountStatus.ACTIVE,
                "5000.00"
        );

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("1000.00"));

        accountService.withdraw(1L, request);

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(captor.capture());

        assertEquals(
                new BigDecimal("4000.00"),
                captor.getValue().getBalance()
        );
    }

    @Test
    void shouldNotWithdrawWhenAccountDoesNotBelongToCustomer() {

        authenticateAsCustomer();

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.empty());

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.withdraw(1L, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
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

        Account account = createAccount(
                AccountStatus.ACTIVE,
                "5000.00"
        );

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

        Account account = createAccount(
                AccountStatus.ACTIVE,
                "5000.00"
        );

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

        when(accountRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.empty());

        Optional<Account> result = accountService.findById(1L);

        assertTrue(result.isEmpty());
    }



    @Test
    void shouldSaveAccountForAuthenticatedCustomer() {

        Custumer customer = new Custumer();
        customer.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(appUser);
        when(appUser.getCustomer()).thenReturn(customer);

        when(accountRepository.existsByAccountNumber("1234"))
                .thenReturn(false);

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountRequest request =
                new AccountRequest(
                        "1234",
                        new BigDecimal("5000.00"),
                        AccountType.SAVINGS,
                        AccountStatus.ACTIVE,
                        999L
                );

        Account result = accountService.save(request);

        assertEquals("1234", result.getAccountNumber());
        assertEquals(new BigDecimal("5000.00"), result.getBalance());
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());


        assertEquals(customer, result.getCustomer());

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldNotSaveAccountWithDuplicatedAccountNumber() {

        when(accountRepository.existsByAccountNumber("1234"))
                .thenReturn(true);

        AccountRequest request =
                new AccountRequest(
                        "1234",
                        new BigDecimal("5000.00"),
                        AccountType.SAVINGS,
                        AccountStatus.ACTIVE,
                        1L
                );

        assertThrows(
                AccountNumberAlreadyExistsException.class,
                () -> accountService.save(request)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(currentUserService, never()).getCurrentUser();
    }



    @Test
    void shouldUpdateAccountType() {

        Account account = createAccount(
                AccountStatus.ACTIVE,
                "5000.00"
        );

        account.setAccountType(AccountType.CHECKING);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        AccountUpdateRequest request =
                new AccountUpdateRequest(AccountType.SAVINGS);

        Account result = accountService.update(1L, request);

        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals(account, result);

        verify(accountRepository).findById(1L);
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
    }




    @Test
    void shouldDeleteAccount() {

        Account account = createAccount(
                AccountStatus.ACTIVE,
                "5000.00"
        );

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        accountService.delete(1L);

        verify(accountRepository).findById(1L);
        verify(accountRepository).delete(account);
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
    }



    @Test
    void shouldUpdateAccountStatus() {

        Account account = createAccount(
                AccountStatus.BLOCKED,
                "5000.00"
        );

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        Account result = accountService.updateStatus(
                1L,
                AccountStatus.ACTIVE
        );

        assertEquals(AccountStatus.ACTIVE, result.getStatus());
    }

    @Test
    void shouldNotUpdateStatusWhenAccountNotFound() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.updateStatus(1L, AccountStatus.ACTIVE)
        );
    }



    private void authenticateAsCustomer() {

        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);
    }

    private Account createAccount(AccountStatus status, String balance) {

        Account account = new Account();

        account.setId(1L);
        account.setBalance(new BigDecimal(balance));
        account.setStatus(status);

        return account;
    }
}