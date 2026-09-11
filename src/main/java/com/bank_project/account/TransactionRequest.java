package com.bank_project.account;

import java.math.BigDecimal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record TransactionRequest (@NotNull(message = "The value can't be 0")
                                  @Positive(message = "The value has to be bigger than 0")
                                  BigDecimal amount){}

