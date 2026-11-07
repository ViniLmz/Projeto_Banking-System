package com.bank_project.account;

import com.bank_project.account.domain.Account;
import com.bank_project.auth.JwtService;
import com.bank_project.exception.IdempotencyKeyConflictException;
import com.bank_project.transfer.Transfer;
import com.bank_project.transfer.TransferController;
import com.bank_project.transfer.TransferRequest;
import com.bank_project.transfer.TransferService;
import com.bank_project.user.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturn409WhenIdempotencyKeyIsUsedWithDifferentData() throws Exception {

        when(transferService.transfer(
                anyString(),
                any(TransferRequest.class)
        )).thenThrow(
                new IdempotencyKeyConflictException(
                        "Idempotency key already used with different transfer data"
                )
        );

        mockMvc.perform(post("/api/transfers")
                        .header("Idempotency-Key", "transfer-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "sourceAccountId": 1,
                        "targetAccountId": 2,
                        "amount": 5000.00
                    }
                    """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn200WhenTransfer() throws Exception {

        Account source = new Account();
        Account target = new Account();

        Transfer transfer = new Transfer(
                1L,
                source,
                target,
                new BigDecimal("2000.00"),
                LocalDateTime.of(2026, 11, 2, 15, 0),
                "COMPLETED",
                "transfer-123"
        );

        when(transferService.transfer(
                anyString(),
                any(TransferRequest.class)
        )).thenReturn(transfer);

        mockMvc.perform(post("/api/transfers")
                        .header("Idempotency-Key", "transfer-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "sourceAccountId": 1,
                            "targetAccountId": 2,
                            "amount": 2000.00
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(2000.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.idempotencyKey").value("transfer-123"));

        verify(transferService).transfer(
                anyString(),
                any(TransferRequest.class)
        );
    }

    @Test
    void shouldReturn200WhenFindAll() throws Exception {

        when(transferService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}