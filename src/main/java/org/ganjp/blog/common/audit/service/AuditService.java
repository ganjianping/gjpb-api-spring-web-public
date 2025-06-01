package org.ganjp.blog.common.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.audit.model.entity.AuditLog;
import org.ganjp.blog.common.audit.model.enums.AuditAction;
import org.ganjp.blog.common.audit.model.enums.AuditResult;
import org.ganjp.blog.common.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * Service for managing audit logs.
 * Handles creation and storage of audit log entries.
 */
@Slf4j
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, 
                       @Qualifier("auditObjectMapper") ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    private static final int MAX_DATA_LENGTH = 10000; // Limit data size to prevent database issues

    /**
     * Log an audit event asynchronously
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuditEvent(
            String userId,
            String username,
            String httpMethod,
            String endpoint,
            AuditAction action,
            String resourceType,
            String resourceId,
            Object requestData,
            Object responseData,
            AuditResult result,
            Integer statusCode,
            String errorMessage,
            String ipAddress,
            String userAgent,
            String sessionId,
            Long durationMs,
            Map<String, Object> metadata) {

        try {
            // Try to get request ID from MDC
            String requestId = org.slf4j.MDC.get("requestId");
            
            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .username(username)
                    .httpMethod(httpMethod)
                    .endpoint(endpoint)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .requestData(truncateAndSerialize(requestData))
                    .responseData(truncateAndSerialize(responseData))
                    .result(result)
                    .statusCode(statusCode)
                    .errorMessage(truncateString(errorMessage, 1000))
                    .ipAddress(ipAddress)
                    .userAgent(truncateString(userAgent, 500))
                    .sessionId(sessionId)
                    .requestId(requestId)
                    .durationMs(durationMs)
                    .metadata(serializeMetadata(metadata))
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {}", action, endpoint, result);

        } catch (Exception e) {
            log.error("Failed to create audit log for action: {} on endpoint: {}", action, endpoint, e);
        }
    }

    /**
     * Log an audit event with minimal information
     */
    @Async
    public void logAuditEvent(
            String httpMethod,
            String endpoint,
            AuditResult result,
            Integer statusCode,
            HttpServletRequest request) {

        String userId = extractUserIdFromSecurity();
        String username = extractUsernameFromSecurity();
        AuditAction action = AuditAction.fromHttpMethodAndEndpoint(httpMethod, endpoint);
        String resourceType = extractResourceTypeFromEndpoint(endpoint);
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String sessionId = request.getSession().getId();

        logAuditEvent(
                userId,
                username,
                httpMethod,
                endpoint,
                action,
                resourceType,
                null, // resourceId
                null, // requestData
                null, // responseData
                result,
                statusCode,
                null, // errorMessage
                ipAddress,
                userAgent,
                sessionId,
                null, // durationMs
                null  // metadata
        );
    }

    /**
     * Log a successful operation
     */
    @Async
    public void logSuccess(
            String httpMethod,
            String endpoint,
            Object requestData,
            Object responseData,
            Integer statusCode,
            HttpServletRequest request,
            Long durationMs) {

        String userId = extractUserIdFromSecurity();
        String username = extractUsernameFromSecurity();
        AuditAction action = AuditAction.fromHttpMethodAndEndpoint(httpMethod, endpoint);
        String resourceType = extractResourceTypeFromEndpoint(endpoint);
        String resourceId = extractResourceIdFromEndpoint(endpoint);

        // Get request ID if available
        String requestId = (String) request.getAttribute(org.ganjp.blog.common.filter.RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        
        // Create metadata with request ID if not available in request attributes
        Map<String, Object> metadata = null;
        if (requestId == null) {
            metadata = new HashMap<>();
            metadata.put("generatedRequestId", org.ganjp.blog.common.util.RequestUtils.getCurrentRequestId());
        }
        
        logAuditEvent(
                userId,
                username,
                httpMethod,
                endpoint,
                action,
                resourceType,
                resourceId,
                requestData,
                responseData,
                AuditResult.SUCCESS,
                statusCode,
                null,
                getClientIpAddress(request),
                request.getHeader("User-Agent"),
                request.getSession() != null ? request.getSession().getId() : null,
                durationMs,
                metadata
        );
    }

    /**
     * Log a failed operation
     */
    @Async
    public void logFailure(
            String httpMethod,
            String endpoint,
            Object requestData,
            String errorMessage,
            Integer statusCode,
            HttpServletRequest request,
            Long durationMs) {

        String userId = extractUserIdFromSecurity();
        String username = extractUsernameFromSecurity();
        AuditAction action = AuditAction.fromHttpMethodAndEndpoint(httpMethod, endpoint);
        String resourceType = extractResourceTypeFromEndpoint(endpoint);
        String resourceId = extractResourceIdFromEndpoint(endpoint);
        AuditResult result = AuditResult.fromStatusCode(statusCode);

        logAuditEvent(
                userId,
                username,
                httpMethod,
                endpoint,
                action,
                resourceType,
                resourceId,
                requestData,
                null,
                result,
                statusCode,
                errorMessage,
                getClientIpAddress(request),
                request.getHeader("User-Agent"),
                request.getSession().getId(),
                durationMs,
                null
        );
    }

    /**
     * Log authentication events (login, logout, etc.) with request/response data
     */
    @Async
    public void logAuthenticationEventWithData(
            AuditAction action,
            String username,
            AuditResult result,
            String errorMessage,
            String httpMethod,
            String requestURI,
            String clientIpAddress,
            String userAgent,
            String sessionId,
            Long startTimeMs,
            Object requestData,
            Object responseData,
            String resourceId) {

        String userId = null;
        if (result == AuditResult.SUCCESS && action == AuditAction.LOGIN) {
            userId = extractUserIdFromSecurity();
        }

        // Calculate duration from request start time if available
        Long durationMs = null;
        if (startTimeMs != null) {
            durationMs = System.currentTimeMillis() - startTimeMs;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("authenticationType", action.name());
        if (errorMessage != null) {
            metadata.put("failureReason", errorMessage);
        }

        logAuditEvent(
                userId,
                username,
                httpMethod,
                requestURI,
                action,
                "Authentication",
                resourceId,
                requestData,
                responseData,
                result,
                result == AuditResult.SUCCESS ? 200 : 401,
                errorMessage,
                clientIpAddress,
                userAgent,
                sessionId,
                durationMs,
                metadata
        );
    }

    /**
     * Log authentication events (login, logout, etc.)
     */
    @Async
    public void logAuthenticationEvent(
            AuditAction action,
            String username,
            AuditResult result,
            String errorMessage,
            HttpServletRequest request) {

        String userId = null;
        if (result == AuditResult.SUCCESS && action == AuditAction.LOGIN) {
            userId = extractUserIdFromSecurity();
        }

        // Calculate duration from request start time if available
        Long durationMs = null;
        Object startTimeObj = request.getAttribute("auditStartTime");
        if (startTimeObj instanceof Long) {
            long startTime = (Long) startTimeObj;
            durationMs = System.currentTimeMillis() - startTime;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("authenticationType", action.name());
        if (errorMessage != null) {
            metadata.put("failureReason", errorMessage);
        }

        logAuditEvent(
                userId,
                username,
                request.getMethod(),
                request.getRequestURI(),
                action,
                "Authentication",
                null,
                null,
                null,
                result,
                result == AuditResult.SUCCESS ? 200 : 401,
                errorMessage,
                getClientIpAddress(request),
                request.getHeader("User-Agent"),
                request.getSession().getId(),
                durationMs,
                metadata
        );
    }

    /**
     * Extract user ID from Spring Security context
     */
    private String extractUserIdFromSecurity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    org.springframework.security.core.userdetails.UserDetails userDetails = 
                        (org.springframework.security.core.userdetails.UserDetails) principal;
                    
                    // If using our User entity, it should have an ID
                    if (userDetails instanceof org.ganjp.blog.auth.model.entity.User) {
                        return ((org.ganjp.blog.auth.model.entity.User) userDetails).getId();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract user ID from security context", e);
        }
        return null;
    }

    /**
     * Extract username from Spring Security context
     */
    private String extractUsernameFromSecurity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("Could not extract username from security context", e);
        }
        return null;
    }

    /**
     * Extract resource type from endpoint
     */
    private String extractResourceTypeFromEndpoint(String endpoint) {
        if (endpoint == null) return null;
        
        if (endpoint.contains("/users")) return "User";
        if (endpoint.contains("/roles")) return "Role";
        if (endpoint.contains("/auth")) return "Authentication";
        
        return "Unknown";
    }

    /**
     * Extract resource ID from endpoint (e.g., /v1/users/{id} -> {id})
     */
    private String extractResourceIdFromEndpoint(String endpoint) {
        if (endpoint == null) return null;
        
        // Look for UUID pattern in the endpoint
        String[] segments = endpoint.split("/");
        for (String segment : segments) {
            // Check if segment looks like a UUID or ID
            if (segment.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}") ||
                segment.matches("\\d+") ||
                (segment.length() > 10 && !segment.contains("-") && segment.matches("[a-zA-Z0-9]+"))) {
                return segment;
            }
        }
        
        return null;
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle comma-separated IPs (take the first one)
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Serialize object to JSON string with size limit
     */
    private String truncateAndSerialize(Object data) {
        if (data == null) return null;
        
        try {
            String json = objectMapper.writeValueAsString(data);
            return truncateString(json, MAX_DATA_LENGTH);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit data", e);
            return truncateString(data.toString(), MAX_DATA_LENGTH);
        }
    }

    /**
     * Serialize metadata map to JSON
     */
    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize metadata", e);
            return null;
        }
    }

    /**
     * Truncate string to specified maximum length
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Clean up old audit logs (for scheduled maintenance)
     */
    @Transactional
    public void cleanupOldAuditLogs(int retentionDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            auditLogRepository.deleteOldAuditLogs(cutoffDate);
            log.info("Cleaned up audit logs older than {} days", retentionDays);
        } catch (Exception e) {
            log.error("Failed to cleanup old audit logs", e);
        }
    }
}
