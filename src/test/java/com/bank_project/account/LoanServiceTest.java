
package com.bank_project.account;

import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.InvalidLoanStatusException;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.loan.Loan;
import com.bank_project.loan.LoanRequest;
import com.bank_project.loan.LoanService;
import com.bank_project.loan.LoanRepository;

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
class LoanServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LoanRepository loanRepository;


    @Mock
    private CustumerRepository custumerRepository;

    @InjectMocks
    private LoanService loanService;


    @Test
    void testSaveLoan() {

        Custumer custumer = new Custumer();
        custumer.setId(1L);

        Account account = new Account();
        account.setId(2L);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(custumer);

        when(custumerRepository.findById(1L))
                .thenReturn(Optional.of(custumer));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(account));

        LoanRequest request =
                new LoanRequest(
                        1L,
                        2L,
                        new BigDecimal("1000.00"),
                        new BigDecimal("2.5"),
                        10
                );

        Loan loan = loanService.save(request);

        assertEquals(custumer, loan.getCustomer());
        assertEquals(account, loan.getAccount());
        assertEquals(new BigDecimal("1000.00"), loan.getAmount());
        assertEquals(new BigDecimal("2.5"), loan.getInterestRate());
        assertEquals(10, loan.getInstallments());
        assertEquals("PENDING", loan.getStatus());


    }

    @Test
    void testSaveLoanCustomerNotFound() {
        Custumer custumer = new Custumer();

        when(custumerRepository.findById(1l))
                .thenReturn(Optional.empty());


        LoanRequest request =
                new LoanRequest(
                        1L,
                        2L,
                        new BigDecimal("1000.00"),
                        new BigDecimal("2.5"),
                        10
                );

        assertThrows(ResourceNotFoundException.class,
                ()-> loanService.save(request));
    }

    @Test
    void testSaveLoanAccountNotFound() {

        Custumer custumer = new Custumer();
        custumer.setId(1L);

        when(custumerRepository.findById(1L))
                .thenReturn(Optional.of(custumer));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.empty());

        LoanRequest request =
                new LoanRequest(
                        1L,
                        2L,
                        new BigDecimal("1000.00"),
                        new BigDecimal("2.5"),
                        10
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.save(request)
        );
    }

    @Test
    void testAnotherLoanAccount(){

    Custumer custumer = new Custumer();
    custumer.setId(1l);

    Custumer otherCustomer = new Custumer();
    otherCustomer.setId(2L);


    Account account = new Account();
            account.setId(1l);
            account.setCustomer(otherCustomer);


    when(custumerRepository.findById(1l))
            .thenReturn(Optional.of(custumer));

        when(accountRepository.findById(1l))
                .thenReturn(Optional.of(account));

        LoanRequest request =
                new LoanRequest(
                        1L,
                        1L,
                        new BigDecimal("1000.00"),
                        new BigDecimal("2.5"),
                        10
                );


        assertThrows(ResourceNotFoundException.class,
                ()-> loanService.save(request));


    }

    @Test
    void testStatusApproved(){

        Custumer custumer = new Custumer();
        custumer.setId(1l);

        Account account = new Account();
        account.setId(1l);
        account.setBalance(new BigDecimal("5000.00"));
        account.setCustomer(custumer);


        Loan loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(custumer);
        loan.setAccount(account);
        loan.setAmount(new BigDecimal("1000.00"));
        loan.setStatus("PENDING");


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        Loan approvedLoan = loanService.approve(1l);

        assertEquals("APPROVED", approvedLoan.getStatus());
        assertEquals(new BigDecimal("6000.00"), account.getBalance());

    }
    @Test
    void testApproveLoanNotFound() {

        when(loanRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.approve(1L)
        );
    }

    @Test
    void testLoanReadyApproved(){

        Custumer custumer = new Custumer();
        custumer.setId(1L);

        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("5000.00"));
        account.setCustomer(custumer);

        Loan loan = new Loan();

        loan.setId(1l);
        loan.setCustomer(custumer);
        loan.setAccount(account);
        loan.setAmount(new BigDecimal("1000.00"));
        loan.setStatus("APPROVED");

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        assertThrows(InvalidLoanStatusException.class,
                ()-> loanService.approve(1l));

    }

    @Test
    void testApproveLoanAlreadyRejected(){

        Custumer custumer = new Custumer();
                custumer.setId(1l);
        Account account = new Account();
                account.setId(1l);
                account.setCustomer(custumer);
                account.setBalance(new BigDecimal("4000.00"));

        Loan loan = new Loan();
        loan.setId(1l);
        loan.setCustomer(custumer);
        loan.setAccount(account);
        loan.setAmount(new BigDecimal("1000.00"));
        loan.setStatus("REJECTED");

        when(loanRepository.findById(1l))
                .thenReturn(Optional.of(loan));

        assertThrows(InvalidLoanStatusException.class,()-> loanService.approve(1l));




    }

    @Test
    void testRejectLoan() {

        Custumer custumer = new Custumer();
        custumer.setId(1L);

        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("5000.00"));
        account.setCustomer(custumer);

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(custumer);
        loan.setAccount(account);
        loan.setAmount(new BigDecimal("1000.00"));
        loan.setStatus("PENDING");

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        Loan rejectedLoan = loanService.reject(1L);

        assertEquals("REJECTED", rejectedLoan.getStatus());
    }

    @Test
    void testRejectLoanNotFound() {

        when(loanRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.reject(1L)
        );
    }

    @Test
    void testLoanAlreadyAprroved()
    {
        Custumer custumer = new Custumer();
        custumer.setId(1l);

        Account account = new Account();
        account.setId(1l);
        account.setCustomer(custumer);
        account.setBalance(new BigDecimal("5000.00"));

        Loan loan = new Loan();
        loan.setAccount(account);
        loan.setId(1l);
        loan.setCustomer(custumer);
        loan.setAmount(new BigDecimal("1000.00"));
        loan.setStatus("APPROVED");

        when(loanRepository.findById(1l))
                .thenReturn(Optional.of(loan));

        assertThrows(
                InvalidLoanStatusException.class,
                ()-> loanService.reject(1l)
        );



    }



}

