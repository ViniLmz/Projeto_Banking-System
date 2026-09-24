package com.bank_project.loan;

import com.bank_project.account.Account;
import com.bank_project.account.AccountRepository;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.InvalidLoanStatusException;
import com.bank_project.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final CustumerRepository custumerRepository;
    private final AccountRepository accountRepository;

    public LoanService(
            LoanRepository loanRepository,
            CustumerRepository custumerRepository, AccountRepository accountRepository
    ) {
        this.loanRepository = loanRepository;
        this.custumerRepository = custumerRepository;
        this.accountRepository = accountRepository;
    }

    public Loan save(LoanRequest request) {

        Custumer customer = custumerRepository.findById(request.customerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found")
                );
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found")
                );
        if (!account.getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException(
                    "Account does not belong to customer"
            );
        }

        Loan loan = new Loan(
                null,
                customer,
                account,
                request.amount(),
                request.interestRate(),
                request.installments(),
                "PENDING",
                LocalDateTime.now()
        );

        loanRepository.save(loan);

        return loan;
    }


    public List<Loan> findAll() {
        return loanRepository.findAll();
    }

    public Optional<Loan> findById(Long id) {

        return loanRepository.findById(id);
    }

    public Loan update(Long id, LoanUpdateRequest request) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan not found")
                );

        if (!loan.getStatus().equals("PENDING")) {
            throw new InvalidLoanStatusException("Loan is not pending");
        }


        loan.setAmount(request.amount());
        loan.setInterestRate(request.interestRate());
        loan.setInstallments(request.installments());

        loanRepository.save(loan);

        return loan;
    }

    @Transactional
    public void delete(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan not found")
                );

        loanRepository.delete(loan);
    }

    @Transactional
    public Loan approve(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan not found")
                );

        if (!loan.getStatus().equals("PENDING")) {
            throw new InvalidLoanStatusException("Loan is not pending");
        }

        Account account = loan.getAccount();

        BigDecimal newBalance =
                account.getBalance().add(loan.getAmount());

        account.setBalance(newBalance);

        accountRepository.save(account);

        loan.setStatus("APPROVED");

        loanRepository.save(loan);

        return loan;
    }

    public Loan reject(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan not found")
                );

        if (!loan.getStatus().equals("PENDING")) {
            throw new InvalidLoanStatusException("Loan is not pending");
        }

        loan.setStatus("REJECTED");

        loanRepository.save(loan);

        return loan;
    }
}