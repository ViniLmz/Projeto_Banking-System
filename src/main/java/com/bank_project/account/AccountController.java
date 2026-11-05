
package com.bank_project.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account Controller", description = "Account management endpoints")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(
            summary = "List accounts",
            description = "Returns accounts available to the authenticated user. " +
                    "Administrators can access all accounts."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public ResponseEntity<List<Account>> getAll() {
        return ResponseEntity.ok(accountService.findAll());
    }


    @Operation(
            summary = "Find account by ID",
            description = "Returns an account if it belongs to the authenticated customer " +
                    "or the user has administrator privileges."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Account> getId(@PathVariable Long id) {

        return accountService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create account",
            description = "Creates a new bank account for the authenticated customer."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid account data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "409", description = "Account number already exists")
    })
    @PostMapping
    public ResponseEntity<Account> create(
            @Valid @RequestBody AccountRequest request) {

        Account accountSaved = accountService.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountSaved);
    }

    @Operation(
            summary = "Deposit money",
            description = "Deposits a specified amount into an account owned by the authenticated customer."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid deposit amount"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Account is blocked")
    })
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Account> deposit(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        Account account = accountService.deposit(id, request);

        return ResponseEntity.ok(account);
    }

    @Operation(
            summary = "Withdraw money",
            description = "Withdraws a specified amount from an account owned by the authenticated customer."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid withdrawal amount"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Account is blocked or has insufficient balance")
    })
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Account> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        Account account = accountService.withdraw(id, request);

        return ResponseEntity.ok(account);
    }

    @Operation(
            summary = "Update account",
            description = "Updates an existing account's information. Restricted to administrators."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid account data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Administrator privileges required"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Account> update(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request) {

        Account account = accountService.update(id, request);

        return ResponseEntity.ok(account);
    }

    @Operation(
            summary = "Update account status",
            description = "Changes the status of an account. Restricted to administrators."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid account status"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Administrator privileges required"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<Account> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        Account account = accountService.updateStatus(id, request.status());

        return ResponseEntity.ok(account);
    }

    @Operation(
            summary = "Delete account",
            description = "Deletes an existing account. Restricted to administrators."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Administrator privileges required"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        accountService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
