package com.bank_project.account;


import com.bank_project.transfer.TransferController;
import com.bank_project.transfer.TransferRequest;
import com.bank_project.transfer.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TransferController.class)
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;


    @Test
    void shouldReturn200WhenTransfer() throws Exception {

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "sourceAccountId": 1,
                            "targetAccountId": 2,
                            "amount": 2000.00
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successfully made"));

        verify(transferService).transfer(any(TransferRequest.class));
    }

    @Test
    void shouldReturn200WhenFindAll() throws Exception {
        when(transferService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/transfers")).andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));



    }

}
