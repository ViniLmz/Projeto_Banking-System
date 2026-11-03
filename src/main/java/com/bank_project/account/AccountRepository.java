package com.bank_project.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AccountRepository extends  JpaRepository<Account, Long> {
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByIdAndCustomerId(Long accountId, Long customerId);
    List<Account> findAllByCustomerId(Long customerId);

}