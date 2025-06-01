package org.ganjp.blog.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ganjp.blog.auth.model.dto.request.PasswordChangeRequest;
import org.ganjp.blog.auth.model.dto.request.UserUpsertRequest;
import org.ganjp.blog.auth.model.dto.request.UserPatchRequest;
import org.ganjp.blog.auth.model.dto.response.UserResponse;
import org.ganjp.blog.auth.model.enums.AccountStatus;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = UserController.class,
    excludeFilters = @ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = org.ganjp.blog.common.audit.interceptor.AuthenticationAuditInterceptor.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    private UserResponse userResponse;
    private UserUpsertRequest userUpsertRequest;
    private UserPatchRequest userPatchRequest;
    private PasswordChangeRequest passwordChangeRequest;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id("user-1")
                .username("testuser")
                .email("test@example.com")
                .nickname("Test User")
                .mobileCountryCode("86")
                .mobileNumber("13800138000")
                .accountStatus(AccountStatus.active)
                .active(true)
                .build();
        userUpsertRequest = UserUpsertRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .nickname("Test User")
                .mobileCountryCode("86")
                .mobileNumber("13800138000")
                .password("Password1!")
                .build();
        userPatchRequest = UserPatchRequest.builder()
                .nickname("Patched User")
                .build();
        passwordChangeRequest = PasswordChangeRequest.builder()
                .password("NewPassword1!")
                .build();
    }

    @Test
    @DisplayName("Get all users")
    void getAllUsers() throws Exception {
        Page<UserResponse> page = new PageImpl<>(Collections.singletonList(userResponse), PageRequest.of(0, 10), 1);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"));
    }

    @Test
    @DisplayName("Get user by username")
    void getUserByUsername() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(userResponse);
        mockMvc.perform(get("/v1/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("Get user by ID")
    void getUserById() throws Exception {
        when(userService.getUserById("user-1")).thenReturn(userResponse);
        mockMvc.perform(get("/v1/users/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("user-1"));
    }

    @Test
    @DisplayName("Create user")
    void createUser() throws Exception {
        when(jwtUtils.extractUserIdFromToken(any())).thenReturn("admin-1");
        when(userService.createUser(any(UserUpsertRequest.class), eq("admin-1"))).thenReturn(userResponse);
        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpsertRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("Replace user (PUT)")
    void replaceUser() throws Exception {
        when(jwtUtils.extractUserIdFromToken(any())).thenReturn("admin-1");
        when(userService.updateUserFully(eq("user-1"), any(UserUpsertRequest.class), eq("admin-1"))).thenReturn(userResponse);
        mockMvc.perform(put("/v1/users/user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpsertRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("user-1"));
    }

    @Test
    @DisplayName("Update user partially (PATCH)")
    void updateUserPartially() throws Exception {
        when(jwtUtils.extractUserIdFromToken(any())).thenReturn("admin-1");
        when(userService.updateUserPartially(eq("user-1"), any(UserPatchRequest.class), eq("admin-1"))).thenReturn(userResponse);
        mockMvc.perform(patch("/v1/users/user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userPatchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("user-1"));
    }

    @Test
    @DisplayName("Delete user (soft)")
    void deleteUser() throws Exception {
        when(jwtUtils.extractUserIdFromToken(any())).thenReturn("admin-1");
        mockMvc.perform(delete("/v1/users/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.message").value("User deleted successfully"));
    }

    @Test
    @DisplayName("Delete user (hard)")
    void hardDeleteUser() throws Exception {
        mockMvc.perform(delete("/v1/users/user-1/permanent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.message").value("User permanently deleted"));
    }

    @Test
    @DisplayName("Change password")
    void changePassword() throws Exception {
        when(jwtUtils.extractUserIdFromToken(any())).thenReturn("admin-1");
        when(userService.changePassword("user-1", "NewPassword1!", "admin-1")).thenReturn(userResponse);
        mockMvc.perform(patch("/v1/users/user-1/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.message").value("Password updated successfully"));
    }

    @Test
    @DisplayName("Toggle user active status")
    void toggleUserActiveStatus() throws Exception {
        when(jwtUtils.extractUserIdFromToken(any())).thenReturn("admin-1");
        when(userService.toggleUserActiveStatus("user-1", "admin-1")).thenReturn(userResponse);
        mockMvc.perform(patch("/v1/users/user-1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.message").value("User status toggled successfully"));
    }
}
