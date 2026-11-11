
package com.bank_project.account.register;

import com.bank_project.account.domain.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 120)
        String name,

        @NotBlank(message = "CPF is required")
        @ValidCpf
        String cpf,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Account type is required")
        AccountType accountType,

        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 50)
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72)
        String password

) {}
