package com.bank_project.loan;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getAll() {
        List<Loan> loans = loanService.findAll();

        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getId(@PathVariable Long id){
      return loanService.findById(id)
              .map(ResponseEntity::ok)
              .orElse(ResponseEntity.notFound().build());

    }

    @PostMapping
    public ResponseEntity<Loan> create(@Valid @RequestBody LoanRequest request) {

        Loan savedLoan = loanService.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedLoan);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Loan> update(
            @PathVariable Long id,
            @Valid @RequestBody LoanUpdateRequest request) {

        Loan updatedLoan = loanService.update(id, request);

        return ResponseEntity.ok(updatedLoan);
    }

    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        loanService.delete(id);
        return ResponseEntity.noContent().build();

    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Loan> approve(@PathVariable Long id) {

        Loan loan = loanService.approve(id);

        return ResponseEntity.ok(loan);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Loan> reject (@PathVariable Long id){
        Loan loan = loanService.reject(id);

        return ResponseEntity.ok(loan);

    }



}