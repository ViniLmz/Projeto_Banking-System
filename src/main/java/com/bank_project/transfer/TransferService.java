
package com.bank_project.transfer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.bank_project.account.domain.Account;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.exception.*;
import com.bank_project.user.CurrentUserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final CurrentUserService currentUserService;

    public TransferService(
            AccountRepository accountRepository,
            TransferRepository transferRepository,
            CurrentUserService currentUserService) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.currentUserService = currentUserService;
    }

    private Account findSourceAccountForCurrentUser(Long id) {

        if (currentUserService.isAdmin()) {
            return accountRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Source account not found with id: " + id));
        }

        Long customerId = currentUserService.getCurrentCustomerId();

        return accountRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Source account not found with id: " + id));
    }

    @Transactional
    public Transfer transfer(String idempotencyKey, TransferRequest request) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key cannot be blank");
        }

        findSourceAccountForCurrentUser(request.sourceAccountId());

        Optional<Transfer> existingTransfer =
                transferRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTransfer.isPresent()) {

            Transfer existing = existingTransfer.get();

            boolean sameSource = existing.getSourceAccount().getId()
                    .equals(request.sourceAccountId());

            boolean sameTarget = existing.getTargetAccount().getId()
                    .equals(request.targetAccountId());

            boolean sameAmount = existing.getAmount()
                    .compareTo(request.amount()) == 0;

            if (!sameSource || !sameTarget || !sameAmount) {
                throw new IdempotencyKeyConflictException(
                        "Idempotency key already used with different transfer data"
                );
            }

            return existing;
        }


        List<Account> lockedAccounts = accountRepository.findAllByIdsWithLock(
                List.of(request.sourceAccountId(), request.targetAccountId())
        );

        Map<Long, Account> accountsById = lockedAccounts.stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

            Account source = accountsById.get(request.sourceAccountId());

        if (source == null || (!currentUserService.isAdmin()
                && !source.getCustomer().getId()
                .equals(currentUserService.getCurrentCustomerId()))) {

            throw new ResourceNotFoundException(
                    "Source account not found with id: " + request.sourceAccountId());
        }

        Account target = accountsById.get(request.targetAccountId());

        if (target == null) {
            throw new ResourceNotFoundException(
                    "Target account not found with id: " + request.targetAccountId());
        }

        if (source.getId().equals(target.getId())) {
            throw new SameAccountTransferException(
                    "Source and target accounts must be different");
        }

        if (source.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("Source account is blocked");
        }

        if (target.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("Target account is blocked");
        }

        if (source.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }


        source.setBalance(source.getBalance().subtract(request.amount()));
        target.setBalance(target.getBalance().add(request.amount()));

        accountRepository.save(source);
        accountRepository.save(target);


        Transfer transfer = new Transfer(
                null,
                source,
                target,
                request.amount(),
                LocalDateTime.now(),
                "COMPLETED",
                idempotencyKey
        );

        return transferRepository.save(transfer);
    }

    public List<Transfer> findAll() {

        if (currentUserService.isAdmin()) {
            return transferRepository.findAll();
        }



        Long customerId = currentUserService.getCurrentCustomerId();

        return transferRepository
                .findBySourceAccount_Customer_IdOrTargetAccount_Customer_Id(
                        customerId,
                        customerId
                );
    }
}
