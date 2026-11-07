
package com.bank_project.account;
import com.bank_project.account.Account;
import com.bank_project.account.AccountRepository;
import com.bank_project.account.AccountStatus;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.InvalidLoanStatusException;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.loan.*;
import com.bank_project.user.CurrentUserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustumerRepository custumerRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private LoanService loanService;


    @Test
    void shouldSaveLoanAsAdmin() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Custumer customer = new Custumer();
        customer.setId(1L);

        Account account = new Account();
        account.setId(2L);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        when(custumerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(account));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanRequest request = new LoanRequest(
                1L,
                2L,
                new BigDecimal("1000.00"),
                new BigDecimal("2.5"),
                10
        );

        Loan loan = loanService.save(request);

        assertEquals(customer, loan.getCustomer());
        assertEquals(account, loan.getAccount());
        assertEquals(new BigDecimal("1000.00"), loan.getAmount());
        assertEquals(new BigDecimal("2.5"), loan.getInterestRate());
        assertEquals(10, loan.getInstallments());
        assertEquals("PENDING", loan.getStatus());

        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void shouldSaveLoanForAuthenticatedCustomer() {

        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Custumer customer = new Custumer();
        customer.setId(1L);

        Account account = new Account();
        account.setId(2L);
        account.setCustomer(customer);

        when(custumerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(accountRepository.findByIdAndCustomerId(2L, 1L))
                .thenReturn(Optional.of(account));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mesmo enviando outro customerId, o Service usa o autenticado.
        LoanRequest request = new LoanRequest(
                999L,
                2L,
                new BigDecimal("1000.00"),
                new BigDecimal("2.5"),
                10
        );

        Loan loan = loanService.save(request);

        assertEquals(1L, loan.getCustomer().getId());

        verify(custumerRepository).findById(1L);
        verify(accountRepository).findByIdAndCustomerId(2L, 1L);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {

        when(currentUserService.isAdmin()).thenReturn(true);

        when(custumerRepository.findById(1L))
                .thenReturn(Optional.empty());

        LoanRequest request = new LoanRequest(
                1L, 2L,
                new BigDecimal("1000.00"),
                new BigDecimal("2.5"),
                10
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.save(request)
        );

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void shouldThrowWhenAccountNotFound() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Custumer customer = new Custumer();
        customer.setId(1L);

        when(custumerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.empty());

        LoanRequest request = new LoanRequest(
                1L, 2L,
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
    void shouldNotSaveLoanWhenAccountBelongsToAnotherCustomer() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Custumer customer = new Custumer();
        customer.setId(1L);

        Custumer otherCustomer = new Custumer();
        otherCustomer.setId(2L);

        Account account = new Account();
        account.setId(2L);
        account.setCustomer(otherCustomer);

        when(custumerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(account));

        LoanRequest request = new LoanRequest(
                1L, 2L,
                new BigDecimal("1000.00"),
                new BigDecimal("2.5"),
                10
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.save(request)
        );

        verify(loanRepository, never()).save(any(Loan.class));
    }




    @Test
    void shouldFindAllLoansAsAdmin() {

        when(currentUserService.isAdmin()).thenReturn(true);
        when(loanRepository.findAll()).thenReturn(List.of());

        List<Loan> loans = loanService.findAll();

        assertTrue(loans.isEmpty());

        verify(loanRepository).findAll();
        verify(loanRepository, never()).findByCustomerId(anyLong());
    }

    @Test
    void shouldFindOnlyAuthenticatedCustomerLoans() {

        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        when(loanRepository.findByCustomerId(1L))
                .thenReturn(List.of());

        List<Loan> loans = loanService.findAll();

        assertTrue(loans.isEmpty());

        verify(loanRepository).findByCustomerId(1L);
        verify(loanRepository, never()).findAll();
    }


    @Test
    void shouldFindLoanByIdAsAdmin() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Loan loan = new Loan();
        loan.setId(1L);

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        Optional<Loan> result = loanService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void shouldFindLoanByIdForAuthenticatedCustomer() {

        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        Loan loan = new Loan();
        loan.setId(1L);

        when(loanRepository.findByIdAndCustomerId(1L, 1L))
                .thenReturn(Optional.of(loan));

        Optional<Loan> result = loanService.findById(1L);

        assertTrue(result.isPresent());

        verify(loanRepository).findByIdAndCustomerId(1L, 1L);
    }




    @Test
    void shouldUpdatePendingLoanAsAdmin() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Loan loan = createPendingLoan();

        when(loanRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(loan));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanUpdateRequest request = new LoanUpdateRequest(
                new BigDecimal("2000.00"),
                new BigDecimal("3.0"),
                12
        );

        Loan updatedLoan = loanService.update(1L, request);

        assertEquals(new BigDecimal("2000.00"), updatedLoan.getAmount());
        assertEquals(new BigDecimal("3.0"), updatedLoan.getInterestRate());
        assertEquals(12, updatedLoan.getInstallments());

        verify(loanRepository).findByIdWithLock(1L);
    }

    @Test
    void shouldNotUpdateLoanFromAnotherCustomer() {

        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        when(loanRepository.findByIdAndCustomerIdWithLock(1L, 1L))
                .thenReturn(Optional.empty());

        LoanUpdateRequest request = new LoanUpdateRequest(
                new BigDecimal("2000.00"),
                new BigDecimal("3.0"),
                12
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.update(1L, request)
        );

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void shouldNotUpdateApprovedLoan() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Loan loan = createPendingLoan();
        loan.setStatus("APPROVED");

        when(loanRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(loan));

        LoanUpdateRequest request = new LoanUpdateRequest(
                new BigDecimal("2000.00"),
                new BigDecimal("3.0"),
                12
        );

        assertThrows(
                InvalidLoanStatusException.class,
                () -> loanService.update(1L, request)
        );
    }




    @Test
    void shouldDeletePendingLoanAsAdmin() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Loan loan = createPendingLoan();

        when(loanRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(loan));

        loanService.delete(1L);

        verify(loanRepository).delete(loan);
    }

    @Test
    void shouldNotDeleteLoanFromAnotherCustomer() {

        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentCustomerId()).thenReturn(1L);

        when(loanRepository.findByIdAndCustomerIdWithLock(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.delete(1L)
        );

        verify(loanRepository, never()).delete(any(Loan.class));
    }

    @Test
    void shouldNotDeleteApprovedLoan() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Loan loan = createPendingLoan();
        loan.setStatus("APPROVED");

        when(loanRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(loan));

        assertThrows(
                InvalidLoanStatusException.class,
                () -> loanService.delete(1L)
        );

        verify(loanRepository, never()).delete(any(Loan.class));
    }




    @Test
    void shouldApproveLoanAndIncreaseAccountBalance() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Loan loan = createPendingLoan();

        when(loanRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(loan));

        when(accountRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(loan.getAccount()));

        Loan approvedLoan = loanService.approve(1L);

        assertEquals("APPROVED", approvedLoan.getStatus());

        assertEquals(
                new BigDecimal("6000.00"),
                loan.getAccount().getBalance()
        );

        verify(accountRepository).save(loan.getAccount());
        verify(loanRepository).save(loan);
    }



    @Test
    void shouldNotApproveLoanAsCustomer() {

        when(currentUserService.isAdmin()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> loanService.approve(1L)
        );

        verify(loanRepository, never()).findByIdWithLock(anyLong());
    }

    @Test
    void shouldNotApproveAlreadyApprovedLoan() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Loan loan = createPendingLoan();
        loan.setStatus("APPROVED");

        when(loanRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(loan));

        assertThrows(
                InvalidLoanStatusException.class,
                () -> loanService.approve(1L)
        );

        verify(accountRepository, never()).findByIdWithLock(anyLong());
    }




    @Test
    void shouldRejectPendingLoanAsAdmin() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Loan loan = createPendingLoan();

        when(loanRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(loan));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Loan rejectedLoan = loanService.reject(1L);

        assertEquals("REJECTED", rejectedLoan.getStatus());

        verify(loanRepository).save(loan);
    }


    @Test
    void shouldNotRejectLoanAsCustomer() {

        when(currentUserService.isAdmin()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> loanService.reject(1L)
        );

        verify(loanRepository, never()).findByIdWithLock(anyLong());
    }

    @Test
    void shouldNotRejectAlreadyApprovedLoan() {

        when(currentUserService.isAdmin()).thenReturn(true);

        Loan loan = createPendingLoan();
        loan.setStatus("APPROVED");

        when(loanRepository.findByIdWithLock(1L))
                .thenReturn(Optional.of(loan));

        assertThrows(
                InvalidLoanStatusException.class,
                () -> loanService.reject(1L)
        );
    }


   

    private Loan createPendingLoan() {

        Custumer customer = new Custumer();
        customer.setId(1L);

        Account account = new Account();
        account.setId(1L);
        account.setCustomer(customer);
        account.setBalance(new BigDecimal("5000.00"));

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(customer);
        loan.setAccount(account);
        loan.setAmount(new BigDecimal("1000.00"));
        loan.setInterestRate(new BigDecimal("2.5"));
        loan.setInstallments(10);
        loan.setStatus("PENDING");

        return loan;
    }
}
