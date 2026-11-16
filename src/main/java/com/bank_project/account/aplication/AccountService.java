
package com.bank_project.account.aplication;

import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.http.AccountRequest;
import com.bank_project.account.http.AccountUpdateRequest;
import com.bank_project.account.http.TransactionRequest;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.audit.AuditLogService;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.AccountNumberAlreadyExistsException;
import com.bank_project.exception.BlockedAccountException;
import com.bank_project.exception.InsufficientBalanceException;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.ledger.LedgerService;
import com.bank_project.user.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final LedgerService ledgerService;
    private final AccountRepository accountRepository;
    private final CustumerRepository custumerRepository;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    public AccountService(
            LedgerService ledgerService, AccountRepository accountRepository,
            CustumerRepository custumerRepository,
            CurrentUserService currentUserService,
            AuditLogService auditLogService
    ) {
        this.ledgerService = ledgerService;
        this.accountRepository = accountRepository;
        this.custumerRepository = custumerRepository;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
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

    @Transactional
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

        Account savedAccount = accountRepository.save(account);

        auditLogService.log(
                "ACCOUNT_CREATED",
                "ACCOUNT",
                savedAccount.getId().toString(),
                "Account created: " + savedAccount.getAccountNumber()
        );

        return savedAccount;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));

        auditLogService.log(
                "ACCOUNT_DELETED",
                "ACCOUNT",
                account.getId().toString(),
                "Account deleted: " + account.getAccountNumber()
        );

        accountRepository.delete(account);
    }


    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Account update(Long id, AccountUpdateRequest request) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));

        account.setAccountType(request.accountType());

        auditLogService.log(
                "ACCOUNT_UPDATED",
                "ACCOUNT",
                account.getId().toString(),
                "Account type updated to: " + account.getAccountType()
        );

        return account;
    }



    @Transactional
    public Account deposit(Long id, TransactionRequest request) {

        Account account = findAccountForCurrentUserWithLock(id);

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("This account is currently blocked");
        }

        account.setBalance(
                account.getBalance().add(request.amount())
        );

        Account savedAccount = accountRepository.save(account);
        ledgerService.recordDeposit(savedAccount, request.amount());

        auditLogService.log(
                "ACCOUNT_DEPOSIT",
                "ACCOUNT",
                savedAccount.getId().toString(),
                "Deposit of " + request.amount()
                        + " performed. New balance: " + savedAccount.getBalance()
        );

        return savedAccount;
    }



    @Transactional
    public Account withdraw(Long id, TransactionRequest request) {

        Account account = findAccountForCurrentUserWithLock(id);


        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("This account is currently blocked");
        }

        if (request.amount().compareTo(account.getBalance()) > 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(
                account.getBalance().subtract(request.amount())
        );

        Account savedWithdraw = accountRepository.save(account);
        ledgerService.recordWithdrawal(savedWithdraw, request.amount());

        auditLogService.log(
                "ACCOUNT_WITHDRAWAL",
                "ACCOUNT",
                savedWithdraw.getId().toString(),
                "Withdrawal of " + request.amount()
                        + " performed. New balance: " + savedWithdraw.getBalance()
        );

        return savedWithdraw;
    }



    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Account updateStatus(Long id, AccountStatus status) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: " + id));

        account.setStatus(status);

        auditLogService.log(
                "ACCOUNT_STATUS_UPDATED",
                "ACCOUNT",
                account.getId().toString(),
                "Account status updated to: " + account.getStatus()
        );

        return account;
    }



    private Account findAccountForCurrentUserWithLock(Long id) {

        if (currentUserService.isAdmin()) {
            return accountRepository.findByIdWithLock(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account not found with id: " + id));
        }

        Long customerId = currentUserService.getCurrentCustomerId();

        return accountRepository.findByIdAndCustomerIdWithLock(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));
    }


}
