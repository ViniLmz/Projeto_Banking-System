package com.bank_project.account;

import com.bank_project.exception.AccountNumberAlreadyExistsException;
import com.bank_project.exception.BlockedAccountException;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.user.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.exception.InsufficientBalanceException;
import java.util.List;
import java.util.Optional;


@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustumerRepository custumerRepository;
    private final CurrentUserService currentUserService;


    public AccountService(AccountRepository accountRepository, CustumerRepository custumerRepository, CurrentUserService currentUserService) {
        this.accountRepository = accountRepository;
        this.custumerRepository = custumerRepository;
        this.currentUserService = currentUserService;
    }

    public List<Account> findAll() {
        Long customerId = currentUserService.getCurrentCustomerId();

        return accountRepository.findAllByCustomerId(customerId);
    }

    public Optional<Account> findById(Long id) {

        Long customerId = currentUserService.getCurrentCustomerId();

        return accountRepository.findByIdAndCustomerId(id, customerId);
    }


    public Account save(AccountRequest request) {

        if (accountRepository.existsByAccountNumber(request.accountNumber())) {
            throw new AccountNumberAlreadyExistsException("Account number already exists");
        }

        Account account = new Account(
                null,
                request.accountNumber(),
                request.balance(),
                request.accountType(),
                request.status()
        );

        Custumer custumer = currentUserService.getCurrentUser().getCustomer();
        account.setCustomer(custumer);

        return accountRepository.save(account);
    }

    @Transactional
    public void delete(Long id) {

        Long customerId = currentUserService.getCurrentCustomerId();

        Account account = accountRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));

        accountRepository.delete(account);
    }

    @Transactional
    public Account update(Long id, AccountUpdateRequest request) {

        Long customerId = currentUserService.getCurrentCustomerId();

        Account account = accountRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));

        account.setAccountType(request.accountType());

        return account;
    }


    public Account deposit(Long id, TransactionRequest request) {

        Long customerId = currentUserService.getCurrentCustomerId();

        Account account = accountRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));
        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("This account is currently blocked");
        }

        account.setBalance(
                account.getBalance().add(request.amount())
        );

        return accountRepository.save(account);
    }

    public Account withdraw(Long id, TransactionRequest request) {


        Long customerId = currentUserService.getCurrentCustomerId();

        Account account = accountRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id));

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("This account is currently blocked");
        }

        if (request.amount().compareTo(account.getBalance()) <= 0) {
            account.setBalance(
                    account.getBalance().subtract(request.amount())
            );
        } else {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        return accountRepository.save(account);
    }

    @Transactional
    public Account updateStatus(Long id, AccountStatus status) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found with id: " + id));

        account.setStatus(status);

        return accountRepository.save(account);
    }


}




