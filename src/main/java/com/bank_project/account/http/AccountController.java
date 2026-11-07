
package com.bank_project.account.http;

import com.bank_project.account.aplication.AccountService;
import com.bank_project.account.domain.Account;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account Controller", description = "Account management endpoints")
public class AccountController {

    private final AccountMapper accountMapper;
    private final AccountService accountService;

    public AccountController(AccountMapper accountMapper, AccountService accountService) {
        this.accountMapper = accountMapper;
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
    public ResponseEntity<List<AccountResponse>> getAll() {

        List<AccountResponse> accounts = accountService.findAll()
                .stream()
                .map(accountMapper::toResponse)
                .toList();

        return ResponseEntity.ok(accounts);
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
    public ResponseEntity<AccountResponse> getId(@PathVariable Long id) {

        return accountService.findById(id)
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create account",
            description = "Creates a new account. Customers create accounts for themselves, " +
                    "while administrators can specify the customer ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid account data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "409", description = "Account number already exists")
    })
    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody AccountRequest request) {

        Account accountSaved = accountService.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(accountSaved));
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
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        Account account = accountService.deposit(id, request);

        return ResponseEntity.ok(toResponse(account));
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
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        Account account = accountService.withdraw(id, request);

        return ResponseEntity.ok(toResponse(account));
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
    public ResponseEntity<AccountResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request) {

        Account account = accountService.update(id, request);

        return ResponseEntity.ok(toResponse(account));
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
    public ResponseEntity<AccountResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        Account account = accountService.updateStatus(id, request.status());

        return ResponseEntity.ok(toResponse(account));
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

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getAccountType(),
                account.getStatus(),
                account.getCustomer().getId()
        );
    }
}


