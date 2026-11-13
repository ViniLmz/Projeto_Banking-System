
package com.bank_project.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long actorId,
        String actorUsername,
        String action,
        String resourceType,
        String resourceId,
        String description,
        LocalDateTime createdAt
) {

    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActorId(),
                auditLog.getActorUsername(),
                auditLog.getAction(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getDescription(),
                auditLog.getCreatedAt()
        );
    }
}
