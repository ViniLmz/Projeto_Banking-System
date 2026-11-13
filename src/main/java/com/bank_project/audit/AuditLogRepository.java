
package com.bank_project.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:actorUsername IS NULL OR
                   LOWER(a.actorUsername) LIKE LOWER(CONCAT('%', :actorUsername, '%')))
            AND (:action IS NULL OR a.action = :action)
            AND (:resourceType IS NULL OR a.resourceType = :resourceType)
            AND (:startDate IS NULL OR a.createdAt >= :startDate)
            AND (:endDate IS NULL OR a.createdAt <= :endDate)
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findWithFilters(
            @Param("actorUsername") String actorUsername,
            @Param("action") String action,
            @Param("resourceType") String resourceType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
