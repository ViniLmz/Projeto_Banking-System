package com.bank_project.account;

import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.BlockedAccountException;
import com.bank_project.exception.InsufficientBalanceException;
import com.bank_project.exception.SameAccountTransferException;
import com.bank_project.transfer.TransferRepository;
import com.bank_project.transfer.TransferRequest;
import com.bank_project.transfer.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TransferServiceTest  {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustumerRepository custumerRepository;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void testTransfer (){
        Account sourceAccount = new Account();
        sourceAccount.setId(1l);
        sourceAccount.setBalance(new BigDecimal("5000.00"));
        sourceAccount.setStatus(AccountStatus.ACTIVE);


        Account targetAccount = new Account();
        targetAccount.setId(2l);
        targetAccount.setBalance(new BigDecimal("1000.00"));
        targetAccount.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1l))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findById(2l))
                .thenReturn(Optional.of(targetAccount));


        TransferRequest request = new TransferRequest(1l,2l,new BigDecimal("1000.00"));

        transferService.transfer(request);

        assertEquals(
                new BigDecimal("4000.00"),
                sourceAccount.getBalance()
        );

        assertEquals(
                new BigDecimal("2000.00"),
                targetAccount.getBalance()
        );
    }
    @Test
    void testTransferInsufficientBalance() {

        Account sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setBalance(new BigDecimal("500.00"));
        sourceAccount.setStatus(AccountStatus.ACTIVE);

        Account targetAccount = new Account();
        targetAccount.setId(2L);
        targetAccount.setBalance(new BigDecimal("1000.00"));
        targetAccount.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(targetAccount));

        TransferRequest request =
                new TransferRequest(
                        1L,
                        2L,
                        new BigDecimal("5000.00")
                );

        assertThrows(
                InsufficientBalanceException.class,
                () -> transferService.transfer(request)
        );
    }

    @Test
    void  testTransferBlockedTargetAccount(){

        Account sourceAccount = new Account();
        sourceAccount.setId(1l);
        sourceAccount.setBalance(new BigDecimal("5000.00"));
        sourceAccount.setStatus(AccountStatus.ACTIVE);

        Account targetAccount = new Account();
        targetAccount.setId(2l);
        targetAccount.setBalance(new BigDecimal("1000.00"));
        targetAccount.setStatus(AccountStatus.BLOCKED);

        when(accountRepository.findById(1l))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findById(2l))
                .thenReturn(Optional.of(targetAccount));

        TransferRequest request = new TransferRequest (1l,2l,new BigDecimal("500"));

        assertThrows(
                BlockedAccountException.class,
                ()-> transferService.transfer(request)
        );
    }


    @Test
    void  testTransferSource2Source(){

        Account sourceAccount = new Account();
        sourceAccount.setId(1l);
        sourceAccount.setBalance(new BigDecimal("5000.00"));
        sourceAccount.setStatus(AccountStatus.ACTIVE);
        
        when(accountRepository.findById(1l))
                .thenReturn(Optional.of(sourceAccount));

        TransferRequest request = new TransferRequest (1l,1l,new BigDecimal("500"));

        assertThrows(
                SameAccountTransferException.class,
                ()-> transferService.transfer(request)
        );
    }


}