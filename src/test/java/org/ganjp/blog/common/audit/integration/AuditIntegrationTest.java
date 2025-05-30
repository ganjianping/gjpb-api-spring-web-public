package org.ganjp.blog.common.audit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ganjp.blog.common.audit.model.entity.AuditLog;
import org.ganjp.blog.common.audit.model.enums.AuditAction;
import org.ganjp.blog.common.audit.model.enums.AuditResult;
import org.ganjp.blog.common.audit.repository.AuditLogRepository;
import org.ganjp.blog.auth.model.dto.request.UserUpsertRequest;
import org.ganjp.blog.auth.model.dto.request.UserPatchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Audit Integration Tests")
class AuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should audit user creation operation")
    void shouldAuditUserCreation() throws Exception {
        // Given
        UserUpsertRequest createRequest = new UserUpsertRequest();
        createRequest.setUsername("newuser");
        createRequest.setNickname("New User");
        createRequest.setEmail("newuser@example.com");
        createRequest.setPassword("Password123!");

        String requestJson = objectMapper.writeValueAsString(createRequest);

        // When
        mockMvc.perform(post("/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isCreated());

        // Then - Wait for async audit logging
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getAction()).isEqualTo(AuditAction.USER_CREATE);
            assertThat(auditLog.getResult()).isEqualTo(AuditResult.SUCCESS);
            assertThat(auditLog.getResourceType()).isEqualTo("User");
            assertThat(auditLog.getHttpMethod()).isEqualTo("POST");
            assertThat(auditLog.getEndpoint()).isEqualTo("/v1/users");
            assertThat(auditLog.getUsername()).isEqualTo("admin");
            assertThat(auditLog.getRequestData()).contains("newuser");
            assertThat(auditLog.getRequestData()).doesNotContain("Password123!");
        });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should audit user update operation")
    void shouldAuditUserUpdate() throws Exception {
        // Given
        UserPatchRequest updateRequest = new UserPatchRequest();
        updateRequest.setNickname("Updated User");

        String requestJson = objectMapper.writeValueAsString(updateRequest);

        // When
        mockMvc.perform(put("/v1/users/user-123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getAction()).isEqualTo(AuditAction.USER_UPDATE);
            assertThat(auditLog.getResourceType()).isEqualTo("User");
            assertThat(auditLog.getResourceId()).isEqualTo("user-123");
            assertThat(auditLog.getHttpMethod()).isEqualTo("PUT");
            assertThat(auditLog.getEndpoint()).isEqualTo("/v1/users/user-123");
        });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should audit user deletion operation")
    void shouldAuditUserDeletion() throws Exception {
        // When
        mockMvc.perform(delete("/v1/users/user-123")
                .with(csrf()))
                .andDo(print());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getAction()).isEqualTo(AuditAction.USER_DELETE);
            assertThat(auditLog.getResourceType()).isEqualTo("User");
            assertThat(auditLog.getResourceId()).isEqualTo("user-123");
            assertThat(auditLog.getHttpMethod()).isEqualTo("DELETE");
        });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should audit password change operation")
    void shouldAuditPasswordChange() throws Exception {
        // Given
        String passwordChangeJson = "{\"oldPassword\":\"oldpass\",\"newPassword\":\"newpass\"}";

        // When
        mockMvc.perform(patch("/v1/users/user-123/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordChangeJson))
                .andDo(print());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getAction()).isEqualTo(AuditAction.PASSWORD_CHANGE);
            assertThat(auditLog.getResourceType()).isEqualTo("User");
            assertThat(auditLog.getResourceId()).isEqualTo("user-123");
            assertThat(auditLog.getHttpMethod()).isEqualTo("PATCH");
            assertThat(auditLog.getRequestData()).doesNotContain("oldpass");
            assertThat(auditLog.getRequestData()).doesNotContain("newpass");
        });
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Should audit failed operations due to insufficient permissions")
    void shouldAuditFailedOperationsDueToPermissions() throws Exception {
        // When - User without admin role tries to create user
        mockMvc.perform(post("/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\"}"))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getAction()).isEqualTo(AuditAction.USER_CREATE);
            assertThat(auditLog.getResult()).isEqualTo(AuditResult.DENIED);
            assertThat(auditLog.getUsername()).isEqualTo("user");
        });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should audit validation errors")
    void shouldAuditValidationErrors() throws Exception {
        // Given - Invalid user data
        String invalidUserJson = "{\"username\":\"\",\"email\":\"invalid-email\"}";

        // When
        mockMvc.perform(post("/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUserJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getAction()).isEqualTo(AuditAction.USER_CREATE);
            assertThat(auditLog.getResult()).isEqualTo(AuditResult.VALIDATION_ERROR);
        });
    }

    @Test
    @DisplayName("Should audit authentication attempts")
    void shouldAuditAuthenticationAttempts() throws Exception {
        // Given
        String loginJson = "{\"username\":\"testuser\",\"password\":\"password\"}";

        // When
        mockMvc.perform(post("/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andDo(print());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getAction()).isEqualTo(AuditAction.LOGIN);
            assertThat(auditLog.getHttpMethod()).isEqualTo("POST");
            assertThat(auditLog.getEndpoint()).isEqualTo("/v1/auth/login");
            assertThat(auditLog.getRequestData()).doesNotContain("password");
        });
    }

    @Test
    @DisplayName("Should audit signup attempts")
    void shouldAuditSignupAttempts() throws Exception {
        // Given
        String signupJson = "{\"username\":\"newuser\",\"email\":\"new@example.com\",\"password\":\"Password123!\"}";

        // When
        mockMvc.perform(post("/v1/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupJson))
                .andDo(print());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getAction()).isEqualTo(AuditAction.SIGNUP);
            assertThat(auditLog.getHttpMethod()).isEqualTo("POST");
            assertThat(auditLog.getEndpoint()).isEqualTo("/v1/auth/signup");
            assertThat(auditLog.getRequestData()).doesNotContain("Password123!");
        });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should audit role operations")
    void shouldAuditRoleOperations() throws Exception {
        // Given
        String roleJson = "{\"name\":\"NEW_ROLE\",\"description\":\"New test role\"}";

        // When - Create role
        mockMvc.perform(post("/v1/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleJson))
                .andDo(print());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getAction()).isEqualTo(AuditAction.ROLE_CREATE);
            assertThat(auditLog.getResourceType()).isEqualTo("Role");
            assertThat(auditLog.getHttpMethod()).isEqualTo("POST");
            assertThat(auditLog.getEndpoint()).isEqualTo("/v1/roles");
        });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should capture client information in audit logs")
    void shouldCaptureClientInformation() throws Exception {
        // When
        mockMvc.perform(post("/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\"}")
                .header("User-Agent", "Test-Agent/1.0")
                .with(request -> {
                    request.setRemoteAddr("192.168.1.100");
                    return request;
                }))
                .andDo(print());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getIpAddress()).isEqualTo("192.168.1.100");
            assertThat(auditLog.getUserAgent()).isEqualTo("Test-Agent/1.0");
            assertThat(auditLog.getSessionId()).isNotNull();
        });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should measure operation duration")
    void shouldMeasureOperationDuration() throws Exception {
        // When
        mockMvc.perform(post("/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\"}"))
                .andDo(print());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).hasSize(1);

            AuditLog auditLog = auditLogs.get(0);
            assertThat(auditLog.getDurationMs()).isNotNull();
            assertThat(auditLog.getDurationMs()).isGreaterThanOrEqualTo(0L);
        });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should audit multiple operations in sequence")
    void shouldAuditMultipleOperations() throws Exception {
        // When - Perform multiple operations
        mockMvc.perform(post("/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user1\",\"email\":\"user1@example.com\",\"password\":\"Password1!\"}"))
                .andDo(print());

        mockMvc.perform(post("/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user2\",\"email\":\"user2@example.com\",\"password\":\"Password2!\"}"))
                .andDo(print());

        mockMvc.perform(delete("/v1/users/user-123")
                .with(csrf()))
                .andDo(print());

        // Then
        await().untilAsserted(() -> {
            List<AuditLog> auditLogs = auditLogRepository.findAll();
            auditLogs.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
            assertThat(auditLogs).hasSize(3);

            assertThat(auditLogs.get(0).getAction()).isEqualTo(AuditAction.USER_CREATE);
            assertThat(auditLogs.get(1).getAction()).isEqualTo(AuditAction.USER_CREATE);
            assertThat(auditLogs.get(2).getAction()).isEqualTo(AuditAction.USER_DELETE);

            // Verify timestamps are in order
            assertThat(auditLogs.get(0).getTimestamp()).isBefore(auditLogs.get(1).getTimestamp());
            assertThat(auditLogs.get(1).getTimestamp()).isBefore(auditLogs.get(2).getTimestamp());
        });
    }
}
