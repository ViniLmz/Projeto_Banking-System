package com.bank_project.repository;

import com.bank_project.account.Account;
import com.bank_project.account.AccountRepository;
import com.bank_project.account.AccountStatus;
import com.bank_project.account.AccountType;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.loan.Loan;
import com.bank_project.loan.LoanRepository;
import com.bank_project.loan.LoanRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CustumerRepository custumerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldSaveAndFindLoanById() {
        Custumer custumer = new Custumer();
        custumer.setCpf("23445566");
        custumer.setName("Vinicius");
        custumer.setEmail("test@hotmail.com");

        custumer = custumerRepository.save(custumer);

        Account account = new Account();
        account.setBalance(new BigDecimal("4000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountNumber("12345");
        account.setAccountType(AccountType.CHECKING);

        account = accountRepository.save(account);

        LoanRequest request = new LoanRequest(
                custumer.getId(),
                account.getId(),
                new BigDecimal("3000.00"),
                new BigDecimal("2.5"),
                12
        );

        Loan loan = new Loan(
                null,
                custumer,
                account,
                request.amount(),
                request.interestRate(),
                request.installments(),
                "PENDING",
                LocalDateTime.now()
        );

        Loan savedLoan = loanRepository.save(loan);

        Optional<Loan> foundLoan = loanRepository.findById(savedLoan.getId());

        assertTrue(foundLoan.isPresent());
        assertEquals(new BigDecimal("3000.00"), foundLoan.get().getAmount());
        assertEquals("PENDING", foundLoan.get().getStatus());
        assertEquals(custumer.getId(), foundLoan.get().getCustomer().getId());
        assertEquals(account.getId(), foundLoan.get().getAccount().getId());
    }

    @Test
    void shouldFindAllLoans() {
        Custumer custumer = new Custumer();
        custumer.setCpf("23445567");
        custumer.setName("Vinicius");
        custumer.setEmail("loan@test.com");

        custumer = custumerRepository.save(custumer);

        Account account = new Account();
        account.setBalance(new BigDecimal("4000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountNumber("54321");
        account.setAccountType(AccountType.CHECKING);
        account.setCustomer(custumer);

        account = accountRepository.save(account);

        Loan loan = new Loan(
                null,
                custumer,
                account,
                new BigDecimal("3000.00"),
                new BigDecimal("2.5"),
                12,
                "PENDING",
                LocalDateTime.now()
        );

        loanRepository.save(loan);

        List<Loan> loans = loanRepository.findAll();

        assertFalse(loans.isEmpty());
        assertEquals(1, loans.size());
        assertEquals(
                new BigDecimal("3000.00"),
                loans.get(0).getAmount()
        );
    }

}