
package com.bank_project.account.register;

import com.bank_project.account.aplication.AccountNumberGenerator;
import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.domain.AccountType;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.user.AppUser;
import com.bank_project.user.AppUserRepository;
import com.bank_project.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustumerRepository custumerRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    private RegistrationRequest request;

    @BeforeEach
    void setUp() {
        request = new RegistrationRequest(
                "João Silva",
                "12345678900",
                "joao@email.com",
                AccountType.CHECKING,
                "joao123",
                "MinhaSenha123!"
        );
    }

    @Test
    void shouldRegisterCustomerSuccessfully() {

        when(custumerRepository.save(any(Custumer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(accountNumberGenerator.generate())
                .thenReturn("5839201746");

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encryptedPassword");

        RegistrationResponse response =
                registrationService.save(request);

        assertNotNull(response);
        assertEquals(
                "Registration completed successfully",
                response.message()
        );
        assertEquals("joao123", response.username());
        assertEquals("5839201746", response.accountNumber());

        ArgumentCaptor<Account> accountCaptor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(accountCaptor.capture());

        Account savedAccount = accountCaptor.getValue();

        assertEquals("5839201746", savedAccount.getAccountNumber());
        assertEquals(
                java.math.BigDecimal.ZERO,
                savedAccount.getBalance()
        );
        assertEquals(AccountType.CHECKING, savedAccount.getAccountType());
        assertEquals(AccountStatus.ACTIVE, savedAccount.getStatus());
        assertNotNull(savedAccount.getCustomer());

        ArgumentCaptor<AppUser> userCaptor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertEquals("joao123", savedUser.getUsername());
        assertEquals("encryptedPassword", savedUser.getPassword());
        assertEquals(UserRole.CUSTOMER, savedUser.getRole());
        assertNotNull(savedUser.getCustomer());

        verify(passwordEncoder).encode(request.password());
        verify(custumerRepository).save(any(Custumer.class));
        verify(accountNumberGenerator).generate();
    }

    @Test
    void shouldThrowExceptionWhenCpfAlreadyExists() {

        when(custumerRepository.existsByCpf(request.cpf()))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.save(request)
        );

        verify(custumerRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        when(custumerRepository.existsByEmail(request.email()))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.save(request)
        );

        verify(custumerRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {

        when(appUserRepository.existsByUsername(request.username()))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.save(request)
        );

        verify(custumerRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
        verify(appUserRepository, never()).save(any());
    }
}
