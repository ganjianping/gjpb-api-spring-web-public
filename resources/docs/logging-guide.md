# Logging Best Practices Guide

This guide provides recommendations for using the logging infrastructure in the GJPB API.

## Key Components

1. **RequestIdFilter**: Automatically adds request ID and session ID to all requests.
2. **LoggingConfig**: Provides centralized logging configuration.
3. **LoggingEnhancer**: Utility for enriching logs with contextual information.
4. **LoggingAspect**: Adds automatic logging around service and controller methods.
5. **AsyncLoggerConfig**: Ensures MDC context is preserved in async operations.

## Basic Logging

### Standard Log Levels

- **ERROR**: Use for errors that affect application functionality
- **WARN**: Use for potentially harmful situations that don't affect core functionality
- **INFO**: Use for important application events (startup, shutdown, major processing)
- **DEBUG**: Use for detailed information helpful during development and debugging

### Including Context Information

All logs automatically include:
- Request ID
- Session ID 
- User ID (when authenticated)
- Username (when authenticated)
- Client IP

### Code Examples

#### Basic Logging

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyService {
    public void doSomething() {
        // The log will automatically include request ID, session ID, etc.
        log.info("Processing operation X");
        
        // Error logging with exception
        try {
            // operation code
        } catch (Exception e) {
            log.error("Failed to complete operation X", e);
        }
    }
}
```

#### Enhanced Exception Logging

```java
import org.ganjp.blog.common.util.LoggingEnhancer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyService {
    public void doSomething() {
        try {
            // operation code
        } catch (Exception e) {
            LoggingEnhancer.logException(log, "doSomething", e, "Custom error context");
        }
    }
}
```

#### Explicit Context Enhancement

```java
import org.ganjp.blog.common.util.LoggingEnhancer;
import org.ganjp.blog.common.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyService {
    public void doSomethingWithRequest(HttpServletRequest request) {
        // Run code with enriched MDC context
        LoggingEnhancer.withEnrichedContext(request, () -> {
            log.info("This log has enhanced context");
            processData();
        });
    }
    
    public Object getSomethingWithResult(HttpServletRequest request) {
        // Run code with enriched MDC context and return result
        return LoggingEnhancer.withEnrichedContext(request, () -> {
            log.info("Processing data with enhanced context");
            return createResult();
        });
    }
    
    public void logCurrentRequest() {
        // Get current request from RequestUtils
        HttpServletRequest request = RequestUtils.getCurrentRequest();
        if (request != null) {
            log.info("Request URI: {}", request.getRequestURI());
        }
    }
}
```

## Async Operations

For `@Async` methods, make sure to use the `asyncExecutor` bean to preserve MDC context:

```java
@Async("asyncExecutor")
public void processDataAsync() {
    // MDC context (request ID, session ID) is preserved from the calling thread
    log.info("Processing data asynchronously");
}
```

## Audit Logging

The AuditService automatically includes request IDs and session IDs in audit logs. Use it for significant business events:

```java
@Autowired
private AuditService auditService;

public void updateImportantResource(String resourceId, Object data, HttpServletRequest request) {
    // Business logic
    
    // Log audit event
    auditService.logSuccess(
        request.getMethod(),
        request.getRequestURI(),
        data,
        result,
        200,
        request,
        timer.durationMs()
    );
}
```

## Adding Custom MDC Values

You can add custom values to the MDC for specific logging needs:

```java
import org.slf4j.MDC;

public void processWithCustomContext() {
    MDC.put("customKey", "customValue");
    try {
        log.info("This log includes the custom context");
    } finally {
        MDC.remove("customKey");
    }
}
```

## Searching Logs

The structured format allows for easy filtering when using log aggregation tools:

- Filter by request ID: `requestId=abc-123`
- Filter by session ID: `sessionId=xyz-789`
- Filter by user: `userId=user-123` or `username=johndoe`
- Filter by client IP: `clientIp=192.168.1.1`
