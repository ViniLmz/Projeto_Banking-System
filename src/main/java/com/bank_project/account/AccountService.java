package com.bank_project.account;

import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
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
        Account account = new Account(
                null,
                request.accountNumber(),
                request.balance(),
                request.accountType(),
                request.status()
        );

        if (request.customerId() != null) {
            Custumer custumer = custumerRepository.findById(request.customerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.customerId()));
            account.setCustomer(custumer);
        }

        return accountRepository.save(account);
    }

    public void delete(Long id) {
        accountRepository.deleteById(id);
    }

    @Transactional
    public Account update(Long id, Account account) {
        if (accountRepository.existsById(id)) {
            account.setId(id);
            return accountRepository.save(account);
        } else {
            throw new RuntimeException("Account not found");
        }
    }


    public Account deposit(Long id, TransactionRequest request) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(
                account.getBalance().add(request.amount())
        );

        return accountRepository.save(account);
    }

    public Account withdraw(Long id, TransactionRequest request) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (request.amount().compareTo(account.getBalance()) <= 0) {
            account.setBalance(
                    account.getBalance().subtract(request.amount())
            );
        } else {
            throw new RuntimeException("Insufficient balance");
        }

        return accountRepository.save(account);
    }


}




