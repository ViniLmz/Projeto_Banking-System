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
    private final LoanMapper loanMapper;

    public LoanController(LoanService loanService, LoanMapper loanMapper) {
        this.loanService = loanService;
        this.loanMapper = loanMapper;
    }


    @Operation(
            summary = "List Loans",
            description = "Returns all loans for administrators. Customers can only view their own loans."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loans retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAll() {

        List<LoanResponse> response = loanService.findAll()
                .stream()
                .map(loanMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
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
    public ResponseEntity<LoanResponse> getId(@PathVariable Long id){
      return loanService.findById(id)
              .map(loanMapper::toResponse)
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
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody LoanRequest request) {

        Loan savedLoan = loanService.save(request);

        LoanResponse response = loanMapper.toResponse(savedLoan);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
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
    public ResponseEntity<LoanResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LoanUpdateRequest request) {

        Loan updatedLoan = loanService.update(id, request);

        LoanResponse response = loanMapper.toResponse(updatedLoan);

        return ResponseEntity.ok(response);
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
            description = "Approves a pending loan and adds the loan amount to the associated account balance"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Loan approved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can approve loans"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan or associated account not found"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Loan is not pending"
            )
    })
    @PatchMapping("/{id}/approve")
    public ResponseEntity<LoanResponse> approve(@PathVariable Long id) {

        Loan loan = loanService.approve(id);

        LoanResponse response = loanMapper.toResponse(loan);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Reject Loan",
            description = "Rejects a pending loan. Only administrators can perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Loan rejected successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only administrators can reject loans"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Loan is not pending"
            )
    })
    @PatchMapping("/{id}/reject")
    public ResponseEntity<LoanResponse> reject (@PathVariable Long id){
        Loan loan = loanService.reject(id);

        LoanResponse response = loanMapper.toResponse(loan);

        return ResponseEntity.ok(response);

    }



}