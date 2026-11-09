package com.bank_project.custumer;

import jakarta.validation.constraints.NotNull;

public record CustomerRequest (

        @NotNull(message = "The name cannot be null")
        String name,

        @NotNull(message = "The email cannot be null")
        String email,

        @NotNull(message = "The CPF cannot be null")
        String cpf){


}


