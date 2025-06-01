package org.ganjp.blog.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that adds a unique request ID to each incoming HTTP request.
 * The request ID is stored in the request attributes and can be retrieved
 * throughout the request processing.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_ATTRIBUTE = "requestId";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Generate a unique request ID (UUID v4)
        String requestId = UUID.randomUUID().toString();
        
        // Store the request ID in the request attributes
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        
        // Also add the request ID as a response header
        response.setHeader(REQUEST_ID_HEADER, requestId);
        
        // Add request ID to MDC for logging
        MDC.put(REQUEST_ID_ATTRIBUTE, requestId);
        
        // Log the request with its ID
        log.debug("Processing request with ID: {}, URI: {}, Method: {}", 
                requestId, request.getRequestURI(), request.getMethod());
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Log completion of request processing
            log.debug("Completed request with ID: {}", requestId);
            
            // Remove request ID from MDC
            MDC.remove(REQUEST_ID_ATTRIBUTE);
        }
    }
}
