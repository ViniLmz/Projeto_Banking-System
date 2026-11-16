package com.bank_project.reconciliation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(
            ReconciliationService reconciliationService
    ) {
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ReconciliationResponse reconcile(
            @PathVariable Long accountId
    ) {
        return reconciliationService.reconcile(accountId);
    }
}
