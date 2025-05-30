package org.ganjp.blog.common.audit.repository;

import org.ganjp.blog.common.audit.model.entity.AuditLog;
import org.ganjp.blog.common.audit.model.enums.AuditAction;
import org.ganjp.blog.common.audit.model.enums.AuditResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity.
 * Provides data access methods for audit logs.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    /**
     * Find audit logs by user ID
     */
    Page<AuditLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    /**
     * Find audit logs by user ID within a time range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserIdAndTimestampBetween(
            @Param("userId") String userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * Find audit logs by action type
     */
    Page<AuditLog> findByActionOrderByTimestampDesc(AuditAction action, Pageable pageable);

    /**
     * Find audit logs by result
     */
    Page<AuditLog> findByResultOrderByTimestampDesc(AuditResult result, Pageable pageable);

    /**
     * Find failed operations for a specific user
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.result IN ('FAILURE', 'ERROR', 'DENIED', 'AUTHENTICATION_FAILED') ORDER BY a.timestamp DESC")
    Page<AuditLog> findFailedOperationsByUser(@Param("userId") String userId, Pageable pageable);

    /**
     * Find audit logs by resource type and ID
     */
    @Query("SELECT a FROM AuditLog a WHERE a.resourceType = :resourceType AND a.resourceId = :resourceId ORDER BY a.timestamp DESC")
    Page<AuditLog> findByResourceTypeAndResourceId(
            @Param("resourceType") String resourceType,
            @Param("resourceId") String resourceId,
            Pageable pageable);

    /**
     * Find audit logs within a time range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * Find audit logs by IP address
     */
    Page<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress, Pageable pageable);

    /**
     * Count failed login attempts for a user within a time period
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.action = 'LOGIN' AND a.result = 'AUTHENTICATION_FAILED' AND a.timestamp >= :since")
    long countFailedLoginAttempts(@Param("userId") String userId, @Param("since") LocalDateTime since);

    /**
     * Count failed login attempts from an IP address within a time period
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.action = 'LOGIN' AND a.result = 'AUTHENTICATION_FAILED' AND a.timestamp >= :since")
    long countFailedLoginAttemptsByIp(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    /**
     * Find recent audit logs (last 24 hours)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentAuditLogs(@Param("since") LocalDateTime since);

    /**
     * Count operations by user and action within a time period
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.action = :action AND a.timestamp >= :since")
    long countOperationsByUserAndAction(
            @Param("userId") String userId,
            @Param("action") AuditAction action,
            @Param("since") LocalDateTime since);

    /**
     * Find audit logs by multiple criteria
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:result IS NULL OR a.result = :result) AND " +
           "(:resourceType IS NULL OR a.resourceType = :resourceType) AND " +
           "(:startTime IS NULL OR a.timestamp >= :startTime) AND " +
           "(:endTime IS NULL OR a.timestamp <= :endTime) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByCriteria(
            @Param("userId") String userId,
            @Param("action") AuditAction action,
            @Param("result") AuditResult result,
            @Param("resourceType") String resourceType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * Delete audit logs older than specified date (for cleanup)
     */
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    void deleteOldAuditLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count total audit logs
     */
    @Query("SELECT COUNT(a) FROM AuditLog a")
    long countTotalAuditLogs();

    /**
     * Get audit log statistics for dashboard
     */
    @Query("SELECT a.action, a.result, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since GROUP BY a.action, a.result")
    List<Object[]> getAuditStatistics(@Param("since") LocalDateTime since);
}
