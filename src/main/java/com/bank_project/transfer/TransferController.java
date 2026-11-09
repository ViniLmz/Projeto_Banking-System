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
    private final TransferMapper transferMapper;

    public TransferController(TransferService transferService, TransferMapper transferMapper) {
        this.transferService = transferService;
        this.transferMapper = transferMapper;
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
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        Transfer transfer = transferService.transfer(idempotencyKey, request);

        TransferResponse response = transferMapper.toResponse(transfer);

        return ResponseEntity.ok(response);
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
    public ResponseEntity<List<TransferResponse>> findAll() {
            List<TransferResponse> transfers =
                        transferService.findAll()
                        .stream()
                        .map(transferMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(transfers);
}




}
