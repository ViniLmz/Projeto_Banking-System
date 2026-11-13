
package com.bank_project.ledger;

import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.domain.AccountType;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.audit.AuditLog;
import com.bank_project.audit.AuditLogRepository;
import com.bank_project.transfer.TransferRepository;
import com.bank_project.transfer.TransferRequest;
import com.bank_project.transfer.TransferService;
import com.bank_project.user.AppUser;
import com.bank_project.user.AppUserRepository;
import com.bank_project.user.UserRole;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class TransferLedgerRollbackTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @MockitoBean
    private LedgerService ledgerService;

    @Test
    void shouldRollbackTransferWhenLedgerFails() {

        String uniqueNumber = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        Account source = accountRepository.save(new Account(
                null,
                "9" + uniqueNumber,
                new BigDecimal("1000.00"),
                AccountType.CHECKING,
                AccountStatus.ACTIVE
        ));

        Account target = accountRepository.save(new Account(
                null,
                "8" + uniqueNumber,
                new BigDecimal("500.00"),
                AccountType.CHECKING,
                AccountStatus.ACTIVE
        ));

        BigDecimal sourceBalanceBefore = source.getBalance();
        BigDecimal targetBalanceBefore = target.getBalance();

        String idempotencyKey = UUID.randomUUID().toString();

        TransferRequest request = new TransferRequest(
                source.getId(),
                target.getId(),
                new BigDecimal("200.00")
        );

        doThrow(new RuntimeException("Ledger failure"))
                .when(ledgerService).recordTransfer(any());

        assertThatThrownBy(() ->
                transferService.transfer(idempotencyKey, request)
        ).isInstanceOf(RuntimeException.class);

        Account sourceAfter = accountRepository.findById(source.getId())
                .orElseThrow();

        Account targetAfter = accountRepository.findById(target.getId())
                .orElseThrow();

        assertThat(sourceAfter.getBalance())
                .isEqualByComparingTo(sourceBalanceBefore);

        assertThat(targetAfter.getBalance())
                .isEqualByComparingTo(targetBalanceBefore);

        assertThat(transferRepository.findByIdempotencyKey(idempotencyKey))
                .isEmpty();
    }

    @Test
    void shouldPersistAuditLogWhenTransferSucceeds() {

        String uniqueId = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        String username = "audit_admin_" + uniqueId;

        AppUser admin = appUserRepository.save(
                new AppUser(username, "encoded-password", null, UserRole.ADMIN)
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        try {
            Account source = accountRepository.save(new Account(
                    null,
                    "7" + uniqueId,
                    new BigDecimal("1000.00"),
                    AccountType.CHECKING,
                    AccountStatus.ACTIVE
            ));

            Account target = accountRepository.save(new Account(
                    null,
                    "6" + uniqueId,
                    new BigDecimal("500.00"),
                    AccountType.CHECKING,
                    AccountStatus.ACTIVE
            ));

            String idempotencyKey = UUID.randomUUID().toString();

            TransferRequest request = new TransferRequest(
                    source.getId(),
                    target.getId(),
                    new BigDecimal("200.00")
            );

            var transfer = transferService.transfer(idempotencyKey, request);

            List<AuditLog> logs = auditLogRepository.findAll();

            AuditLog auditLog = logs.stream()
                    .filter(log -> log.getResourceId().equals(transfer.getId().toString()))
                    .filter(log -> log.getAction().equals("TRANSFER"))
                    .findFirst()
                    .orElseThrow();

            assertThat(auditLog.getActorId()).isEqualTo(admin.getId());
            assertThat(auditLog.getActorUsername()).isEqualTo(username);
            assertThat(auditLog.getAction()).isEqualTo("TRANSFER");
            assertThat(auditLog.getResourceType()).isEqualTo("TRANSFER");
            assertThat(auditLog.getResourceId()).isEqualTo(transfer.getId().toString());
            assertThat(auditLog.getDescription())
                    .contains(source.getAccountNumber(), target.getAccountNumber());
            assertThat(auditLog.getCreatedAt()).isNotNull();

        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
