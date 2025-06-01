package org.ganjp.blog.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ganjp.blog.auth.model.dto.request.RegisterRequest;
import org.ganjp.blog.auth.model.dto.response.RegisterResponse;
import org.ganjp.blog.auth.model.enums.AccountStatus;
import org.ganjp.blog.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = RegisterController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = org.ganjp.blog.common.audit.interceptor.AuthenticationAuditInterceptor.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
class RegisterControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private org.ganjp.blog.common.audit.service.AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRequest;
    private RegisterResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .mobileCountryCode("86")
                .mobileNumber("13800138000")
                .password("Password1!")
                .nickname("Test User")
                .build();
        validResponse = RegisterResponse.builder()
                .id("user-123")
                .username("testuser")
                .email("test@example.com")
                .mobileCountryCode("86")
                .mobileNumber("13800138000")
                .nickname("Test User")
                .accountStatus(AccountStatus.pending_verification)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(validResponse);

        ResultActions result = mockMvc.perform(post("/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));

        try {
            result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status.code").value(201))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
        } catch (AssertionError e) {
            System.out.println("Response: " + result.andReturn().getResponse().getContentAsString());
            throw e;
        }
    }

    @Test
    @DisplayName("Should return 400 if registration fails due to duplicate username/email/mobile")
    void shouldReturnBadRequestOnDuplicate() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenThrow(new IllegalArgumentException("Username is already taken"));

        ResultActions result = mockMvc.perform(post("/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(400))
                .andExpect(jsonPath("$.status.message").value("Registration failed"))
                .andExpect(jsonPath("$.status.errors.error").value("Username is already taken"));
    }

    @Test
    @DisplayName("Should return 500 if unexpected error occurs")
    void shouldReturnInternalServerError() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenThrow(new RuntimeException("Unexpected error"));

        ResultActions result = mockMvc.perform(post("/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));

        result.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status.code").value(500))
                .andExpect(jsonPath("$.status.message").value("Internal Server Error"))
                .andExpect(jsonPath("$.status.errors.error").value("Unexpected error"));
    }

    @Test
    @DisplayName("Should return 400 if validation fails (e.g., missing username)")
    void shouldReturnBadRequestOnValidationFailure() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password1!")
                .build(); // missing username

        ResultActions result = mockMvc.perform(post("/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
    }
}
