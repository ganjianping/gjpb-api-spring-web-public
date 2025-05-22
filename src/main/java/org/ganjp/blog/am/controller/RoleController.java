package org.ganjp.blog.am.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.am.model.dto.request.RoleRequest;
import org.ganjp.blog.am.model.dto.response.ApiResponse;
import org.ganjp.blog.am.model.dto.response.RoleResponse;
import org.ganjp.blog.am.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        try {
            List<RoleResponse> roles = roleService.getAllRoles();
            return ResponseEntity.ok(ApiResponse.success(roles, "Roles retrieved successfully"));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error retrieving roles", errors));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getActiveRoles() {
        try {
            List<RoleResponse> roles = roleService.getActiveRoles();
            return ResponseEntity.ok(ApiResponse.success(roles, "Active roles retrieved successfully"));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error retrieving active roles", errors));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable String id) {
        try {
            RoleResponse role = roleService.getRoleById(id);
            return ResponseEntity.ok(ApiResponse.success(role, "Role retrieved successfully"));
        } catch (RuntimeException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Role not found", errors));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error retrieving role", errors));
        }
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByCode(@PathVariable String code) {
        try {
            RoleResponse role = roleService.getRoleByCode(code);
            return ResponseEntity.ok(ApiResponse.success(role, "Role retrieved successfully"));
        } catch (RuntimeException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Role not found", errors));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error retrieving role", errors));
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleRequest roleRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            RoleResponse createdRole = roleService.createRole(roleRequest, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdRole, "Role created successfully"));
        } catch (RuntimeException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "Error creating role", errors));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error creating role", errors));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable String id,
            @Valid @RequestBody RoleRequest roleRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            RoleResponse updatedRole = roleService.updateRole(id, roleRequest, userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.success(updatedRole, "Role updated successfully"));
        } catch (RuntimeException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "Error updating role", errors));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error updating role", errors));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));
        } catch (RuntimeException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Role not found", errors));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error deleting role", errors));
        }
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<RoleResponse>> toggleRoleStatus(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            RoleResponse updatedRole = roleService.toggleRoleStatus(id, userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.success(updatedRole, "Role status toggled successfully"));
        } catch (RuntimeException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Role not found", errors));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error toggling role status", errors));
        }
    }
}