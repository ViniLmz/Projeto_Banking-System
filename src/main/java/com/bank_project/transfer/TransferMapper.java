package com.bank_project.transfer;

import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

    public TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccount().getId(),
                transfer.getTargetAccount().getId(),
                transfer.getAmount(),
                transfer.getDate(),
                transfer.getStatus()
        );
    }
}