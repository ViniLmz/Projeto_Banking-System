
package com.bank_project.account.domain;

import com.bank_project.custumer.Custumer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String accountNumber;

    @NotNull
    @PositiveOrZero
    private BigDecimal balance;

    @Column(name = "reconciliation_opening_balance", precision = 19, scale = 2)
    private BigDecimal reconciliationOpeningBalance;

    @Column(name = "reconciliation_started_at")
    private LocalDateTime reconciliationStartedAt;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Custumer customer;

    @PrePersist
    private void initializeReconciliation() {
        if (reconciliationOpeningBalance == null) {
            reconciliationOpeningBalance = balance != null ? balance : BigDecimal.ZERO;
        }

        if (reconciliationStartedAt == null) {
            reconciliationStartedAt = LocalDateTime.now();
        }
    }

    public void setCustomer(Custumer customer) {
        this.customer = customer;
    }

    public Custumer getCustomer() {
        return customer;
    }

    public Account(Long id, String accountNumber, BigDecimal balance,
                   AccountType accountType, AccountStatus status) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.status = status;
    }

    public Account() {}

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getReconciliationOpeningBalance() {
        return reconciliationOpeningBalance;
    }

    public void setReconciliationOpeningBalance(BigDecimal reconciliationOpeningBalance) {
        this.reconciliationOpeningBalance = reconciliationOpeningBalance;
    }

    public LocalDateTime getReconciliationStartedAt() {
        return reconciliationStartedAt;
    }

    public void setReconciliationStartedAt(LocalDateTime reconciliationStartedAt) {
        this.reconciliationStartedAt = reconciliationStartedAt;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public AccountStatus getStatus() {
        return status;
    }
}
