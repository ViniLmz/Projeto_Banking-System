package com.bank_project.ledger;


import com.bank_project.account.infra.AccountRepository;
import com.bank_project.user.CurrentUserService;
import com.bank_project.account.domain.Account;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.transfer.Transfer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LedgerService {

    private final CurrentUserService currentUserService;
    private final LedgerEntryMapper ledgerEntryMapper;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRepository accountRepository;

    public LedgerService(CurrentUserService currentUserService, LedgerEntryMapper ledgerEntryMapper, LedgerEntryRepository ledgerEntryRepository, AccountRepository accountRepository) {
        this.currentUserService = currentUserService;
        this.ledgerEntryMapper = ledgerEntryMapper;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void recordTransfer(Transfer transfer) {

        if (ledgerEntryRepository.existsByTransferId(transfer.getId())) {
            throw new IllegalStateException(
                    "Ledger entries already exist for transfer: " + transfer.getId()
            );
        }

        LedgerEntry debit = new LedgerEntry(
                transfer.getSourceAccount(),
                transfer,
                EntryType.DEBIT,
                transfer.getAmount()
        );

        LedgerEntry credit = new LedgerEntry(
                transfer.getTargetAccount(),
                transfer,
                EntryType.CREDIT,
                transfer.getAmount()
        );

        ledgerEntryRepository.saveAll(List.of(debit, credit));
    }


    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> findByAccount(Long accountId) {

        Account account;

        if (currentUserService.isAdmin()) {
            account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account not found with id: " + accountId));
        } else {
            Long customerId = currentUserService.getCurrentCustomerId();

            account = accountRepository.findByIdAndCustomerId(accountId, customerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account not found with id: " + accountId));
        }

        return ledgerEntryRepository
                .findAllByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(ledgerEntryMapper::toResponse)
                .toList();
    }



}
