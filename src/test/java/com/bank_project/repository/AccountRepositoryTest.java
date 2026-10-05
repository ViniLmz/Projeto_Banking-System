package com.bank_project.repository;

import com.bank_project.account.Account;
import com.bank_project.account.AccountRepository;
import com.bank_project.account.AccountStatus;
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

    @Test
    void shouldFindAllAccounts() {
        Account account1 = new Account();
        account1.setStatus(AccountStatus.ACTIVE);
        account1.setBalance(new BigDecimal("4000.00"));

        Account account2 = new Account();
        account2.setStatus(AccountStatus.ACTIVE);
        account2.setBalance(new BigDecimal("2000.00"));

        repository.save(account1);
        repository.save(account2);

        var accounts = repository.findAll();

        assertEquals(2, accounts.size());
    }


    @Test
    void testShouldDeleteById(){

        Account account = new Account();

        repository.save(account);

        var id = account.getId();

        repository.deleteById(id);

        assertFalse(repository.findById(id).isPresent());
    }

    @Test
    void testExistsByAccountNumber(){
        Account account = new Account();
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountNumber("1234");

        repository.save(account);

        var exists = repository.existsByAccountNumber("1234");

        assertTrue(exists);

    }

    @Test
    void testNotExistsByAccountNumber (){

        Account account = new Account();
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountNumber("1234");

        repository.save(account);

        var exists = repository.existsByAccountNumber("3234");

        assertFalse(exists);

    }




}

