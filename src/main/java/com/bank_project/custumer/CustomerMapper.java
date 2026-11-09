package com.bank_project.custumer;

import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Custumer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail()
        );
    }

    public Custumer toEntity(CustomerRequest request) {
        return new Custumer(
                null,
                request.name(),
                request.cpf(),
                request.email()
        );
    }
}