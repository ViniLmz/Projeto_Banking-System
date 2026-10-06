package com.bank_project.custumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest {

    @Autowired
    private CustumerRepository repository;

    @Test
    void testExistsByCpf() {

        Custumer custumer = new Custumer();
        custumer.setName("Vinicius Lima");
        custumer.setEmail("vinicius@email.com");
        custumer.setCpf("12345678900");

        repository.save(custumer);

        boolean result = repository.existsByCpf("12345678900");

        assertThat(result).isTrue();
    }

    @Test
    void testexistsByCpfAndIdNot() {
        Custumer custumer1 = new Custumer();
        custumer1.setName("Vinicius Lima");
        custumer1.setEmail("vinicius@email.com");
        custumer1.setCpf("12345678900");

        Custumer custumer2 = new Custumer();
        custumer2.setName("Marcos Andre");
        custumer2.setEmail("test@email.com");
        custumer2.setCpf("98765432100");

        repository.save(custumer1);
        repository.save(custumer2);

        boolean result = repository.existsByCpfAndIdNot(
                custumer2.getCpf(),
                custumer1.getId()
        );

        assertThat(result).isTrue();
    }

    @Test
    void testExistsByCpfNotFound(){
        boolean result = repository.existsByCpf("123455");

        assertThat(result).isFalse();

    }
    @Test
    void testExistsByCpfAndIdNotFalse(){
        Custumer custumer = new Custumer();
        custumer.setName("Vinicius Lima");
        custumer.setEmail("vinicius@email.com");
        custumer.setCpf("12345678900");

        repository.save(custumer);

        Boolean result = repository.existsByCpfAndIdNot(custumer.getCpf(),custumer.getId());

        assertThat(result).isFalse();

    }




}