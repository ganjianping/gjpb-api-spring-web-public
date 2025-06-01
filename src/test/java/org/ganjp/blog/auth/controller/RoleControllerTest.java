package org.ganjp.blog.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ganjp.blog.auth.model.dto.request.RoleUpsertRequest;
import org.ganjp.blog.auth.model.dto.request.RolePatchRequest;
import org.ganjp.blog.auth.model.dto.response.RoleResponse;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.auth.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = RoleController.class,
    excludeFilters = @ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = org.ganjp.blog.common.audit.interceptor.AuthenticationAuditInterceptor.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtUtils jwtUtils;

    private RoleResponse roleResponse;
    private RoleUpsertRequest roleUpsertRequest;
    private RolePatchRequest rolePatchRequest;

    @BeforeEach
    void setUp() {
        roleResponse = RoleResponse.builder()
                .id("role-1")
                .code("ADMIN")
                .name("Administrator")
                .active(true)
                .build();
        roleUpsertRequest = RoleUpsertRequest.builder()
                .code("ADMIN")
                .name("Administrator")
                .active(true)
                .build();
        rolePatchRequest = RolePatchRequest.builder()
                .name("Updated Name")
                .build();
    }

    @Test
    @DisplayName("Get all roles")
    void getAllRoles() throws Exception {
        when(roleService.getAllRoles()).thenReturn(List.of(roleResponse));
        mockMvc.perform(get("/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("ADMIN"));
    }

    @Test
    @DisplayName("Get active roles")
    void getActiveRoles() throws Exception {
        when(roleService.getActiveRoles()).thenReturn(List.of(roleResponse));
        mockMvc.perform(get("/v1/roles/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].active").value(true));
    }

    @Test
    @DisplayName("Get role by ID")
    void getRoleById() throws Exception {
        when(roleService.getRoleById("role-1")).thenReturn(roleResponse);
        mockMvc.perform(get("/v1/roles/role-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("role-1"));
    }

    @Test
    @DisplayName("Get role by code")
    void getRoleByCode() throws Exception {
        when(roleService.getRoleByCode("ADMIN")).thenReturn(roleResponse);
        mockMvc.perform(get("/v1/roles/code/ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("ADMIN"));
    }

    @Test
    @DisplayName("Create role")
    void createRole() throws Exception {
        when(jwtUtils.extractUserIdFromToken(any())).thenReturn("admin-1");
        when(roleService.createRole(any(RoleUpsertRequest.class), eq("admin-1"))).thenReturn(roleResponse);
        mockMvc.perform(post("/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUpsertRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("ADMIN"));
    }

    @Test
    @DisplayName("Update role (PUT)")
    void updateRole() throws Exception {
        when(jwtUtils.extractUserId(any())).thenReturn("admin-1");
        when(roleService.updateRoleFully(eq("role-1"), any(RoleUpsertRequest.class), eq("admin-1"))).thenReturn(roleResponse);
        mockMvc.perform(put("/v1/roles/role-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUpsertRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.message").value("Role updated successfully"));
    }

    @Test
    @DisplayName("Update role partially (PATCH)")
    void updateRolePartially() throws Exception {
        when(jwtUtils.extractUserId(any())).thenReturn("admin-1");
        when(roleService.updateRolePartially(eq("role-1"), any(RolePatchRequest.class), eq("admin-1"))).thenReturn(roleResponse);
        mockMvc.perform(patch("/v1/roles/role-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rolePatchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.message").value("Role updated successfully"));
    }

    @Test
    @DisplayName("Delete role")
    void deleteRole() throws Exception {
        mockMvc.perform(delete("/v1/roles/role-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.message").value("Role deleted successfully"));
    }

    @Test
    @DisplayName("Toggle role status")
    void toggleRoleStatus() throws Exception {
        when(jwtUtils.extractUserId(any())).thenReturn("admin-1");
        when(roleService.toggleRoleStatus("role-1", "admin-1")).thenReturn(roleResponse);
        mockMvc.perform(patch("/v1/roles/role-1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.message").value("Role status toggled successfully"));
    }
}
