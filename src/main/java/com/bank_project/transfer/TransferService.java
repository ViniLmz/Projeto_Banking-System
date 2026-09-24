package com.bank_project.transfer;

import java.time.LocalDateTime;
import java.util.List;

import com.bank_project.account.AccountStatus;
import com.bank_project.exception.BlockedAccountException;
import com.bank_project.exception.InsufficientBalanceException;
import com.bank_project.account.Account;
import com.bank_project.account.AccountRepository;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.exception.SameAccountTransferException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    public TransferService(AccountRepository accountRepository, TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }


    @Transactional
    public void transfer(TransferRequest request) {
        Account source = accountRepository.findById(request.sourceAccountId())
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Source account not found with id: " + request.sourceAccountId()));

        Account target = accountRepository.findById(request.targetAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Target account not found with id: " + request.targetAccountId()));

        if (source.getId().equals(target.getId())) {
            throw new SameAccountTransferException(
                    "Source and target accounts must be different"
            );
        }
        if (source.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("Source account is blocked");
        }

        if (target.getStatus() == AccountStatus.BLOCKED) {
            throw new BlockedAccountException("Target account is blocked");
        }


        if (source.getBalance().compareTo(request.amount()) <0){
            throw new InsufficientBalanceException("Insufficient balance");
        }

        source.setBalance(
                source.getBalance().subtract(request.amount())
        );
        target.setBalance(
                target.getBalance().add(request.amount())
        );


        accountRepository.save(source);
        accountRepository.save(target);


        Transfer transfer = new Transfer(
                null,
                source,
                target,
                request.amount(),
                LocalDateTime.now(),
                "COMPLETED"
        );
        transferRepository.save(transfer);

    }

    public List<Transfer> findAll() {
        return transferRepository.findAll();
    }



}