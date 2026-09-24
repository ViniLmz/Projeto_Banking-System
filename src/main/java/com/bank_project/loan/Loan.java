package com.bank_project.loan;

import com.bank_project.account.Account;
import com.bank_project.custumer.Custumer;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Custumer customer;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private BigDecimal amount;

    private BigDecimal interestRate;

    private Integer installments;

    private String status;

    private LocalDateTime createdAt;


    public Loan() {
    }


    public Loan(Long id, Custumer customer, Account account, BigDecimal amount,
                BigDecimal interestRate, Integer installments,
                String status, LocalDateTime createdAt) {

        this.id = id;
        this.customer = customer;
        this.account=account;
        this.amount = amount;
        this.interestRate = interestRate;
        this.installments = installments;
        this.status = status;
        this.createdAt = createdAt;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount(){
        return account;
    }


    public Custumer getCustomer() {
        return customer;
    }

    public void setCustomer(Custumer customer) {
        this.customer = customer;
    }


    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }


    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}