package com.bank_project.repository;


import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CustumerRepositoryTest {

    @Autowired
    CustumerRepository repository;

    @Test
    void existsByCpf(){
        Custumer custumer = new Custumer();
        custumer.setName("Vini");
        custumer.setCpf("493827879-44");
        custumer.setEmail("test@hotmail.com");

        repository.save(custumer);

        var exists = repository.existsByCpf("493827879-44");

        assertTrue(exists);
    }

    @Test
    void testExistsByCpfAndIdNot() {
        Custumer custumer1 = new Custumer();
        custumer1.setName("Vini");
        custumer1.setCpf("493827879-44");
        custumer1.setEmail("test@hotmail.com");

        Custumer custumer2 = new Custumer();
        custumer2.setName("Lucas");
        custumer2.setCpf("393827879-44");
        custumer2.setEmail("test2@hotmail.com");

        repository.save(custumer1);
        repository.save(custumer2);

        var exists = repository.existsByCpfAndIdNot(
                custumer1.getCpf(),
                custumer1.getId()
        );

        assertFalse(exists);
    }

    @Test
    void shouldFindAllCustumers() {

        Custumer custumer1 = new Custumer();
        custumer1.setName("Vini");
        custumer1.setCpf("493827879-44");
        custumer1.setEmail("test@hotmail.com");

        Custumer custumer2 = new Custumer();
        custumer2.setName("Lucas");
        custumer2.setCpf("393827879-44");
        custumer2.setEmail("test2@hotmail.com");

        repository.save(custumer2);
        repository.save(custumer1);

        var findAll = repository.findAll();
        assertEquals(2,findAll.size());
    }

    @Test
    void shouldFindCustumerById() {
        Custumer custumer = new Custumer();
        custumer.setName("Vini");
        custumer.setCpf("493827879-44");
        custumer.setEmail("test@hotmail.com");

        repository.save(custumer);

        var id = custumer.getId();

        var custumers = repository.findById(id);

        assertTrue(custumers.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenCustumerDoesNotExist(){

        var result = repository.findById(22222l);

        assertTrue(result.isEmpty());

    }

    @Test
    void shouldDeleteCustumerById() {
        Custumer custumer = new Custumer();
        custumer.setName("Vini");
        custumer.setCpf("493827879-44");
        custumer.setEmail("test@hotmail.com");

        repository.save(custumer);

        var id = custumer.getId();

        repository.deleteById(id);

        assertFalse(repository.findById(id).isPresent());
    }






}
