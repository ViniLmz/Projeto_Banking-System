package com.bank_project.ledger;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }




    @GetMapping("/accounts/{accountId}")
    public List<LedgerEntryResponse> findByAccount(
            @PathVariable Long accountId
    ) {
        return ledgerService.findByAccount(accountId);
    }


}

