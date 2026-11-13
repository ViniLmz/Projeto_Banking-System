
package com.bank_project.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(
        name = "Audit Trail",
        description = "Audit log consultation and filtering"
)
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Find audit logs",
            description = "Returns audit logs with optional filters and pagination. Only ADMIN users can access this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Audit logs retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have ADMIN permission"
            )
    })
    public ResponseEntity<Page<AuditLogResponse>> findWithFilters(

            @Parameter(
                    description = "Username of the user who performed the action",
                    example = "vinicius",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false)
            String actorUsername,

            @Parameter(
                    description = "Action performed, such as ACCOUNT_DEPOSIT or LOAN_APPROVED",
                    example = "ACCOUNT_DEPOSIT",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false)
            String action,

            @Parameter(
                    description = "Resource type affected by the action",
                    example = "ACCOUNT",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false)
            String resourceType,

            @Parameter(
                    description = "Start date and time of the search period",
                    example = "2026-11-01T00:00:00",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @Parameter(
                    description = "End date and time of the search period",
                    example = "2026-11-13T23:59:59",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,

            @Parameter(description = "Page number (starts at 0)")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Number of records per page")
            @RequestParam(defaultValue = "10")
            int size

    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<AuditLogResponse> result = auditLogService.findWithFilters(
                actorUsername,
                action,
                resourceType,
                startDate,
                endDate,
                pageable
        );

        return ResponseEntity.ok(result);
    }
}
