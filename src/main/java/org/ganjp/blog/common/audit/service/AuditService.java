package org.ganjp.blog.common.audit.service;

import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.audit.model.entity.AuditLog;
import org.ganjp.blog.common.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for creating audit log entries
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Log a successful operation
     */
    @Async
    public void logSuccess(
            String httpMethod,
            String endpoint,
            String resultMessage,
            Integer statusCode,
            HttpServletRequest request,
            Long durationMs) {

        try {
            String userId = extractUserIdFromSecurity();
            String username = extractUsernameFromSecurity();
            String requestId = getRequestId(request);

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .username(username)
                    .httpMethod(httpMethod)
                    .endpoint(endpoint)
                    .result(resultMessage)
                    .statusCode(statusCode)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(requestId)
                    .durationMs(durationMs)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {}", httpMethod, endpoint, resultMessage);

        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log a failed operation
     */
    @Async
    public void logFailure(
            String httpMethod,
            String endpoint,
            String resultMessage,
            Integer statusCode,
            String errorMessage,
            HttpServletRequest request,
            Long durationMs) {

        try {
            String userId = extractUserIdFromSecurity();
            String username = extractUsernameFromSecurity();
            String requestId = getRequestId(request);

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .username(username)
                    .httpMethod(httpMethod)
                    .endpoint(endpoint)
                    .result(resultMessage)
                    .statusCode(statusCode)
                    .errorMessage(errorMessage)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(requestId)
                    .durationMs(durationMs)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {}", httpMethod, endpoint, resultMessage);

        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log authentication events with detailed data
     */
    @Async
    public void logAuthenticationEventWithData(
            String httpMethod,
            String endpoint,
            String userId,
            String username,
            String resultMessage,
            Integer statusCode,
            HttpServletRequest request,
            Long durationMs) {

        try {
            String requestId = getRequestId(request);

            if ("Login successful".equals(resultMessage)) {
                log.info("User {} successfully logged in from IP: {}", username, getClientIpAddress(request));
            } else if ("Logout successful".equals(resultMessage)) {
                log.info("User {} logged out from IP: {}", username, getClientIpAddress(request));
            }

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .username(username)
                    .httpMethod(httpMethod)
                    .endpoint(endpoint)
                    .result(resultMessage)
                    .statusCode(statusCode)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(requestId)
                    .durationMs(durationMs)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Authentication audit log created: {} - {} - {}", httpMethod, endpoint, resultMessage);

        } catch (Exception e) {
            log.error("Failed to create authentication audit log", e);
        }
    }

    /**
     * Log authentication events with extracted request data (for async processing)
     */
    @Async
    public void logAuthenticationEvent(AuthenticationAuditData auditData) {
        try {
            if ("Login successful".equals(auditData.resultMessage)) {
                log.info("User {} successfully logged in from IP: {}", auditData.username, auditData.ipAddress);
            } else if ("Logout successful".equals(auditData.resultMessage)) {
                log.info("User {} logged out from IP: {}", auditData.username, auditData.ipAddress);
            }

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(auditData.userId)
                    .username(auditData.username)
                    .httpMethod(auditData.httpMethod)
                    .endpoint(auditData.endpoint)
                    .result(auditData.resultMessage)
                    .statusCode(auditData.statusCode)
                    .ipAddress(auditData.ipAddress)
                    .userAgent(auditData.userAgent)
                    .sessionId(auditData.sessionId)
                    .requestId(auditData.requestId)
                    .durationMs(auditData.durationMs)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Authentication audit log created: {} - {} - {}", auditData.httpMethod, auditData.endpoint, auditData.resultMessage);

        } catch (Exception e) {
            log.error("Failed to create authentication audit log", e);
        }
    }

    /**
     * Data class for authentication audit information
     */
    public static class AuthenticationAuditData {
        public final String httpMethod;
        public final String endpoint;
        public final String userId;
        public final String username;
        public final String resultMessage;
        public final Integer statusCode;
        public final String ipAddress;
        public final String userAgent;
        public final String sessionId;
        public final String requestId;
        public final Long durationMs;

        private AuthenticationAuditData(Builder builder) {
            this.httpMethod = builder.httpMethod;
            this.endpoint = builder.endpoint;
            this.userId = builder.userId;
            this.username = builder.username;
            this.resultMessage = builder.resultMessage;
            this.statusCode = builder.statusCode;
            this.ipAddress = builder.ipAddress;
            this.userAgent = builder.userAgent;
            this.sessionId = builder.sessionId;
            this.requestId = builder.requestId;
            this.durationMs = builder.durationMs;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String httpMethod;
            private String endpoint;
            private String userId;
            private String username;
            private String resultMessage;
            private Integer statusCode;
            private String ipAddress;
            private String userAgent;
            private String sessionId;
            private String requestId;
            private Long durationMs;

            public Builder httpMethod(String httpMethod) { this.httpMethod = httpMethod; return this; }
            public Builder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
            public Builder userId(String userId) { this.userId = userId; return this; }
            public Builder username(String username) { this.username = username; return this; }
            public Builder resultMessage(String resultMessage) { this.resultMessage = resultMessage; return this; }
            public Builder statusCode(Integer statusCode) { this.statusCode = statusCode; return this; }
            public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
            public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
            public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
            public Builder requestId(String requestId) { this.requestId = requestId; return this; }
            public Builder durationMs(Long durationMs) { this.durationMs = durationMs; return this; }

            public AuthenticationAuditData build() {
                return new AuthenticationAuditData(this);
            }
        }
    }

    // Helper methods

    /**
     * Get request ID from various sources
     */
    private String getRequestId(HttpServletRequest request) {
        // Try to get from request attribute first
        String requestId = (String) request.getAttribute("REQUEST_ID");
        if (requestId != null) {
            return requestId;
        }

        // Try to get from MDC
        requestId = org.slf4j.MDC.get("requestId");
        if (requestId != null) {
            return requestId;
        }

        // Generate a new one if none found
        return UUID.randomUUID().toString();
    }

    /**
     * Extract user ID from Spring Security context
     * User ID is stored as a custom claim in the JWT token
     */
    private String extractUserIdFromSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            // Try to extract user ID from JWT token via request
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        return jwtUtils.extractUserId(token);
                    }
                }
            } catch (Exception e) {
                log.debug("Could not extract user ID from JWT token, falling back to username", e);
            }
            
            // Fallback to username if user ID extraction fails
            return authentication.getName();
        }
        return null;
    }

    /**
     * Extract username from Spring Security context
     * Username is stored as the subject in the JWT token
     */
    private String extractUsernameFromSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Get client IP address from HTTP request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Handle multiple IPs in X-Forwarded-For (take the first one)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        // Normalize IPv6 localhost to IPv4 for better readability
        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        
        return ipAddress;
    }

    /**
     * Get user agent from HTTP request
     */
    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * Get session ID from HTTP request
     */
    private String getSessionId(HttpServletRequest request) {
        try {
            return request.getSession(false) != null ? request.getSession().getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clean up old audit logs based on retention policy
     */
    public void cleanupOldAuditLogs(int retentionDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            log.info("Cleaning up audit logs older than {} days (before {})", retentionDays, cutoffDate);
            
            // Perform actual cleanup using repository
            auditLogRepository.deleteOldAuditLogs(cutoffDate);
            log.info("Cleanup completed for audit logs older than {}", cutoffDate);
        } catch (Exception e) {
            log.error("Failed to cleanup old audit logs", e);
            throw e;
        }
    }
}
