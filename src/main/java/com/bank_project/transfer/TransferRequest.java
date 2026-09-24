    package com.bank_project.transfer;

    import java.math.BigDecimal;

    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Positive;

    public record TransferRequest(
            @NotNull(message = "Source account can't be null")
            Long sourceAccountId,

            @NotNull(message = "Target account can't be null")
            Long targetAccountId,

            @NotNull(message = "The value can't be null")
            @Positive(message = "The value has to be bigger than 0")
            BigDecimal amount){}