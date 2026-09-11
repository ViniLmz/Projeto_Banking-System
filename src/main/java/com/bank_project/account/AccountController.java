package com.bank_project.account;

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

    @GetMapping
    public ResponseEntity<List<Account>> getAll() {
        return ResponseEntity.ok(accountService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity <Account> getId (@PathVariable Long id){
        return accountService.findById(id).
                map(ResponseEntity::ok).
                orElse(ResponseEntity.notFound().build());

    }

    @PostMapping
    public ResponseEntity<Account> create(@RequestBody AccountRequest request) {
        Account accountSaved = accountService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountSaved);
    }

    @PostMapping ("/{id}/deposit")
    public ResponseEntity  <Account> deposit  (@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        Account accountDeposit = accountService.deposit(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountDeposit);
    }


    @PostMapping ("/{id}/withdraw")
    public ResponseEntity  <Account> withdraw  (@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        Account accountWithdraw = accountService.withdraw(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountWithdraw);
    }



    @PutMapping ("/{id}")
    public ResponseEntity  <Account> update (@PathVariable Long id, @Valid @RequestBody Account account) {
       Account accountUpdated = accountService.update(id, account);
       return ResponseEntity.ok(accountUpdated);
    }


    @DeleteMapping ("/{id}")
    public ResponseEntity <Void> delete (@PathVariable Long id){
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
