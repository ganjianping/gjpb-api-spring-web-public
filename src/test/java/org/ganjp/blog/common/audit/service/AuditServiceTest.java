package org.ganjp.blog.common.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ganjp.blog.common.audit.model.entity.AuditLog;
import org.ganjp.blog.common.audit.model.enums.AuditAction;
import org.ganjp.blog.common.audit.model.enums.AuditResult;
import org.ganjp.blog.common.audit.repository.AuditLogRepository;
import org.ganjp.blog.auth.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Tests")
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditService auditService;

    private MockHttpServletRequest request;
    private User testUser;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.addHeader("X-Forwarded-For", "203.0.113.195");
        request.setMethod("POST");
        request.setRequestURI("/v1/users");
        request.setSession(new MockHttpSession());

        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setUsername("testuser");

        // Mock security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    @DisplayName("Should log audit event with full parameters")
    void shouldLogAuditEventWithFullParameters() throws Exception {
        // Given
        String requestData = "{\"name\":\"Test User\"}";
        String responseData = "{\"id\":\"user-123\",\"name\":\"Test User\"}";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "manual");
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        auditService.logAuditEvent(
            "user-123",
            "testuser",
            "POST",
            "/v1/users",
            AuditAction.USER_CREATE,
            "User",
            "user-123",
            requestData,
            responseData,
            AuditResult.SUCCESS,
            201,
            null,
            "192.168.1.100",
            "Mozilla/5.0",
            "session-123",
            100L,
            metadata
        );

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserId()).isEqualTo("user-123");
        assertThat(savedLog.getUsername()).isEqualTo("testuser");
        assertThat(savedLog.getHttpMethod()).isEqualTo("POST");
        assertThat(savedLog.getEndpoint()).isEqualTo("/v1/users");
        assertThat(savedLog.getAction()).isEqualTo(AuditAction.USER_CREATE);
        assertThat(savedLog.getResourceType()).isEqualTo("User");
        assertThat(savedLog.getResourceId()).isEqualTo("user-123");
        assertThat(savedLog.getResult()).isEqualTo(AuditResult.SUCCESS);
        assertThat(savedLog.getStatusCode()).isEqualTo(201);
        assertThat(savedLog.getIpAddress()).isEqualTo("192.168.1.100");
        assertThat(savedLog.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(savedLog.getSessionId()).isEqualTo("session-123");
        assertThat(savedLog.getDurationMs()).isEqualTo(100L);
        assertThat(savedLog.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should log audit event with minimal information")
    void shouldLogAuditEventWithMinimalInfo() {
        // When
        auditService.logAuditEvent("POST", "/v1/users", AuditResult.SUCCESS, 201, request);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserId()).isEqualTo("test-user-id");
        assertThat(savedLog.getUsername()).isEqualTo("testuser");
        assertThat(savedLog.getHttpMethod()).isEqualTo("POST");
        assertThat(savedLog.getEndpoint()).isEqualTo("/v1/users");
        assertThat(savedLog.getResult()).isEqualTo(AuditResult.SUCCESS);
        assertThat(savedLog.getStatusCode()).isEqualTo(201);
        assertThat(savedLog.getIpAddress()).isEqualTo("203.0.113.195"); // X-Forwarded-For takes precedence
        assertThat(savedLog.getUserAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    @DisplayName("Should log successful operation")
    void shouldLogSuccessfulOperation() throws Exception {
        // Given
        String requestData = "{\"name\":\"Test User\"}";
        String responseData = "{\"id\":\"user-123\"}";
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        auditService.logSuccess("POST", "/v1/users", requestData, responseData, 201, request, 150L);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getResult()).isEqualTo(AuditResult.SUCCESS);
        assertThat(savedLog.getStatusCode()).isEqualTo(201);
        assertThat(savedLog.getDurationMs()).isEqualTo(150L);
        assertThat(savedLog.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should log failed operation")
    void shouldLogFailedOperation() throws Exception {
        // Given
        String requestData = "{\"name\":\"\"}";
        String errorMessage = "Validation failed: name is required";
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        auditService.logFailure("POST", "/v1/users", requestData, errorMessage, 400, request, 50L);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getResult()).isEqualTo(AuditResult.FAILURE);
        assertThat(savedLog.getStatusCode()).isEqualTo(400);
        assertThat(savedLog.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(savedLog.getDurationMs()).isEqualTo(50L);
        assertThat(savedLog.getResponseData()).isNull();
    }

    @Test
    @DisplayName("Should log authentication event - login success")
    void shouldLogAuthenticationEventLoginSuccess() {
        // When
        auditService.logAuthenticationEvent(AuditAction.LOGIN, "testuser", AuditResult.SUCCESS, null, request);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getAction()).isEqualTo(AuditAction.LOGIN);
        assertThat(savedLog.getUsername()).isEqualTo("testuser");
        assertThat(savedLog.getResult()).isEqualTo(AuditResult.SUCCESS);
        assertThat(savedLog.getStatusCode()).isEqualTo(200);
        assertThat(savedLog.getResourceType()).isEqualTo("Authentication");
        assertThat(savedLog.getMetadata()).contains("authenticationType");
    }

    @Test
    @DisplayName("Should log authentication event - login failure")
    void shouldLogAuthenticationEventLoginFailure() {
        // When
        auditService.logAuthenticationEvent(AuditAction.LOGIN, "invaliduser", AuditResult.FAILURE, "Invalid credentials", request);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getAction()).isEqualTo(AuditAction.LOGIN);
        assertThat(savedLog.getUsername()).isEqualTo("invaliduser");
        assertThat(savedLog.getResult()).isEqualTo(AuditResult.FAILURE);
        assertThat(savedLog.getStatusCode()).isEqualTo(401);
        assertThat(savedLog.getErrorMessage()).isEqualTo("Invalid credentials");
        assertThat(savedLog.getMetadata()).contains("failureReason");
    }

    @Test
    @DisplayName("Should log logout event")
    void shouldLogLogoutEvent() {
        // When
        auditService.logAuthenticationEvent(AuditAction.LOGOUT, "testuser", AuditResult.SUCCESS, null, request);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getAction()).isEqualTo(AuditAction.LOGOUT);
        assertThat(savedLog.getUsername()).isEqualTo("testuser");
        assertThat(savedLog.getResult()).isEqualTo(AuditResult.SUCCESS);
        assertThat(savedLog.getResourceType()).isEqualTo("Authentication");
    }

    @Test
    @DisplayName("Should handle unauthenticated requests")
    void shouldHandleUnauthenticatedRequests() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        auditService.logAuditEvent("POST", "/v1/auth/login", AuditResult.FAILURE, 401, request);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserId()).isNull();
        assertThat(savedLog.getUsername()).isNull();
    }

    @Test
    @DisplayName("Should handle anonymous users")
    void shouldHandleAnonymousUsers() {
        // Given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // When
        auditService.logAuditEvent("GET", "/v1/public/info", AuditResult.SUCCESS, 200, request);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserId()).isNull();
    }

    @Test
    @DisplayName("Should extract client IP from X-Forwarded-For header")
    void shouldExtractClientIpFromForwardedFor() {
        // Given
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        request.setRemoteAddr("10.0.0.1");

        // When
        auditService.logAuditEvent("POST", "/v1/users", AuditResult.SUCCESS, 201, request);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getIpAddress()).isEqualTo("203.0.113.195"); // First IP in the chain
    }

    @Test
    @DisplayName("Should handle missing security context gracefully")
    void shouldHandleMissingSecurityContextGracefully() {
        // Given
        SecurityContextHolder.clearContext();

        // When
        auditService.logAuditEvent("POST", "/v1/users", AuditResult.SUCCESS, 201, request);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserId()).isNull();
        assertThat(savedLog.getUsername()).isNull();
    }

    @Test
    @DisplayName("Should handle null request gracefully")
    void shouldHandleNullRequestGracefully() {
        // When
        auditService.logAuditEvent("POST", "/v1/users", AuditResult.SUCCESS, 201, null);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getIpAddress()).isNull();
        assertThat(savedLog.getUserAgent()).isNull();
        assertThat(savedLog.getSessionId()).isNull();
    }

    @Test
    @DisplayName("Should handle exceptions during audit logging gracefully")
    void shouldHandleExceptionsGracefully() {
        // Given
        when(auditLogRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        // When & Then - should not throw exception
        auditService.logAuditEvent("POST", "/v1/users", AuditResult.SUCCESS, 201, request);

        // Verify that the exception is caught and logged, but doesn't propagate
        verify(auditLogRepository).save(any());
    }
}
