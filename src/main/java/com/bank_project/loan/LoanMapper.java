package com.bank_project.loan;

import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getCustomer().getId(),
                loan.getAccount().getId(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getInstallments(),
                loan.getStatus(),
                loan.getCreatedAt()
        );
    }



}