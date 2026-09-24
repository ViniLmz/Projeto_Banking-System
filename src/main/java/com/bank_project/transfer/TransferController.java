package com.bank_project.transfer;

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

@PostMapping
public ResponseEntity<String> transfer(@Valid @RequestBody TransferRequest request) {
    transferService.transfer(request);

    return ResponseEntity.ok("Transfer successfully made");
}

    @GetMapping
    public ResponseEntity<List<Transfer>> findAll() {
        return ResponseEntity.ok(transferService.findAll());
}




}
