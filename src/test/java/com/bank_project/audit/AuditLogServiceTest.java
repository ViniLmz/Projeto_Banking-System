
package com.bank_project.audit;

import com.bank_project.user.AppUser;
import com.bank_project.user.CurrentUserService;
import com.bank_project.user.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void shouldSaveAuditLogWithAuthenticatedUserInformation() {

        AppUser user = new AppUser(
                "vinicius",
                "encoded-password",
                null,
                UserRole.CUSTOMER
        );

        user.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(user);

        auditLogService.log(
                "TRANSFER",
                "TRANSFER",
                "25",
                "Transfer completed"
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();

        assertEquals(1L, savedLog.getActorId());
        assertEquals("vinicius", savedLog.getActorUsername());
        assertEquals("TRANSFER", savedLog.getAction());
        assertEquals("TRANSFER", savedLog.getResourceType());
        assertEquals("25", savedLog.getResourceId());
        assertEquals("Transfer completed", savedLog.getDescription());
        assertNotNull(savedLog.getCreatedAt());

        verify(currentUserService).getCurrentUser();
    }
}
