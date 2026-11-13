
package com.bank_project.loan;

import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.domain.AccountType;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.audit.AuditLog;
import com.bank_project.audit.AuditLogRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LoanAuditIntegrationTest {

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

    @Autowired
    private AuditLogRepository auditLogRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPersistAuditLogWhenLoanIsApproved() {

        TestData data = createTestData("approve");

        authenticate(data.admin().getUsername());

        Loan approvedLoan = loanService.approve(data.loan().getId());

        assertEquals("APPROVED", approvedLoan.getStatus());

        Account updatedAccount = accountRepository
                .findById(data.account().getId())
                .orElseThrow();

        assertEquals(
                new BigDecimal("6000.00"),
                updatedAccount.getBalance()
        );

        AuditLog auditLog = findAuditLog(data.loan().getId());

        assertEquals(data.admin().getId(), auditLog.getActorId());
        assertEquals(data.admin().getUsername(), auditLog.getActorUsername());
        assertEquals("LOAN_APPROVED", auditLog.getAction());
        assertEquals("LOAN", auditLog.getResourceType());
        assertEquals(data.loan().getId().toString(), auditLog.getResourceId());
        assertEquals(
                "Loan approved for customer " + data.customer().getId(),
                auditLog.getDescription()
        );
        assertNotNull(auditLog.getCreatedAt());
    }

    @Test
    void shouldPersistAuditLogWhenLoanIsRejected() {

        TestData data = createTestData("reject");

        authenticate(data.admin().getUsername());

        Loan rejectedLoan = loanService.reject(data.loan().getId());

        assertEquals("REJECTED", rejectedLoan.getStatus());

        AuditLog auditLog = findAuditLog(data.loan().getId());

        assertEquals(data.admin().getId(), auditLog.getActorId());
        assertEquals(data.admin().getUsername(), auditLog.getActorUsername());
        assertEquals("LOAN_REJECTED", auditLog.getAction());
        assertEquals("LOAN", auditLog.getResourceType());
        assertEquals(data.loan().getId().toString(), auditLog.getResourceId());
        assertEquals(
                "Loan rejected for customer " + data.customer().getId(),
                auditLog.getDescription()
        );
        assertNotNull(auditLog.getCreatedAt());
    }

    private TestData createTestData(String suffix) {

        String unique = UUID.randomUUID().toString();

        Custumer customer = new Custumer();
        customer.setName("Test Customer " + suffix);
        customer.setEmail(unique + "@test.com");
        customer.setCpf(unique.substring(0, 11).replace("-", "0"));

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

    private AuditLog findAuditLog(Long loanId) {

        return auditLogRepository.findAll()
                .stream()
                .filter(log -> "LOAN".equals(log.getResourceType()))
                .filter(log -> loanId.toString().equals(log.getResourceId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Audit log not found for loan " + loanId
                ));
    }

    private record TestData(
            Custumer customer,
            Account account,
            Loan loan,
            AppUser admin
    ) {
    }
}
