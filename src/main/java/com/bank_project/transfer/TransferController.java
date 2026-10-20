package com.bank_project.transfer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController


@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }


    @Operation(summary = "Transfer money between accounts")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer successfully made"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transfer request"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"),
            @ApiResponse(
                    responseCode = "422",
                    description = "Insufficient balance or account cannot make the transfer")
    })
@PostMapping
public ResponseEntity<String> transfer(@Valid @RequestBody TransferRequest request) {
    transferService.transfer(request);

    return ResponseEntity.ok("Transfer successfully made");
}


    @Operation(
            summary = "List of Transfer ",
            description = "Returns a list of transfer made, registered in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account found successfully"
            ),
    })
    @GetMapping
    public ResponseEntity<List<Transfer>> findAll() {
        return ResponseEntity.ok(transferService.findAll());
}




}
