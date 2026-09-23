package com.bank_project.account;

import com.bank_project.exception.AccountNumberAlreadyExistsException;
import com.bank_project.exception.BlockedAccountException;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.exception.InsufficientBalanceException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Mock
    private final AccountRepository accountRepository;

    @Mock
    private final CustumerRepository custumerRepository;



    public AccountService(AccountRepository accountRepository, CustumerRepository custumerRepository) {
        this.accountRepository = accountRepository;
        this.custumerRepository = custumerRepository;
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
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

        Custumer custumer = custumerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.customerId()));
        account.setCustomer(custumer);

        return accountRepository.save(account);
    }

    @Transactional
    public void delete(Long id) {

        if (!accountRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Account not found with id: " + id
            );
        }

        accountRepository.deleteById(id);
    }

    @Transactional
    public Account update(Long id, AccountUpdateRequest request) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + id
                ));

        account.setAccountType(request.accountType());

        return account;
    }


    public Account deposit(Long id, TransactionRequest request) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("This account is currently blocked");
        }

        account.setBalance(
                account.getBalance().add(request.amount())
        );

        return accountRepository.save(account);
    }

    public Account withdraw(Long id, TransactionRequest request) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

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




