package com.bank_project.account;

import com.bank_project.custumer.CustumerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.bank_project.exception.BlockedAccountException;
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





}