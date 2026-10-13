package com.bank_project.custumer;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import org.springframework.http.MediaType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CustomerController.class)
public class CostumerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;


    @Test
    void shouldReturn200OKWhenFetchingCustomers() throws Exception {

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk());
    }


    @Test
    void shouldReturnById200() throws Exception {
        Custumer custumer = new Custumer();
        custumer.setId(1L);
        custumer.setName("Vinicius Lima");
        custumer.setEmail("vinicius@email.com");
        custumer.setCpf("12345678900");

        when(customerService.findById(1L))
                .thenReturn(Optional.of(custumer));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenCustomerNotFound() throws Exception{
        Custumer custumer = new Custumer();

        when(customerService.findById(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn201WhenCreate() throws Exception {
        Custumer custumer = new Custumer();
        custumer.setId(1L);
        custumer.setName("Vinicius Lima");
        custumer.setEmail("vinicius@email.com");
        custumer.setCpf("12345678900");

        when(customerService.save(any(Custumer.class)))
                .thenReturn(custumer);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Vinicius Lima",
                                "email": "vinicius@email.com",
                                "cpf": "12345678900"
                            }
                            """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn200WhenUpdate() throws Exception {
        Custumer custumer = new Custumer();
        custumer.setId(1L);
        custumer.setName("Vinicius Lima");
        custumer.setEmail("vinicius@email.com");
        custumer.setCpf("12345678900");

        when(customerService.update(eq(1L), any(Custumer.class)))
                .thenReturn(custumer);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Vinicius Lima",
                                "email": "vinicius@email.com",
                                "cpf": "12345678900"
                            }
                            """))
                .andExpect(status().isOk());
    }


    @Test
    void shouldDeleteByIdReturn204() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }



}
