package com.bank_project.account.http;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.bank_project.account.domain.Account;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.AccountNumberAlreadyExistsException;
import com.bank_project.exception.BlockedAccountException;
import com.bank_project.exception.InsufficientBalanceException;
import com.bank_project.exception.ResourceNotFoundException;
import com.bank_project.user.CurrentUserService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public record   TransactionRequest (@NotNull(message = "The value can't be 0")
                                  @Positive(message = "The value has to be bigger than 0")
                                  BigDecimal amount){

}

