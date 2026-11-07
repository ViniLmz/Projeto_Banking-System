package com.bank_project.account.aplication;

import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.http.AccountRequest;
import com.bank_project.account.http.AccountUpdateRequest;
import com.bank_project.account.http.TransactionRequest;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.AccountNumberAlreadyExistsException;
import com.bank_project.exception.BlockedAccountException;
import com.bank_project.exception.InsufficientBalanceException;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.user.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public  class AccountService {

    private final AccountRepository accountRepository;
    private final CustumerRepository custumerRepository;
    private final CurrentUserService currentUserService;


    public AccountService(AccountRepository accountRepository, CustumerRepository custumerRepository, CurrentUserService currentUserService) {
        this.accountRepository = accountRepository;
        this.custumerRepository = custumerRepository;
        this.currentUserService = currentUserService;
    }

    private Account findAccountForCurrentUser(Long id) {

        if (currentUserService.isAdmin()) {
            return accountRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account not found with id: " + id));
        }

        Long customerId = currentUserService.getCurrentCustomerId();

        return accountRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));
    }

    public List<Account> findAll() {

        if (currentUserService.isAdmin()) {
            return accountRepository.findAll();
        }

        Long customerId = currentUserService.getCurrentCustomerId();

        return accountRepository.findAllByCustomerId(customerId);
    }

    public Optional<Account> findById(Long id) {

        if (currentUserService.isAdmin()) {
            return accountRepository.findById(id);
        }

        Long customerId = currentUserService.getCurrentCustomerId();

        return accountRepository.findByIdAndCustomerId(id, customerId);
    }


    public Account save(AccountRequest request) {

        if (accountRepository.existsByAccountNumber(request.accountNumber())) {
            throw new AccountNumberAlreadyExistsException("Account number already exists");
        }

        Custumer customer;

        if (currentUserService.isAdmin()) {

            if (request.customerId() == null) {
                throw new IllegalArgumentException("Customer ID is required for administrators");
            }

            customer = custumerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        } else {
            customer = currentUserService.getCurrentUser().getCustomer();

            if (customer == null) {
                throw new ResourceNotFoundException("Customer not found for authenticated user");
            }
        }

        Account account = new Account(
                null,
                request.accountNumber(),
                BigDecimal.ZERO,
                request.accountType(),
                AccountStatus.ACTIVE
        );

        account.setCustomer(customer);

        return accountRepository.save(account);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));

        accountRepository.delete(account);
    }


    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Account update(Long id, AccountUpdateRequest request) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));

        account.setAccountType(request.accountType());

        return account;
    }


    public Account deposit(Long id, TransactionRequest request) {

        Account account = findAccountForCurrentUser(id);

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("This account is currently blocked");
        }

        account.setBalance(
                account.getBalance().add(request.amount())
        );

        return accountRepository.save(account);
    }


    public Account withdraw(Long id, TransactionRequest request) {

        Account account = findAccountForCurrentUser(id);

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("This account is currently blocked");
        }

        if (request.amount().compareTo(account.getBalance()) > 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(
                account.getBalance().subtract(request.amount())
        );

        return accountRepository.save(account);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Account updateStatus(Long id, AccountStatus status) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: " + id));

        account.setStatus(status);

        return account;
    }


}