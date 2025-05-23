package org.ganjp.blog.am.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.am.model.dto.request.RoleRequest;
import org.ganjp.blog.am.model.dto.request.RoleUpdateRequest;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.am.model.dto.response.RoleResponse;
import org.ganjp.blog.am.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles, "Roles retrieved successfully"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getActiveRoles() {
        List<RoleResponse> roles = roleService.getActiveRoles();
        return ResponseEntity.ok(ApiResponse.success(roles, "Active roles retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable String id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role, "Role retrieved successfully"));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByCode(@PathVariable String code) {
        RoleResponse role = roleService.getRoleByCode(code);
        return ResponseEntity.ok(ApiResponse.success(role, "Role retrieved successfully"));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleRequest roleRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        RoleResponse createdRole = roleService.createRole(roleRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdRole, "Role created successfully"));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable String id,
            @Valid @RequestBody RoleRequest roleRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        RoleResponse updatedRole = roleService.updateRole(id, roleRequest, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(updatedRole, "Role updated successfully"));
    }
    
    /**
     * Partially updates a role with only the fields provided in the request.
     * This allows for updating individual fields without needing to send the entire role object.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRolePartially(
            @PathVariable String id,
            @Valid @RequestBody RoleUpdateRequest updateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        RoleResponse updatedRole = roleService.updateRolePartially(id, updateRequest, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(updatedRole, "Role updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<RoleResponse>> toggleRoleStatus(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        RoleResponse updatedRole = roleService.toggleRoleStatus(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(updatedRole, "Role status toggled successfully"));
    }
}