
package com.bank_project.loan;

import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.domain.AccountType;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.audit.AuditLogService;
import com.bank_project.custumer.Custumer;
import com.bank_project.custumer.CustumerRepository;
import com.bank_project.user.AppUser;
import com.bank_project.user.AppUserRepository;
import com.bank_project.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class LoanAuditRollbackTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private CustumerRepository custumerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @MockitoBean
    private AuditLogService auditLogService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRollbackLoanApprovalWhenAuditFails() {
        TestData data = createTestData("approval");
        authenticate(data.admin().getUsername());

        doThrow(new RuntimeException("Audit failure"))
                .when(auditLogService)
                .log(anyString(), anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class,
                () -> loanService.approve(data.loan().getId()));

        Loan loan = loanRepository.findById(data.loan().getId()).orElseThrow();
        Account account = accountRepository.findById(data.account().getId()).orElseThrow();

        assertEquals("PENDING", loan.getStatus());
        assertEquals(0, new BigDecimal("5000.00").compareTo(account.getBalance()));
    }

    @Test
    void shouldRollbackLoanRejectionWhenAuditFails() {
        TestData data = createTestData("rejection");
        authenticate(data.admin().getUsername());

        doThrow(new RuntimeException("Audit failure"))
                .when(auditLogService)
                .log(anyString(), anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class,
                () -> loanService.reject(data.loan().getId()));

        Loan loan = loanRepository.findById(data.loan().getId()).orElseThrow();

        assertEquals("PENDING", loan.getStatus());
    }

    private TestData createTestData(String suffix) {
        String unique = UUID.randomUUID().toString();
        String cpf = unique.replace("-", "").substring(0, 11);

        Custumer customer = new Custumer();
        customer.setName("Test Customer " + suffix);
        customer.setEmail(unique + "@test.com");
        customer.setCpf(cpf);
        customer = custumerRepository.save(customer);

        Account account = new Account(
                null,
                "ACC-" + unique,
                new BigDecimal("5000.00"),
                AccountType.CHECKING,
                AccountStatus.ACTIVE
        );
        account.setCustomer(customer);
        account = accountRepository.save(account);

        Loan loan = new Loan(
                null,
                customer,
                account,
                new BigDecimal("1000.00"),
                new BigDecimal("2.5"),
                10,
                "PENDING",
                LocalDateTime.now()
        );
        loan = loanRepository.save(loan);

        AppUser admin = new AppUser(
                "admin-loan-" + unique,
                "test-password",
                null,
                UserRole.ADMIN
        );
        admin = appUserRepository.save(admin);

        return new TestData(customer, account, loan, admin);
    }

    private void authenticate(String username) {
        var authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private record TestData(
            Custumer customer,
            Account account,
            Loan loan,
            AppUser admin
    ) {}
}
