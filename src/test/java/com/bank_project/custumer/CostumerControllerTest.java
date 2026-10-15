package com.bank_project.custumer;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



@WebMvcTest(CustomerController.class)
public class CostumerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;



    @Test
    void shouldReturn200OKWhenFetchingCustomers() throws Exception {

        when(customerService.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1l));
    }

    @Test
    void shouldReturn404WhenCustomerNotFound() throws Exception {
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1l))
                .andExpect(jsonPath("$.name").value("Vinicius Lima"))
                .andExpect(jsonPath("$.email").value("vinicius@email.com"))
                .andExpect(jsonPath("$.cpf").value("12345678900"));

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vinicius Lima"))
                .andExpect(jsonPath("$.email").value("vinicius@email.com"))
                .andExpect(jsonPath("$.cpf").value("12345678900"));
    }


    @Test
    void shouldDeleteByIdReturn204() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).delete(1l);

    }



}
