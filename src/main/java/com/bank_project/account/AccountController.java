package com.bank_project.account;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")

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
    public ResponseEntity  <Account> create (@Valid @RequestBody Account account){
      Account  accountSaved = accountService.save(account);
      return  ResponseEntity.status(HttpStatus.CREATED).body(accountSaved);
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
