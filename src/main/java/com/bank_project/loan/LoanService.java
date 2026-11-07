package com.bank_project.loan;

import com.bank_project.account.domain.Account;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.InvalidLoanStatusException;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.user.CurrentUserService;

import org.springframework.security.access.AccessDeniedException;
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
    private final CurrentUserService currentUserService;

    public LoanService(
            LoanRepository loanRepository,
            CustumerRepository custumerRepository,
            AccountRepository accountRepository,
            CurrentUserService currentUserService
    ) {
        this.loanRepository = loanRepository;
        this.custumerRepository = custumerRepository;
        this.accountRepository = accountRepository;
        this.currentUserService = currentUserService;
    }



    public Loan save(LoanRequest request) {

        Long customerId;

        if (currentUserService.isAdmin()) {
            customerId = request.customerId();
        } else {
            customerId = currentUserService.getCurrentCustomerId();
        }

        Custumer customer = custumerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found")
                );

        Account account;

        if (currentUserService.isAdmin()) {
            account = accountRepository.findById(request.accountId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Account not found")
                    );
        } else {
            account = accountRepository.findByIdAndCustomerId(
                    request.accountId(),
                    customerId
            ).orElseThrow(() ->
                    new ResourceNotFoundException("Account not found")
            );
        }

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

        return loanRepository.save(loan);
    }



    public List<Loan> findAll() {

        if (currentUserService.isAdmin()) {
            return loanRepository.findAll();
        }

        Long customerId = currentUserService.getCurrentCustomerId();

        return loanRepository.findByCustomerId(customerId);
    }



    public Optional<Loan> findById(Long id) {

        if (currentUserService.isAdmin()) {
            return loanRepository.findById(id);
        }

        Long customerId = currentUserService.getCurrentCustomerId();

        return loanRepository.findByIdAndCustomerId(id, customerId);
    }



    @Transactional
    public Loan update(Long id, LoanUpdateRequest request) {

        Loan loan = findLoanForCurrentUserWithLock(id);

        if (!loan.getStatus().equals("PENDING")) {
            throw new InvalidLoanStatusException("Loan is not pending");
        }

        loan.setAmount(request.amount());
        loan.setInterestRate(request.interestRate());
        loan.setInstallments(request.installments());

        return loanRepository.save(loan);
    }



    @Transactional
    public void delete(Long id) {

        Loan loan = findLoanForCurrentUserWithLock(id);

        if (!loan.getStatus().equals("PENDING")) {
            throw new InvalidLoanStatusException(
                    "Only pending loans can be deleted"
            );
        }

        loanRepository.delete(loan);
    }



    @Transactional
    public Loan approve(Long id) {

        if (!currentUserService.isAdmin()) {
            throw new AccessDeniedException(
                    "Only administrators can approve loans"
            );
        }

        Loan loan = loanRepository.findByIdWithLock(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan not found")
                );

        if (!loan.getStatus().equals("PENDING")) {
            throw new InvalidLoanStatusException("Loan is not pending");
        }

        Account account = accountRepository.findByIdWithLock(
                loan.getAccount().getId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Account not found")
        );

        BigDecimal newBalance =
                account.getBalance().add(loan.getAmount());

        account.setBalance(newBalance);

        loan.setStatus("APPROVED");

        accountRepository.save(account);
        loanRepository.save(loan);

        return loan;
    }



    @Transactional
    public Loan reject(Long id) {

        if (!currentUserService.isAdmin()) {
            throw new AccessDeniedException(
                    "Only administrators can reject loans"
            );
        }

        Loan loan = loanRepository.findByIdWithLock(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan not found")
                );

        if (!loan.getStatus().equals("PENDING")) {
            throw new InvalidLoanStatusException("Loan is not pending");
        }

        loan.setStatus("REJECTED");

        return loanRepository.save(loan);
    }



    private Loan findLoanForCurrentUserWithLock(Long id) {

        if (currentUserService.isAdmin()) {
            return loanRepository.findByIdWithLock(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Loan not found")
                    );
        }

        Long customerId = currentUserService.getCurrentCustomerId();

        return loanRepository.findByIdAndCustomerIdWithLock(
                id,
                customerId
        ).orElseThrow(() ->
                new ResourceNotFoundException("Loan not found")
        );
    }
}