package com.bank_project.custumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.lang.classfile.Opcode;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void testFindById(){
        Custumer custumer = new Custumer();
        custumer.setName("Vinicius Lima");
        custumer.setEmail("vinicius@email.com");
        custumer.setCpf("12345678900");

        Custumer saved = repository.save(custumer);

      var result = repository.findById(saved.getId());

      assertThat(result).isPresent();
      assertThat(saved.getId()).isEqualTo(result.get().getId());

    }


    @Test
    void testNotFindById() {
        var result = repository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindAll(){
        Custumer custumer1 = new Custumer();
        custumer1.setName("Vinicius Lima");
        custumer1.setEmail("vinicius@email.com");
        custumer1.setCpf("12345678900");

        Custumer custumer2 = new Custumer();
        custumer2.setName("Angelo Silva");
        custumer2.setEmail("angelo@email.com");
        custumer2.setCpf("374674");

        repository.save(custumer1);
        repository.save(custumer2);

        List<Custumer> result = repository.findAll();

        assertEquals(2, result.size());
        assertThat(result).contains(custumer1, custumer2);

    }

        @Test
        void testFindAllEmpty(){
            List<Custumer> result = repository.findAll();
            assertThat(result).isEmpty();
        }

        @Test
        void testDeleteById(){
            Custumer custumer = new Custumer();
            custumer.setName("Vinicius Lima");
            custumer.setEmail("vinicius@email.com");
            custumer.setCpf("12345678900");

            repository.save(custumer);
            var id = custumer.getId();
            repository.deleteById(id);

            var result = repository.existsById(id);

            assertThat(result).isFalse();
        }






}