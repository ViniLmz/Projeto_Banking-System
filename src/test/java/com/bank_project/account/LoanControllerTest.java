package com.bank_project.account;


import com.bank_project.custumer.Custumer;
import com.bank_project.loan.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
public class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    @Test
    void shouldReturn201WhenLoanIsCreated() throws Exception {

        Custumer custumer = new Custumer();
        custumer.setId(1l);
        Account account = new Account();
        account.setId(2l);
        account.setCustomer(custumer);

       LoanRequest request =
                new LoanRequest
                        (1l,
                        2l,
                        new BigDecimal("4000.00"),
                        new BigDecimal(2.5),
                        12);

        Loan loan = new Loan(
                null,
                custumer,
                account,
                request.amount(),
                request.interestRate(),
                request.installments(),
                "PENDING",
                LocalDateTime.now()
        );
                when(loanService.save(request)).thenReturn(loan);

                        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "customerId": 1,
                                "accountId": 2,
                                "amount": 4000.00,
                                "interestRate": 2.5,
                                 "installments": 12
                                }
                           
                                """)).andExpect(status().isCreated());
    }


    @Test
    void shouldReturn200WhenGetAll() throws Exception {
        when(loanService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturn200WhenGetId() throws Exception{
        Account account = new Account();
        Custumer custumer = new Custumer();

        Loan loan = new Loan();
        loan.setAccount(account);
        loan.setId(1l);
        loan.setCustomer(custumer);
        loan.setAmount(new BigDecimal("1000.00"));
        loan.setInstallments(24);
        loan.setInterestRate(new BigDecimal(2.5));
        loan.setStatus("APPROVED");

        when(loanService.findById(1l)).thenReturn(Optional.of(loan));

        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1l))
                .andExpect(jsonPath("$.amount").value(1000.00))
                .andExpect(jsonPath("$.installments").value(24))
                .andExpect(jsonPath("$.interestRate").value(2.5))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldReturn204WhenDelete() throws Exception {
        mockMvc.perform(delete("/api/loans/1"))
                .andExpect(status().isNoContent());

        verify(loanService).delete(1l);

    }


    @Test
    void shouldReturn200WhenUpdated() throws Exception {

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setAmount(new BigDecimal("10.00"));
        loan.setInstallments(24);
        loan.setInterestRate(new BigDecimal("3.5"));

        when(loanService.update(eq(1L), any(LoanUpdateRequest.class)))
                .thenReturn(loan);

        mockMvc.perform(put("/api/loans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "amount": 10.00,
                                "interestRate": 3.5,
                                "installments": 24
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installments").value(24))
                .andExpect(jsonPath("$.amount").value(10.00))
                .andExpect(jsonPath("$.interestRate").value(3.5));
    }


    @Test
    void shouldReturn200WhenApproved() throws Exception {

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setStatus("APPROVED");
        loan.setAmount(new BigDecimal("1000.00"));

        when(loanService.approve(1L)).thenReturn(loan);

        mockMvc.perform(patch("/api/loans/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.amount").value(1000.00));
    }


    @Test
    void shouldReturn200WhenRejected() throws Exception {

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setStatus("REJECTED");
        loan.setAmount(new BigDecimal("1000.00"));

        when(loanService.reject(1L)).thenReturn(loan);

        mockMvc.perform(patch("/api/loans/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.amount").value(1000.00));
    }

}
