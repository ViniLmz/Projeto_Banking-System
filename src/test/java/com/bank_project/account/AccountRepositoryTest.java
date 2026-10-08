package com.bank_project.account;




import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository repository;

    @Test
    void testFindAll(){
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);

        repository.save(account);

        List<Account> result = repository.findAll();


        assertEquals(1, result.size());
        assertThat(result).contains(account);
    }

    @Test
    void testFindAllEmpty(){
        List<Account> result = repository.findAll();
       assertThat(result).isEmpty();
    }

    @Test
    void testFindById(){
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);

        Account saved = repository.save(account);

        var result = repository.findById(saved.getId());

      assertThat(result).isPresent();
      assertThat(saved.getId()).isEqualTo(result.get().getId());
    }

    @Test
    void testNotFindById() {
        var result = repository.findById(999L);

        Assertions.assertThat(result).isEmpty();
    }

    @Test
    void testDeleteById(){
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);

       repository.save(account);
       var id = account.getId();
       repository.deleteById(id);
        var result = repository.existsById(id);
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void testExistsByAccountNumber(){
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);


       var result = repository.existsByAccountNumber(account.getAccountNumber());

        assertThat(result).isTrue();
    }

    @Test
    void existsByAccountNumberNotFound() {
        var result = repository.existsByAccountNumber("999999");

        assertThat(result).isFalse();
    }
}
