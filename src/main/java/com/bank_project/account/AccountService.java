package com.bank_project.account;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;


    public  AccountService (AccountRepository accountRepository) {
        this.accountRepository =accountRepository;
    }

    public List <Account> findAll()
    {
        return accountRepository.findAll();
    }

    public Optional <Account> findById (Long id)
    {
        return accountRepository.findById(id);
    }

    public Account save (Account account)
    {
        return accountRepository.save(account);
    }


    public void delete (Long id)
    {
         accountRepository.deleteById(id);
    }

    @Transactional
    public Account update ( Long id, Account account)
    {
        if (accountRepository.existsById(id))
        {
            account.setId(id);
            return accountRepository.save(account);
        }else throw new RuntimeException("Account  not found");
    }



}
