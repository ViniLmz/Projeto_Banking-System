package com.bank_project.repository;

import com.bank_project.account.Account;
import com.bank_project.account.AccountRepository;
import com.bank_project.account.AccountStatus;
import com.bank_project.account.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;



@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountRepositoryTest {
    @Autowired
    AccountRepository repository;

    @Test
    void shouldSaveAccount() {
        Account account = new Account();
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("4000.00"));

        Account result = repository.save(account);

        var found = repository.findById(result.getId());

        assertNotNull(result.getId());
        assertTrue(found.isPresent());
    }
}

