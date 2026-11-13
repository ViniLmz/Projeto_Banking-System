
package com.bank_project.ledger;

import com.bank_project.account.domain.Account;
import com.bank_project.account.domain.AccountStatus;
import com.bank_project.account.domain.AccountType;
import com.bank_project.account.infra.AccountRepository;
import com.bank_project.audit.AuditLogService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class AuditTrailRollbackTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @MockitoBean
    private AuditLogService auditLogService;

    @Test
    void shouldRollbackTransferAndLedgerWhenAuditFails() {

        String uniqueId = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        String username = "audit_rollback_" + uniqueId;

        appUserRepository.save(
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
                    "5" + uniqueId,
                    new BigDecimal("1000.00"),
                    AccountType.CHECKING,
                    AccountStatus.ACTIVE
            ));

            Account target = accountRepository.save(new Account(
                    null,
                    "4" + uniqueId,
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

            doThrow(new RuntimeException("Audit failure"))
                    .when(auditLogService)
                    .log(anyString(), anyString(), anyString(), anyString());

            assertThatThrownBy(() ->
                    transferService.transfer(idempotencyKey, request)
            ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Audit failure");

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

            assertThat(ledgerEntryRepository
                    .findAllByAccountIdOrderByCreatedAtDesc(source.getId()))
                    .isEmpty();

            assertThat(ledgerEntryRepository
                    .findAllByAccountIdOrderByCreatedAtDesc(target.getId()))
                    .isEmpty();

        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
