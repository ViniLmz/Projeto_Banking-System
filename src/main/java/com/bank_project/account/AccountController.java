package com.bank_project.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")

public class AccountController {

   private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(
            summary = "List of accounts ",
            description = "Returns a list of accounts registered in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account found successfully"
            ),
    })
    @GetMapping
    public ResponseEntity<List<Account>> getAll() {
        return ResponseEntity.ok(accountService.findAll());
    }

    @Operation(
            summary = "Find account by ID",
            description = "Returns an account by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account found successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid account id"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity <Account> getId (@PathVariable Long id){
        return accountService.findById(id).
                map(ResponseEntity::ok).
                orElse(ResponseEntity.notFound().build());

    }

    @Operation(
            summary = "Create account",
            description = "Creates a new account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid account data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Account number already exists"),


    })
    @PostMapping
    public ResponseEntity<Account> create(@RequestBody @Valid AccountRequest request) {
        Account accountSaved = accountService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountSaved);
    }


    @Operation(
            summary = "Deposit money into an account",
            description = "Deposits a specified amount into an existing account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Deposit completed successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid deposit amount"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"),
            @ApiResponse(
                    responseCode = "422",
                    description = "Account is blocked")
    })
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Account> deposit(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        Account accountDeposit = accountService.deposit(id, request);

        return ResponseEntity.ok(accountDeposit);
    }

    @Operation(
            summary = "Withdraw money from an account",
            description = "Withdraws a specified amount from an existing account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Withdraw completed successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid Withdraw amount"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"),
            @ApiResponse(
                    responseCode = "422",
                    description = "Account is blocked or has insufficient balance"),
    })
    @PostMapping ("/{id}/withdraw")
    public ResponseEntity  <Account> withdraw  (@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        Account accountWithdraw = accountService.withdraw(id, request);
        return ResponseEntity.ok(accountWithdraw);
    }



    @Operation(
            summary = "Update account",
            description = "Updates an existing account's information using its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid account data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    @PutMapping ("/{id}")
    public ResponseEntity  <Account> update (@PathVariable Long id, @Valid @RequestBody  AccountUpdateRequest request) {
       Account accountUpdated = accountService.update(id, request);
       return ResponseEntity.ok(accountUpdated);
    }


    @Operation(
            summary = "Update account status",
            description = "Updates the status of an existing account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account updated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<Account> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request) {

        Account accountUpdated =
                accountService.updateStatus(id, request.status());

        return ResponseEntity.ok(accountUpdated);
    }

    @Operation(
            summary = "Delete account",
            description = "Deletes a accounts using their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Account deleted successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found")
    })
    @DeleteMapping ("/{id}")
    public ResponseEntity <Void> delete (@PathVariable Long id){
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
