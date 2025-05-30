package org.ganjp.blog.common.audit.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.common.audit.model.enums.AuditAction;
import org.ganjp.blog.common.audit.model.enums.AuditResult;

import java.time.LocalDateTime;

/**
 * Entity representing audit logs for API operations.
 * Tracks all non-GET API calls for security and compliance purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_result", columnList = "result"),
    @Index(name = "idx_audit_endpoint", columnList = "endpoint"),
    @Index(name = "idx_audit_user_timestamp", columnList = "user_id, timestamp"),
    @Index(name = "idx_audit_action_timestamp", columnList = "action, timestamp")
})
public class AuditLog {

    /**
     * Unique identifier for the audit log entry
     */
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    /**
     * ID of the user who performed the action (null for anonymous actions)
     */
    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;

    /**
     * Username of the user who performed the action (for quick reference)
     */
    @Column(name = "username", length = 30)
    private String username;

    /**
     * HTTP method of the request (POST, PUT, PATCH, DELETE, etc.)
     */
    @Column(name = "http_method", length = 10, nullable = false)
    private String httpMethod;

    /**
     * API endpoint that was called
     */
    @Column(name = "endpoint", length = 255, nullable = false)
    private String endpoint;

    /**
     * Type of action performed
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    /**
     * Resource type being operated on (User, Role, etc.)
     */
    @Column(name = "resource_type", length = 20)
    private String resourceType;

    /**
     * ID of the resource being operated on
     */
    @Column(name = "resource_id", length = 36)
    private String resourceId;

    /**
     * Request payload (limited size for security)
     */
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    /**
     * Response data or summary
     */
    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    /**
     * Result of the operation
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private AuditResult result;

    /**
     * HTTP status code of the response
     */
    @Column(name = "status_code")
    private Integer statusCode;

    /**
     * Error message if the operation failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * IP address from which the request originated
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent string from the request
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Session ID if available
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * Duration of the operation in milliseconds
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * Additional metadata in JSON format
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    /**
     * Timestamp when the action was performed
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Pre-persist hook to set ID and timestamp
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
