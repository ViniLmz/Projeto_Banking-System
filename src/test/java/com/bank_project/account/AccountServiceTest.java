package com.bank_project.account;

import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.AccountNumberAlreadyExistsException;
import com.bank_project.exception.InsufficientBalanceException;
import com.bank_project.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.bank_project.exception.BlockedAccountException;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustumerRepository custumerRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void testDeposit() {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000.00"));
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        accountService.deposit(1l, request);
        assertEquals(
                new BigDecimal("1500.00"),
                account.getBalance()
        );
    }


    @Test
    void testDepositBlockedAccount(){
        Account account = new Account();
        account.setId(1l);
        account.setStatus(AccountStatus.BLOCKED);
        account.setBalance(new BigDecimal("1000.00"));
        when(accountRepository.findById(1l)).thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));
        assertThrows(
                BlockedAccountException.class,
                ()->accountService.deposit(1l,request)
        );
    }

    @Test
    void testWithdraw(){
        Account account = new Account();
        account.setId(1l);
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.ACTIVE);


        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request = new TransactionRequest(new BigDecimal("500.00"));

        accountService.withdraw(1l, request);

        assertEquals(
                new BigDecimal("500.00"),
                account.getBalance());

    }

    @Test
    void testWithdrawBlockedAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.BLOCKED);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(
                BlockedAccountException.class,
                () -> accountService.withdraw(1L, request)
        );
    }

    @Test
    void testDepositAccountNotFound() {
        when(accountRepository.findById(1l))
                .thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(ResourceNotFoundException.class, () -> accountService.deposit(1l, request));
    }

    @Test
    void testWithdrawAccountNotFound() {

        when(accountRepository.findById(1l))
                .thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(ResourceNotFoundException.class,
                ()-> accountService.withdraw(1l, request));

    }
    @Test
    void testWithdrawInsufficientBalance() {

        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("500.00"));
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("1000.00"));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.withdraw(1L, request)
        );
    }

    @Test
    void testWithdrawSavesAccount() {

        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1500.00"));
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("1000.00"));

        accountService.withdraw(1L, request);

        verify(accountRepository).save(account);
    }

    @Test
    void testDepositSavesAccount() {

        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("500.00"));
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("1000.00"));

        accountService.deposit(1l, request);

        verify(accountRepository).save(account);

    }

    @Test
    void testDepositSavesCorrectBalance(){

        Account account = new Account();
        account.setId(1l);
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1l))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("4000.00"));

        accountService.deposit(1l, request);

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(captor.capture());

        Account accountSaved = captor.getValue();

        assertEquals(new BigDecimal("5000.00"), accountSaved.getBalance());

    }

    @Test
    void testWithdrawSavesCorrectBalance(){

        Account account = new Account();
        account.setId(1l);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1l))
                .thenReturn(Optional.of(account));

        TransactionRequest request = new TransactionRequest(new BigDecimal("1000.00"));

        accountService.withdraw(1L,request);

            ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(captor.capture());

        Account accountSaved = captor.getValue();

        assertEquals(new BigDecimal("4000.00"), accountSaved.getBalance());


    }
    @Test
    void testWithdrawBlockedAccountDoesNotSave() {

        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.BLOCKED);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("1000.00"));

        assertThrows(
                BlockedAccountException.class,
                () -> accountService.withdraw(1L, request)
        );

        verify(accountRepository, never()).save(account);
    }

    @Test
    void testWithdrawInsufficientBalanceDoesNotSave() {

        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("500.00"));
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("1000.00"));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.withdraw(1L, request)
        );

        verify(accountRepository, never()).save(account);
    }

    @Test
    void testDepositBlockedAccountDoesNotSave(){

         Account account = new Account();
         account.setId(1l);
         account.setBalance(new BigDecimal("500.00"));
         account.setStatus(AccountStatus.BLOCKED);

         when(accountRepository.findById(1l))
                 .thenReturn(Optional.of(account));

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("500.00"));

        assertThrows(
                BlockedAccountException.class,
                () -> accountService.deposit(1L, request)
        );

        verify(accountRepository, never()).save(account);

    }

    @Test
    void testFindAll(){
        Account account= new Account();
        account.setId(1l);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("5000.00"));

        List<Account> accounts = List.of(account);

        when(accountRepository.findAll())
                .thenReturn(accounts);


        List<Account> result = accountService.findAll();

        assertEquals(accounts, result);

    }

    @Test
    void testFindById(){
        Account account = new Account();
        account.setId(1l);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("5000.00"));

        when(accountRepository.findById(1l)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.findById(1L);


        assertTrue(result.isPresent());
        assertEquals(account, result.get());

        verify(accountRepository).findById(1l);

    }

    @Test
    void testSaveAccount(){
          Custumer custumer = new Custumer();
        custumer.setId(1L);

        when(custumerRepository.findById(1L))
                .thenReturn(Optional.of(custumer));


        AccountRequest request =
                new AccountRequest(
                        "1234",
                        new BigDecimal("5000.00"),
                        AccountType.SAVINGS,
                        AccountStatus.ACTIVE,
                        1l);


        when(accountRepository.existsByAccountNumber("1234"))
                .thenReturn(false);

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.save(request);

        assertEquals("1234", result.getAccountNumber());
        assertEquals(new BigDecimal("5000.00"), result.getBalance());
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(custumer, result.getCustomer());

    }

    @Test
    void  testUpdate(){
        Account account = new Account();
        account.setId(1L);
        account.setAccountType(AccountType.CHECKING);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

           AccountUpdateRequest request = new AccountUpdateRequest(AccountType.SAVINGS);

           Account result = accountService.update(1l, request);


           assertEquals(AccountType.SAVINGS,result.getAccountType());
           assertEquals(account, result);
        verify(accountRepository).findById(1L);


    }

    @Test
    void testDeleteById(){

        when(accountRepository.existsById(1L))
                .thenReturn(true);

        accountService.delete(1L);

        verify(accountRepository).existsById(1L);
        verify(accountRepository).deleteById(1L);

    }

    @Test
    void testClientNotFound() {

        when(accountRepository.existsByAccountNumber("1234"))
                .thenReturn(false);

        when(custumerRepository.findById(1L))
                .thenReturn(Optional.empty());

        AccountRequest request =
                new AccountRequest(
                        "1234",
                        new BigDecimal("5000.00"),
                        AccountType.SAVINGS,
                        AccountStatus.ACTIVE,
                        1L
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.save(request)
        );
    }

    @Test
    void testDuplicatedAccount(){
        when(accountRepository.existsByAccountNumber("1234")).thenReturn(true);

        AccountRequest request =
                new AccountRequest(
                        "1234",
                        new BigDecimal("5000.00"),
                        AccountType.SAVINGS,
                        AccountStatus.ACTIVE,
                        1L
                );

        assertThrows(AccountNumberAlreadyExistsException.class,()-> accountService.save(request));
        verify(accountRepository, never()).save(any(Account.class));

    }

    @Test
    void testUpdateAccountNotFound(){
        when(accountRepository.findById(1L))
                .thenReturn(Optional.empty());

        AccountUpdateRequest request = new AccountUpdateRequest(AccountType.SAVINGS);

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.update(1L, request)
        );
    }

    @Test
    void testDeleteAccountNotFound(){
        when(accountRepository.existsById(1l)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                ()-> accountService.delete(1l));

    }

    @Test
    void testUpdateStatus(){
        Account account = new Account();
        account.setId(1L);
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.BLOCKED);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        AccountStatus accountStatus = AccountStatus.ACTIVE;

        when(accountRepository.save(account))
                .thenReturn(account);

        Account result = accountService.updateStatus(1L, accountStatus);

        assertEquals(AccountStatus.ACTIVE, result.getStatus());
    }

    @Test
    void testUpdateStatusAccountNotFound(){

        when(accountRepository.findById(1L))
                .thenReturn(Optional.empty());

        AccountStatus accountStatus = AccountStatus.ACTIVE;

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.updateStatus(1L, accountStatus)
        );
    }


}