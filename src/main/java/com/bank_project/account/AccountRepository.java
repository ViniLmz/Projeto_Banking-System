package com.bank_project.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface AccountRepository extends  JpaRepository<Account, Long> {
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByIdAndCustomerId(Long accountId, Long customerId);
    List<Account> findAllByCustomerId(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.customer.id = :customerId")
    Optional<Account> findByIdAndCustomerIdWithLock(
            @Param("id") Long id,
            @Param("customerId") Long customerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id IN :ids ORDER BY a.id ASC")
    List<Account> findAllByIdsWithLock(@Param("ids") List<Long> ids);
}