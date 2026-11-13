
package com.bank_project.audit;

import com.bank_project.user.AppUser;
import com.bank_project.user.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            CurrentUserService currentUserService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void log(
            String action,
            String resourceType,
            String resourceId,
            String description
    ) {
        AppUser currentUser = currentUserService.getCurrentUser();

        saveLog(
                currentUser,
                action,
                resourceType,
                resourceId,
                description
        );
    }

    @Transactional
    public void logForUser(
            AppUser user,
            String action,
            String resourceType,
            String resourceId,
            String description
    ) {
        saveLog(
                user,
                action,
                resourceType,
                resourceId,
                description
        );
    }

    private void saveLog(
            AppUser user,
            String action,
            String resourceType,
            String resourceId,
            String description
    ) {
        AuditLog auditLog = new AuditLog(
                user.getId(),
                user.getUsername(),
                action,
                resourceType,
                resourceId,
                description
        );

        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findWithFilters(
            String actorUsername,
            String action,
            String resourceType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return auditLogRepository.findWithFilters(
                actorUsername,
                action,
                resourceType,
                startDate,
                endDate,
                pageable
        ).map(AuditLogResponse::from);
    }
}
