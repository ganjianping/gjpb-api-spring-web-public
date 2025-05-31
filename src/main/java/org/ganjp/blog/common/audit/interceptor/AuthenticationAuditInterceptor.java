package org.ganjp.blog.common.audit.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.audit.model.enums.AuditAction;
import org.ganjp.blog.common.audit.model.enums.AuditResult;
import org.ganjp.blog.common.audit.service.AuditService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for auditing authentication-related requests.
 * Specifically handles login/logout operations that need special audit treatment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationAuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Store request start time for duration calculation
        request.setAttribute("auditStartTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // Only audit POST requests to authentication endpoints
        if (!"POST".equalsIgnoreCase(method)) {
            return;
        }

        try {
            if (requestUri.contains("/auth/login")) {
                auditLoginAttempt(request, response, ex);
            } else if (requestUri.contains("/auth/logout")) {
                auditLogoutAttempt(request, response, ex);
            } else if (requestUri.contains("/auth/signup")) {
                auditSignupAttempt(request, response, ex);
            }
        } catch (Exception e) {
            log.error("Error during authentication audit logging", e);
        }
    }

    private void auditLoginAttempt(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        AuditResult result;
        String errorMessage = null;
        String username = extractUsernameFromRequest(request);
        Object requestData = extractRequestData(request);
        Object responseData = extractResponseData(request);
        String resourceId = extractResourceId(request);

        if (ex != null) {
            result = AuditResult.ERROR;
            errorMessage = ex.getMessage();
        } else if (response.getStatus() == 200) {
            result = AuditResult.SUCCESS;
        } else if (response.getStatus() == 401) {
            result = AuditResult.AUTHENTICATION_FAILED;
            errorMessage = "Invalid credentials";
        } else {
            result = AuditResult.FAILURE;
            errorMessage = "Login failed with status: " + response.getStatus();
        }

        // Extract request data before async call to avoid recycled request object
        String httpMethod = request.getMethod();
        String requestURI = request.getRequestURI();
        String clientIpAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String sessionId = null;
        try {
            sessionId = request.getSession(false) != null ? request.getSession(false).getId() : null;
        } catch (IllegalStateException e) {
            // Session cannot be accessed after response is committed
            sessionId = "session-unavailable";
        }
        Long startTimeMs = (Long) request.getAttribute("auditStartTime");

        auditService.logAuthenticationEventWithData(
                AuditAction.LOGIN,
                username,
                result,
                errorMessage,
                httpMethod,
                requestURI,
                clientIpAddress,
                userAgent,
                sessionId,
                startTimeMs,
                requestData,
                responseData,
                resourceId
        );
    }

    private void auditLogoutAttempt(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        AuditResult result;
        String errorMessage = null;
        String username = extractUsernameFromSecurity();
        Object requestData = extractRequestData(request);
        Object responseData = extractResponseData(request);
        String resourceId = extractResourceId(request);

        if (ex != null) {
            result = AuditResult.ERROR;
            errorMessage = ex.getMessage();
        } else if (response.getStatus() == 200) {
            result = AuditResult.SUCCESS;
        } else {
            result = AuditResult.FAILURE;
            errorMessage = "Logout failed with status: " + response.getStatus();
        }

        // Extract request data before async call to avoid recycled request object
        String httpMethod = request.getMethod();
        String requestURI = request.getRequestURI();
        String clientIpAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String sessionId = null;
        try {
            sessionId = request.getSession(false) != null ? request.getSession(false).getId() : null;
        } catch (IllegalStateException e) {
            // Session cannot be accessed after response is committed
            sessionId = "session-unavailable";
        }
        Long startTimeMs = (Long) request.getAttribute("auditStartTime");

        auditService.logAuthenticationEventWithData(
                AuditAction.LOGOUT,
                username,
                result,
                errorMessage,
                httpMethod,
                requestURI,
                clientIpAddress,
                userAgent,
                sessionId,
                startTimeMs,
                requestData,
                responseData,
                resourceId
        );
    }

    private void auditSignupAttempt(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        AuditResult result;
        String errorMessage = null;
        String username = extractUsernameFromRequest(request);
        Object requestData = extractRequestData(request);
        Object responseData = extractResponseData(request);
        String resourceId = extractResourceId(request);

        if (ex != null) {
            result = AuditResult.ERROR;
            errorMessage = ex.getMessage();
        } else if (response.getStatus() == 200 || response.getStatus() == 201) {
            result = AuditResult.SUCCESS;
        } else if (response.getStatus() == 400 || response.getStatus() == 422) {
            result = AuditResult.VALIDATION_ERROR;
            errorMessage = "Signup validation failed";
        } else if (response.getStatus() == 409) {
            result = AuditResult.FAILURE;
            errorMessage = "User already exists";
        } else {
            result = AuditResult.FAILURE;
            errorMessage = "Signup failed with status: " + response.getStatus();
        }

        // Extract request data before async call to avoid recycled request object
        String httpMethod = request.getMethod();
        String requestURI = request.getRequestURI();
        String clientIpAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String sessionId = null;
        try {
            sessionId = request.getSession(false) != null ? request.getSession(false).getId() : null;
        } catch (IllegalStateException e) {
            // Session cannot be accessed after response is committed
            sessionId = "session-unavailable";
        }
        Long startTimeMs = (Long) request.getAttribute("auditStartTime");

        auditService.logAuthenticationEventWithData(
                AuditAction.SIGNUP,
                username,
                result,
                errorMessage,
                httpMethod,
                requestURI,
                clientIpAddress,
                userAgent,
                sessionId,
                startTimeMs,
                requestData,
                responseData,
                resourceId
        );
    }

    /**
     * Extract username from request parameters or body (for failed login attempts)
     */
    private String extractUsernameFromRequest(HttpServletRequest request) {
        // Try to get username from request parameters first (for form-based auth)
        String username = request.getParameter("username");
        if (username != null) {
            return username;
        }

        // Try to get username from request attributes (set by controller)
        Object usernameAttr = request.getAttribute("loginUsername");
        if (usernameAttr instanceof String) {
            return (String) usernameAttr;
        }

        // For successful logins, try to get from security context
        return extractUsernameFromSecurity();
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
     * Extract request data from request attributes (set by controller)
     */
    private Object extractRequestData(HttpServletRequest request) {
        return request.getAttribute("loginRequestData");
    }

    /**
     * Extract response data from request attributes (set by controller)
     */
    private Object extractResponseData(HttpServletRequest request) {
        return request.getAttribute("loginResponseData");
    }

    /**
     * Extract resource ID (user ID) from request attributes
     */
    private String extractResourceId(HttpServletRequest request) {
        Object resourceId = request.getAttribute("loginResourceId");
        return resourceId instanceof String ? (String) resourceId : null;
    }

    /**
     * Get client IP address from request (same logic as AuditService)
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

        // Fallback to remote address
        return request.getRemoteAddr();
    }
}
