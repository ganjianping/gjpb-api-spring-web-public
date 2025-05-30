package org.ganjp.blog.auth.integration;

import org.ganjp.blog.auth.model.dto.request.SignupRequest;
import org.ganjp.blog.auth.model.entity.Role;
import org.ganjp.blog.auth.repository.RoleRepository;
import org.ganjp.blog.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Ensures test data is rolled back after each test
class SignupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Ensure the USER role exists
        if (!roleRepository.existsByCode("USER")) {
            Role userRole = new Role();
            userRole.setId(UUID.randomUUID().toString());
            userRole.setCode("USER");
            userRole.setName("Regular User");
            userRole.setDescription("Standard user role with basic permissions");
            userRole.setSortOrder(100);
            userRole.setActive(true);
            userRole.setCreatedAt(LocalDateTime.now());
            userRole.setUpdatedAt(LocalDateTime.now());
            
            roleRepository.save(userRole);
        }
    }

    @Test
    @DisplayName("POST /v1/auth/signup - Success")
    void testSuccessfulSignup() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .username("integrationuser")
                .password("Integration1!")
                .email("integration@example.com")
                .build();

        mockMvc.perform(post("/v1/auth/signup")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        // Verify user exists in the database
        assertTrue(userRepository.existsByUsername("integrationuser"));
    }

    @Test
    @DisplayName("POST /v1/auth/signup - Username Already Taken")
    void testSignupWithDuplicateUsername() throws Exception {
        // First create a user
        SignupRequest signupRequest = SignupRequest.builder()
                .username("duplicateuser")
                .password("Duplicate1!")
                .email("duplicate@example.com") // Add required email field
                .build();

        // First signup should succeed with 201 Created
        mockMvc.perform(post("/v1/auth/signup")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        // Then try to create another user with the same username - should get 400 Bad Request
        mockMvc.perform(post("/v1/auth/signup")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code", is(400)))
                .andExpect(jsonPath("$.status.message", is("Registration failed")))
                .andExpect(jsonPath("$.status.errors.error", is("Username is already taken")));
    }

    @Test
    @DisplayName("POST /v1/auth/signup - Invalid Password Format")
    void testSignupWithInvalidPasswordFormat() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .username("passworduser")
                .password("weak")  // Doesn't meet password requirements
                .build();

        mockMvc.perform(post("/v1/auth/signup")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }
}
