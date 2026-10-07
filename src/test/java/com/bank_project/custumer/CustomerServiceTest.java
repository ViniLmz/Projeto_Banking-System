package com.bank_project.custumer;

import com.bank_project.exception.CpfAlreadyExistsException;
import com.bank_project.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustumerRepository custumerRepository;

    @InjectMocks
    private CustomerService customerService;


    @Test
    void testSaveCustomer() {

        Custumer custumer = new Custumer();
        custumer.setId(1L);
        custumer.setCpf("12345688");
        custumer.setEmail("testCustumer@hotmail.com");

        when(custumerRepository.existsByCpf("12345688"))
                .thenReturn(false);

        when(custumerRepository.save(custumer))
                .thenReturn(custumer);

        Custumer custumerSaved = customerService.save(custumer);

        assertEquals(custumer, custumerSaved);
    }

    @Test
    void testSaveCustomerCpfAlreadyExists(){
        Custumer custumer = new Custumer();
        custumer.setId(1L);
        custumer.setCpf("12345688");
        custumer.setEmail("testCustumer@hotmail.com");

        when(custumerRepository.existsByCpf("12345688"))
                .thenReturn(true);

        assertThrows(CpfAlreadyExistsException.class,
                () -> customerService.save(custumer));

        verify(custumerRepository,never ()).save(custumer);
    }

    @Test
    void testFindAll(){

        Custumer custumer = new Custumer();
        custumer.setId(1L);
        custumer.setCpf("12345688");
        custumer.setEmail("testCustumer@hotmail.com");

        List<Custumer> customers = List.of(custumer);

        when(custumerRepository.findAll())
                .thenReturn(customers);

        List<Custumer> result =
                customerService.findAll();

        assertEquals(1, result.size());
        assertEquals(custumer, result.get(0));
    }

    @Test
    void testFindById(){
        Custumer custumer = new Custumer();
        custumer.setId(1L);
        custumer.setCpf("12345688");
        custumer.setEmail("testCustumer@hotmail.com");

        when(custumerRepository.findById(1l))
                .thenReturn(Optional.of(custumer));

        Optional<Custumer> foundId = customerService.findById(1L);

        assertTrue(foundId.isPresent());
        assertEquals(custumer, foundId.get());
    }


    @Test
    void testDeleteAll(){

        when(custumerRepository.existsById(1L))
                .thenReturn(true);

        customerService.delete(1L);

        verify(custumerRepository).existsById(1L);
        verify(custumerRepository).deleteById(1L);
    }

    @Test
    void testDeleteCustomerNotFound() {

        when(custumerRepository.existsById(1l))
                .thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.delete(1l));

        verify(custumerRepository, never()).deleteById(1l);
    }


    @Test
    void testUpdateCustomer(){
        Custumer custumer = new Custumer();
        custumer.setId(1L);
        custumer.setCpf("12345688");
        custumer.setEmail("testCustumer@hotmail.com");

        when(custumerRepository.existsById(1L))
                .thenReturn(true);

        when(custumerRepository.existsByCpfAndIdNot("12345688", 1L))
                .thenReturn(false);

        when(custumerRepository.save(custumer))
                .thenReturn(custumer);

        Custumer result = customerService.update(1L, custumer);

        assertEquals(custumer, result);
        verify(custumerRepository).save(custumer);
    }
    @Test
    void testCPFAlreadyUsed(){
        Custumer custumer = new Custumer();
        custumer.setId(1L);
        custumer.setCpf("12345688");
        custumer.setEmail("testCustumer@hotmail.com");

        when(custumerRepository.existsById(1l))
                .thenReturn(true);


        when(custumerRepository.existsByCpfAndIdNot("12345688",1l))
                .thenReturn(true);

        assertThrows(CpfAlreadyExistsException.class,
                () -> customerService.update(1l, custumer));

        verify(custumerRepository, never()).save(custumer);
    }

    @Test
    void testUpdateCustomerNotFound(){
        Custumer custumer = new Custumer();
        custumer.setId(1L);
        custumer.setCpf("12345688");
        custumer.setEmail("testCustumer@hotmail.com");

        when(custumerRepository.existsById(1l))
                .thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                ()-> customerService.update(1l,custumer));

        verify(custumerRepository, never()).save(custumer);

    }

    @Test
    void testFindByIdNotFound() {
        when(custumerRepository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<Custumer> result = customerService.findById(1L);

        assertTrue(result.isEmpty());
    }




}