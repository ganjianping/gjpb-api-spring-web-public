package org.ganjp.blog.common.audit.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ganjp.blog.common.audit.service.AuditService;
import org.ganjp.blog.common.model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspect for auditing API calls.
 * Automatically logs all non-GET HTTP requests to controllers.
 * 
 * Note: Authentication endpoints (/auth/login, /auth/logout, /auth/signup) are excluded
 * from this aspect as they are specifically handled by AuthenticationAuditInterceptor
 * to prevent duplicate audit logging.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    /**
     * Intercept all controller methods (excluding GET requests)
     */
    @Around("execution(* org.ganjp.blog.*.controller.*.*(..))")
    public Object auditApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // Get the HTTP request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        String httpMethod = request.getMethod();
        
        // Skip GET requests as they are read-only
        if ("GET".equalsIgnoreCase(httpMethod)) {
            return joinPoint.proceed();
        }
        
        String endpoint = request.getRequestURI();
        
        // Skip authentication endpoints as they are handled by AuthenticationAuditInterceptor
        if (isAuthenticationEndpoint(endpoint)) {
            return joinPoint.proceed();
        }
        
        Object[] args = joinPoint.getArgs();
        Object result = null;
        Throwable exception = null;
        
        try {
            // Proceed with the actual method execution
            result = joinPoint.proceed();
            
            // Calculate duration
            long duration = System.currentTimeMillis() - startTime;
            
            // Extract response data and status code
            Object responseData = null;
            Integer statusCode = 200;
            
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
                responseData = responseEntity.getBody();
                statusCode = responseEntity.getStatusCode().value();
            }
            
            // Log successful operation
            auditService.logSuccess(
                httpMethod,
                endpoint,
                extractRequestData(args),
                sanitizeResponseData(responseData),
                statusCode,
                request,
                duration
            );
            
            return result;
            
        } catch (Throwable e) {
            exception = e;
            
            // Calculate duration
            long duration = System.currentTimeMillis() - startTime;
            
            // Determine status code based on exception type
            Integer statusCode = determineStatusCodeFromException(e);
            
            // Log failed operation
            auditService.logFailure(
                httpMethod,
                endpoint,
                extractRequestData(args),
                e.getMessage(),
                statusCode,
                request,
                duration
            );
            
            throw e;
        }
    }

    /**
     * Extract relevant request data from method arguments
     */
    private Object extractRequestData(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        // Look for request DTOs (usually the first non-primitive argument)
        for (Object arg : args) {
            if (arg != null && isRequestDto(arg)) {
                return sanitizeRequestData(arg);
            }
        }
        
        return null;
    }

    /**
     * Check if an object is likely a request DTO
     */
    private boolean isRequestDto(Object obj) {
        if (obj == null) return false;
        
        String className = obj.getClass().getSimpleName();
        String packageName = obj.getClass().getPackage().getName();
        
        // Skip Spring framework objects and primitive types
        return !packageName.startsWith("org.springframework") &&
               !packageName.startsWith("jakarta.servlet") &&
               !obj.getClass().isPrimitive() &&
               !(obj instanceof String) &&
               !(obj instanceof Number) &&
               // Include our DTO classes
               (className.endsWith("Request") || 
                className.endsWith("DTO") || 
                packageName.contains("dto"));
    }

    /**
     * Sanitize request data to remove sensitive information
     */
    private Object sanitizeRequestData(Object data) {
        if (data == null) return null;
        
        // For security, create a sanitized copy without sensitive fields
        // This is a simple implementation - you might want to use more sophisticated approaches
        String dataStr = data.toString();
        
        // Remove potential passwords and sensitive data
        dataStr = dataStr.replaceAll("password[^,}]*", "password=***");
        dataStr = dataStr.replaceAll("token[^,}]*", "token=***");
        dataStr = dataStr.replaceAll("secret[^,}]*", "secret=***");
        
        return dataStr;
    }

    /**
     * Sanitize response data to remove sensitive information
     */
    private Object sanitizeResponseData(Object responseData) {
        if (responseData == null) return null;
        
        // If it's an ApiResponse, extract the data
        if (responseData instanceof ApiResponse) {
            ApiResponse<?> apiResponse = (ApiResponse<?>) responseData;
            return createSanitizedResponse(apiResponse);
        }
        
        return sanitizeRequestData(responseData);
    }

    /**
     * Create a sanitized version of ApiResponse for logging
     */
    private Object createSanitizedResponse(ApiResponse<?> apiResponse) {
        return new Object() {
            public final boolean success = apiResponse.getStatus() != null && apiResponse.getStatus().getCode() >= 200 && apiResponse.getStatus().getCode() < 300;
            public final String message = apiResponse.getStatus() != null ? apiResponse.getStatus().getMessage() : null;
            public final Object data = sanitizeDataForLogging(apiResponse.getData());
        };
    }

    /**
     * Sanitize data for logging purposes
     */
    private Object sanitizeDataForLogging(Object data) {
        if (data == null) return null;
        
        // For complex objects, just return basic info
        String className = data.getClass().getSimpleName();
        if (data instanceof java.util.Collection) {
            java.util.Collection<?> collection = (java.util.Collection<?>) data;
            return className + " with " + collection.size() + " items";
        }
        
        return className + " object";
    }

    /**
     * Determine HTTP status code based on exception type
     */
    private Integer determineStatusCodeFromException(Throwable exception) {
        String exceptionName = exception.getClass().getSimpleName();
        
        if (exceptionName.contains("NotFound") || exceptionName.contains("ResourceNotFound")) {
            return 404;
        } else if (exceptionName.contains("Validation") || exceptionName.contains("IllegalArgument")) {
            return 400;
        } else if (exceptionName.contains("AccessDenied") || exceptionName.contains("Forbidden")) {
            return 403;
        } else if (exceptionName.contains("Authentication") || exceptionName.contains("Unauthorized")) {
            return 401;
        } else if (exceptionName.contains("Conflict")) {
            return 409;
        } else {
            return 500;
        }
    }

    /**
     * Check if the endpoint is an authentication endpoint that should be handled
     * by AuthenticationAuditInterceptor instead of this aspect.
     */
    private boolean isAuthenticationEndpoint(String endpoint) {
        return endpoint != null && (
            endpoint.contains("/auth/login") ||
            endpoint.contains("/auth/logout") ||
            endpoint.contains("/auth/signup")
        );
    }
}
