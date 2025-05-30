package org.ganjp.blog.common.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.audit.model.entity.AuditLog;
import org.ganjp.blog.common.audit.model.enums.AuditAction;
import org.ganjp.blog.common.audit.model.enums.AuditResult;
import org.ganjp.blog.common.audit.repository.AuditLogRepository;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for querying audit logs.
 * Provides methods for retrieving and analyzing audit data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Find audit logs with multiple criteria
     */
    public Page<AuditLog> findAuditLogs(
            String userId,
            AuditAction action,
            AuditResult result,
            String resourceType,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        return auditLogRepository.findByCriteria(
                userId, action, result, resourceType, startTime, endTime, pageable);
    }

    /**
     * Find audit logs for a specific user
     */
    public Page<AuditLog> findUserAuditLogs(String userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (startTime != null && endTime != null) {
            return auditLogRepository.findByUserIdAndTimestampBetween(userId, startTime, endTime, pageable);
        } else {
            return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        }
    }

    /**
     * Find failed operations for a user
     */
    public Page<AuditLog> findFailedOperationsByUser(String userId, Pageable pageable) {
        return auditLogRepository.findFailedOperationsByUser(userId, pageable);
    }

    /**
     * Find audit logs for a specific resource
     */
    public Page<AuditLog> findResourceAuditLogs(String resourceType, String resourceId, Pageable pageable) {
        return auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId, pageable);
    }

    /**
     * Find audit logs by IP address
     */
    public Page<AuditLog> findAuditLogsByIpAddress(String ipAddress, Pageable pageable) {
        return auditLogRepository.findByIpAddressOrderByTimestampDesc(ipAddress, pageable);
    }

    /**
     * Find recent audit logs (last 24 hours)
     */
    public List<AuditLog> findRecentAuditLogs() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return auditLogRepository.findRecentAuditLogs(since);
    }

    /**
     * Get audit log by ID
     */
    public AuditLog findAuditLogById(String id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
    }

    /**
     * Count failed login attempts for a user within specified hours
     */
    public long countFailedLoginAttempts(String userId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.countFailedLoginAttempts(userId, since);
    }

    /**
     * Count failed login attempts from an IP address within specified hours
     */
    public long countFailedLoginAttemptsByIp(String ipAddress, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.countFailedLoginAttemptsByIp(ipAddress, since);
    }

    /**
     * Get audit statistics for dashboard
     */
    public Map<String, Object> getAuditStatistics(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Get total count
        long totalLogs = auditLogRepository.countTotalAuditLogs();
        statistics.put("totalLogs", totalLogs);
        
        // Get statistics by action and result
        List<Object[]> rawStats = auditLogRepository.getAuditStatistics(since);
        
        Map<String, Map<String, Long>> actionResultStats = new HashMap<>();
        long totalSuccessful = 0;
        long totalFailed = 0;
        
        for (Object[] row : rawStats) {
            AuditAction action = (AuditAction) row[0];
            AuditResult result = (AuditResult) row[1];
            Long count = (Long) row[2];
            
            String actionKey = action.name();
            String resultKey = result.name();
            
            actionResultStats.computeIfAbsent(actionKey, k -> new HashMap<>())
                           .put(resultKey, count);
            
            if (result == AuditResult.SUCCESS) {
                totalSuccessful += count;
            } else {
                totalFailed += count;
            }
        }
        
        statistics.put("actionResultStats", actionResultStats);
        statistics.put("totalSuccessful", totalSuccessful);
        statistics.put("totalFailed", totalFailed);
        statistics.put("periodDays", days);
        statistics.put("generatedAt", LocalDateTime.now());
        
        // Calculate success rate
        long total = totalSuccessful + totalFailed;
        double successRate = total > 0 ? (double) totalSuccessful / total * 100 : 0.0;
        statistics.put("successRate", Math.round(successRate * 100.0) / 100.0);
        
        return statistics;
    }

    /**
     * Get count of operations by user and action within specified hours
     */
    public long countOperationsByUserAndAction(String userId, AuditAction action, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.countOperationsByUserAndAction(userId, action, since);
    }

    /**
     * Check if a user has exceeded the operation rate limit
     */
    public boolean hasExceededRateLimit(String userId, AuditAction action, int maxOperations, int hours) {
        long count = countOperationsByUserAndAction(userId, action, hours);
        return count >= maxOperations;
    }

    /**
     * Clean up old audit logs
     */
    @Transactional
    public long cleanupOldAuditLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        
        // Count logs to be deleted
        Page<AuditLog> logsToDelete = auditLogRepository.findByTimestampBetween(
                LocalDateTime.MIN, cutoffDate, Pageable.unpaged());
        long countToDelete = logsToDelete.getTotalElements();
        
        // Delete old logs
        auditLogRepository.deleteOldAuditLogs(cutoffDate);
        
        log.info("Cleaned up {} audit logs older than {} days", countToDelete, retentionDays);
        return countToDelete;
    }

    /**
     * Get audit logs for security analysis (suspicious activities)
     */
    public Map<String, Object> getSecurityAnalysis(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        Map<String, Object> analysis = new HashMap<>();
        
        // Find IPs with multiple failed login attempts
        // This would require a custom query, simplified for now
        analysis.put("analysisTimestamp", LocalDateTime.now());
        analysis.put("analysisPeriodHours", hours);
        
        return analysis;
    }

    /**
     * Export audit logs for compliance (simplified version)
     */
    public List<AuditLog> exportAuditLogs(LocalDateTime startTime, LocalDateTime endTime) {
        // For large datasets, this should be paginated or streamed
        return auditLogRepository.findByTimestampBetween(startTime, endTime, Pageable.unpaged()).getContent();
    }
}
