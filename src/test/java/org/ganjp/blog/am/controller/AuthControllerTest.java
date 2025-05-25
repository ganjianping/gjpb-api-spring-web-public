package org.ganjp.blog.am.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.ganjp.blog.am.model.dto.request.LoginRequest;
import org.ganjp.blog.am.model.dto.response.LoginResponse;
import org.ganjp.blog.am.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = {AuthController.class})
@Import(AuthControllerTest.TestConfig.class)
@WithMockUser
class AuthControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public AuthService authService() {
            return mock(AuthService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("Login with valid credentials should return JWT token")
    void loginWithValidCredentialsShouldReturnJwtToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("correctPassword")
                .build();
        String jwtToken = "valid.jwt.token";
        LoginResponse loginResponse = LoginResponse.builder().token(jwtToken).build();
        
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);
        
        // Act & Assert
        mockMvc.perform(post("/v1/auth/login")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.status.message").value("User login successful"))
                .andExpect(jsonPath("$.data.token").value(jwtToken));
    }

    @Test
    @DisplayName("Login with invalid credentials should return 401 Unauthorized")
    void loginWithInvalidCredentialsShouldReturnUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("wrongPassword")
                .build();
        
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Bad credentials"));
        
        // Act & Assert
        mockMvc.perform(post("/v1/auth/login")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status.code").value(401))
                .andExpect(jsonPath("$.status.message").value("Unauthorized"))
                .andExpect(jsonPath("$.status.errors.error").value("Bad credentials"));
    }
}