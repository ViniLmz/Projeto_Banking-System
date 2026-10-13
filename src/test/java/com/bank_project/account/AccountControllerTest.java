package com.bank_project.account;



import com.bank_project.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void shouldReturn201WhenCreate() throws Exception {

        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("123456");
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountService.save(any(AccountRequest.class)))
                .thenReturn(account);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "accountNumber": "123456",
                            "balance": 4000.00,
                            "accountType": "CHECKING",
                            "status": "ACTIVE",
                            "customerId": 1
                        }
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn200WhenDeposited() throws Exception {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("123456");
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountService.deposit(eq(1L), any(TransactionRequest.class)))
                .thenReturn(account);

        mockMvc.perform(post("/api/accounts/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "amount": 4000
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200WhenWithdraw()  throws Exception{
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("123456");
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountService.withdraw(eq(1l),any(TransactionRequest.class)))
                .thenReturn(account);

        mockMvc.perform(post("/api/accounts/1/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            "amount": 1000
                            }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200WhenUpdated()  throws Exception{
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("123456");
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);

            when(accountService.update(eq(1l),any(AccountUpdateRequest.class)))
                .thenReturn(account);

        mockMvc.perform(put("/api/accounts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "accountType": "SAVINGS"
                            }
                            """))
                .andExpect(status().isOk());

    }

    @Test
    void shouldReturn200WhenStatusUpdated()  throws Exception{
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("123456");
        account.setBalance(new BigDecimal("4000.00"));
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.BLOCKED);

        when(accountService.updateStatus(eq(1l),any(AccountStatus.class)))
                .thenReturn(account);

        mockMvc.perform(patch("/api/accounts/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "BLOCKED"
                        }
                        """

                )).andExpect(status().isOk());
    }

    @Test
    void shouldReturn204WhenDeleted() throws Exception{
        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn200WhenGetAll() throws  Exception{
       mockMvc.perform(get("/api/accounts")).andExpect(status().isOk());

    }

    @Test
    void shouldReturn404WhenAccountNotFound() throws Exception {
        when(accountService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldReturn404WhenDeletingAccountNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Account not found with id: 1"))
                .when(accountService).delete(1L);

        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isNotFound());
    }




}
