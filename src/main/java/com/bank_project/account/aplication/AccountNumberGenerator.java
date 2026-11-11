
package com.bank_project.account.aplication;

import com.bank_project.account.infra.AccountRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {

    private final AccountRepository accountRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String generate() {

        String accountNumber;

        do {
            long number = secureRandom.nextLong(
                    1_000_000_000L,
                    10_000_000_000L
            );

            accountNumber = String.valueOf(number);

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}
