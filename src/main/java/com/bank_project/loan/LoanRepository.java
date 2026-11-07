package com.bank_project.loan;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Loan l WHERE l.id = :id")
    Optional<Loan> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Loan l WHERE l.id = :id AND l.customer.id = :customerId")
    Optional<Loan> findByIdAndCustomerIdWithLock(
            @Param("id") Long id,
            @Param("customerId") Long customerId
    );

    List<Loan> findByCustomerId(Long customerId);

    Optional<Loan> findByIdAndCustomerId(Long id, Long customerId);
}
