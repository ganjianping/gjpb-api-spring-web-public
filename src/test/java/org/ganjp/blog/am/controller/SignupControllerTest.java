package org.ganjp.blog.am.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ganjp.blog.am.model.dto.request.SignupRequest;
import org.ganjp.blog.am.model.dto.response.SignupResponse;
import org.ganjp.blog.am.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = {AuthController.class})
@Import(SignupControllerTest.TestConfig.class)
@WithMockUser
class SignupControllerTest {

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
    @DisplayName("Signup with valid registration data should return 201 Created")
    void signupWithValidDataShouldReturnCreated() throws Exception {
        // Arrange
        SignupRequest signupRequest = SignupRequest.builder()
                .username("newuser")
                .password("Password1!")
                .email("newuser@example.com")
                .firstName("New")
                .lastName("User")
                .build();
        
        SignupResponse signupResponse = SignupResponse.builder()
                .id("generated-uuid")
                .username("newuser")
                .active(true)
                .build();
        
        when(authService.signup(any(SignupRequest.class))).thenReturn(signupResponse);
        
        // Act & Assert
        mockMvc.perform(post("/v1/auth/signup")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.status.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.id").value("generated-uuid"))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("Signup with already taken username should return 400 Bad Request")
    void signupWithTakenUsernameShouldReturnBadRequest() throws Exception {
        // Arrange
        SignupRequest signupRequest = SignupRequest.builder()
                .username("existinguser")
                .password("Password1!")
                .build();
        
        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new IllegalArgumentException("Username is already taken"));
        
        // Act & Assert
        mockMvc.perform(post("/v1/auth/signup")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value(400))
                .andExpect(jsonPath("$.status.message").value("Registration failed"))
                .andExpect(jsonPath("$.status.errors.error").value("Username is already taken"));
    }

    @Test
    @DisplayName("Signup with invalid password format should return 400 Bad Request")
    void signupWithInvalidPasswordFormatShouldReturnBadRequest() throws Exception {
        // Arrange
        SignupRequest signupRequest = SignupRequest.builder()
                .username("newuser")
                .password("weak")  // Doesn't meet password requirements
                .build();
        
        // Act & Assert
        mockMvc.perform(post("/v1/auth/signup")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }
}
