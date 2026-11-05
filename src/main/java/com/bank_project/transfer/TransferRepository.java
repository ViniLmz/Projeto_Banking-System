package com.bank_project.transfer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    List<Transfer> findBySourceAccount_Customer_IdOrTargetAccount_Customer_Id(
            Long sourceCustomerId,
            Long targetCustomerId
    );
}