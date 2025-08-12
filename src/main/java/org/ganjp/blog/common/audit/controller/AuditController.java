package org.ganjp.blog.common.audit.controller;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.common.audit.model.entity.AuditLog;
import org.ganjp.blog.common.audit.service.AuditQueryService;
import org.ganjp.blog.common.model.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for audit log management and queries.
 * Provides endpoints for administrators to view and analyze audit logs.
 */
@RestController
@RequestMapping("/v1/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN')")
public class AuditController {

    private final AuditQueryService auditQueryService;

    /**
     * Get audit logs with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(required = false) String resultPattern,
            @RequestParam(required = false) String endpointPattern,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        // Convert date parameters to datetime if provided
        LocalDateTime effectiveStartTime = startTime;
        LocalDateTime effectiveEndTime = endTime;
        
        if (startDate != null && effectiveStartTime == null) {
            effectiveStartTime = startDate.atStartOfDay(); // 00:00:00
        }
        
        if (endDate != null && effectiveEndTime == null) {
            effectiveEndTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999999999
        }

        Page<AuditLog> auditLogs = auditQueryService.findAuditLogs(
                userId, httpMethod, resultPattern, endpointPattern, effectiveStartTime, effectiveEndTime, pageable);

        return ResponseEntity.ok(ApiResponse.success(auditLogs, "Audit logs retrieved successfully"));
    }

    /**
     * Get audit logs for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getUserAuditLogs(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Pageable pageable) {

        Page<AuditLog> auditLogs = auditQueryService.findUserAuditLogs(userId, startTime, endTime, pageable);
        return ResponseEntity.ok(ApiResponse.success(auditLogs, "User audit logs retrieved successfully"));
    }

    /**
     * Get failed operations for a specific user
     */
    @GetMapping("/user/{userId}/failures")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getUserFailedOperations(
            @PathVariable String userId,
            Pageable pageable) {

        Page<AuditLog> failedOperations = auditQueryService.findFailedOperationsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(failedOperations, "Failed operations retrieved successfully"));
    }

    /**
     * Get audit logs for a specific resource
     */
    @GetMapping("/resource/{resourceType}/{resourceId}")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getResourceAuditLogs(
            @PathVariable String resourceType,
            @PathVariable String resourceId,
            Pageable pageable) {

        // Use endpoint pattern to find logs for specific resource
        String endpointPattern = "/" + resourceType + "/" + resourceId;
        Page<AuditLog> auditLogs = auditQueryService.findAuditLogs(
                null, null, null, endpointPattern, null, null, pageable);
        return ResponseEntity.ok(ApiResponse.success(auditLogs, "Resource audit logs retrieved successfully"));
    }

    /**
     * Get audit logs by IP address
     */
    @GetMapping("/ip/{ipAddress}")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogsByIp(
            @PathVariable String ipAddress,
            Pageable pageable) {

        Page<AuditLog> auditLogs = auditQueryService.findAuditLogsByIpAddress(ipAddress, pageable);
        return ResponseEntity.ok(ApiResponse.success(auditLogs, "IP audit logs retrieved successfully"));
    }

    /**
     * Get recent audit logs (last 24 hours)
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getRecentAuditLogs() {
        List<AuditLog> recentLogs = auditQueryService.findRecentAuditLogs();
        return ResponseEntity.ok(ApiResponse.success(recentLogs, "Recent audit logs retrieved successfully"));
    }

    /**
     * Get audit statistics for dashboard
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditStatistics(
            @RequestParam(defaultValue = "7") int days) {

        Map<String, Object> statistics = auditQueryService.getAuditStatistics(days);
        return ResponseEntity.ok(ApiResponse.success(statistics, "Audit statistics retrieved successfully"));
    }

    /**
     * Get failed login attempts count for a user
     */
    @GetMapping("/failed-logins/user/{userId}")
    public ResponseEntity<ApiResponse<Long>> getFailedLoginAttempts(
            @PathVariable String userId,
            @RequestParam(defaultValue = "24") int hours) {

        long count = auditQueryService.countFailedLoginAttempts(userId, hours);
        return ResponseEntity.ok(ApiResponse.success(count, "Failed login attempts count retrieved successfully"));
    }

    /**
     * Get failed login attempts count for an IP address
     */
    @GetMapping("/failed-logins/ip/{ipAddress}")
    public ResponseEntity<ApiResponse<Long>> getFailedLoginAttemptsByIp(
            @PathVariable String ipAddress,
            @RequestParam(defaultValue = "24") int hours) {

        long count = auditQueryService.countFailedLoginAttemptsByIp(ipAddress, hours);
        return ResponseEntity.ok(ApiResponse.success(count, "Failed login attempts count retrieved successfully"));
    }

    /**
     * Delete old audit logs (cleanup)
     * Only super admins can perform this operation
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> cleanupOldAuditLogs(
            @RequestParam(defaultValue = "90") int retentionDays) {

        long deletedCount = auditQueryService.cleanupOldAuditLogs(retentionDays);
        String message = String.format("Cleanup completed. %d audit logs older than %d days were deleted.", 
                                     deletedCount, retentionDays);
        
        return ResponseEntity.ok(ApiResponse.success(message, "Audit logs cleanup completed"));
    }

    /**
     * Get audit log by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLog>> getAuditLogById(@PathVariable String id) {
        AuditLog auditLog = auditQueryService.findAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success(auditLog, "Audit log retrieved successfully"));
    }
}
