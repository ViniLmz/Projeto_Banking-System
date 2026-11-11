
package com.bank_project.account.register;

import com.bank_project.account.aplication.AccountNumberGenerator;
import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.exception.CpfAlreadyExistsException;
import com.bank_project.exception.EmailAlreadyExistsException;
import com.bank_project.exception.UsernameAlreadyExistsException;
import com.bank_project.user.AppUser;
import com.bank_project.user.AppUserRepository;
import com.bank_project.user.UserRole;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RegistrationService {

    private final AccountRepository accountRepository;
    private final CustumerRepository custumerRepository;
    private final AppUserRepository appUserRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            AccountRepository accountRepository,
            CustumerRepository custumerRepository,
            AppUserRepository appUserRepository,
            AccountNumberGenerator accountNumberGenerator,
            PasswordEncoder passwordEncoder
    ) {
        this.accountRepository = accountRepository;
        this.custumerRepository = custumerRepository;
        this.appUserRepository = appUserRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistrationResponse save(RegistrationRequest request) {

        if (custumerRepository.existsByCpf(request.cpf())) {
            throw new CpfAlreadyExistsException();
        }

        if (custumerRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        if (appUserRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException();
        }

        Custumer customer = new Custumer(
                null,
                request.name(),
                request.cpf(),
                request.email()
        );

        customer = custumerRepository.save(customer);

        String accountNumber = accountNumberGenerator.generate();

        Account account = new Account(
                null,
                accountNumber,
                BigDecimal.ZERO,
                request.accountType(),
                AccountStatus.ACTIVE
        );

        account.setCustomer(customer);

        accountRepository.save(account);

        String encryptedPassword =
                passwordEncoder.encode(request.password());

        AppUser appUser = new AppUser(
                request.username(),
                encryptedPassword,
                customer,
                UserRole.CUSTOMER
        );

        appUserRepository.save(appUser);

        return new RegistrationResponse(
                "Registration completed successfully",
                appUser.getUsername(),
                accountNumber
        );
    }
}
