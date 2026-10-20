package com.bank_project.loan;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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


    @Operation(
            summary = "List of Loans ",
            description = "Returns a list of loans registered in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Loans found successfully"
            ),
    })
    @GetMapping
    public ResponseEntity<List<Loan>> getAll() {
        List<Loan> loans = loanService.findAll();

        return ResponseEntity.ok(loans);
    }

    @Operation(
            summary = "Find Loan By ID",
            description = "Return by id a specific loan registered in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Loan found by id successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid loan id"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getId(@PathVariable Long id){
      return loanService.findById(id)
              .map(ResponseEntity::ok)
              .orElse(ResponseEntity.notFound().build());

    }

    @Operation(
            summary = "Create Loan",
            description = "Creates a new Loan"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Loan created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid loan data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found"
            ),
    })
    @PostMapping
    public ResponseEntity<Loan> create(@Valid @RequestBody LoanRequest request) {

        Loan savedLoan = loanService.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedLoan);
    }

    @Operation(
            summary = "Loan Updated",
            description = "Update loan Account Type"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Loan updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid loan data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found"
            ),
    })
    @PutMapping("/{id}")
    public ResponseEntity<Loan> update(
            @PathVariable Long id,
            @Valid @RequestBody LoanUpdateRequest request) {

        Loan updatedLoan = loanService.update(id, request);

        return ResponseEntity.ok(updatedLoan);
    }

    @Operation(
            summary = "Delete Loan",
            description = "Deletes loan using their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Loan deleted successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found")
    })
    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        loanService.delete(id);
        return ResponseEntity.noContent().build();

    }


    @Operation(
            summary = "Approve Loan",
            description = "Deletes a accounts using their ID"
    )

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Loan approved successfully"),
            @ApiResponse(
                    responseCode = "204",
                    description = "Account deleted successfully"),
            @ApiResponse(
                    responseCode = "422",
                    description = "Loan is not pending")
    })
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Loan> approve(@PathVariable Long id) {

        Loan loan = loanService.approve(id);

        return ResponseEntity.ok(loan);
    }

    @Operation(summary = "Rejected loan")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Loan rejected successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found"),
            @ApiResponse(
                    responseCode = "422",
                    description = "Loan is not rejected")
    })
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Loan> reject (@PathVariable Long id){
        Loan loan = loanService.reject(id);

        return ResponseEntity.ok(loan);

    }



}