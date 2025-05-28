package org.ganjp.blog.am.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.am.model.dto.request.PasswordChangeRequest;
import org.ganjp.blog.am.model.dto.request.UserUpsertRequest;
import org.ganjp.blog.am.model.dto.request.UserPatchRequest;
import org.ganjp.blog.am.model.dto.response.UserResponse;
import org.ganjp.blog.am.model.enums.AccountStatus;
import org.ganjp.blog.am.security.JwtUtils;
import org.ganjp.blog.am.service.UserService;
import org.ganjp.blog.common.model.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * Get all users with pagination
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @param sort Sort field
     * @param direction Sort direction (asc or desc)
     * @param username Optional username for filtering
     * @return Paginated list of users
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String username) {
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        Page<UserResponse> users;
        
        if (username != null && !username.trim().isEmpty()) {
            users = userService.findUsersByUsernameContaining(username, pageable);
        } else {
            users = userService.getAllUsers(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
    
    /**
     * Get user by username
     * 
     * @param username Username to search for
     * @return User details if found
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    /**
     * Get a user by ID
     * 
     * @param id User ID
     * @return User details
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    /**
     * Create a new user
     * 
     * @param userUpsertRequest User creation/update request
     * @param request HTTP request for extracting the current user
     * @return Created user details
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserUpsertRequest userUpsertRequest,
            HttpServletRequest request) {
        
        String userId = jwtUtils.extractUserIdFromToken(request);
        UserResponse createdUser = userService.createUser(userUpsertRequest, userId);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "User created successfully"));
    }

    /**
     * Replace an existing user (complete update with PUT)
     *
     * This endpoint follows RESTful convention for PUT operations:
     * - Requires all fields to be provided (complete representation)
     * - Replaces the entire resource with the new representation
     * - Should be idempotent (same result regardless of how many times called)
     *
     * @param id User ID
     * @param userUpsertRequest User data for complete replacement
     * @param request HTTP request for extracting the current user
     * @return Updated user details
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> replaceUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpsertRequest userUpsertRequest,
            HttpServletRequest request) {

        String userId = jwtUtils.extractUserIdFromToken(request);
        UserResponse updatedUser = userService.updateUserFully(id, userUpsertRequest, userId);

        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User replaced successfully"));
    }

    /**
     * Update an existing user (partial update with PATCH)
     * 
     * This endpoint follows RESTful convention for PATCH operations:
     * - Allows partial updates (only the fields provided will be updated)
     * - Preserves existing data for fields not included in the request
     * - Used for making partial modifications to a resource
     * 
     * @param id User ID
     * @param userPatchRequest User patch request for partial updates
     * @param request HTTP request for extracting the current user
     * @return Updated user details
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserPartially(
            @PathVariable String id,
            @Valid @RequestBody UserPatchRequest userPatchRequest,
            HttpServletRequest request) {
        
        String userId = jwtUtils.extractUserIdFromToken(request);
        UserResponse updatedUser = userService.updateUserPartially(id, userPatchRequest, userId);
        
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    /**
     * Delete a user (soft delete)
     * 
     * @param id User ID
     * @param request HTTP request for extracting the current user
     * @return Success message
     */
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String id,
            HttpServletRequest request) {
        
        String userId = jwtUtils.extractUserIdFromToken(request);
        userService.deleteUser(id, userId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    /**
     * Permanently delete a user from the database (hard delete)
     * 
     * @param id User ID
     * @return Success message
     */
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> hardDeleteUser(@PathVariable String id) {
        userService.hardDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User permanently deleted"));
    }

    /**
     * Change a user's password
     * 
     * @param id User ID
     * @param passwordChangeRequest New password request
     * @param request HTTP request for extracting the current user
     * @return Updated user details
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(
            @PathVariable String id,
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest,
            HttpServletRequest request) {
        
        String userId = jwtUtils.extractUserIdFromToken(request);
        UserResponse updatedUser = userService.changePassword(id, passwordChangeRequest.getPassword(), userId);
        
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Password updated successfully"));
    }

    /**
     * Toggle a user's active status
     * 
     * @param id User ID
     * @param request HTTP request for extracting the current user
     * @return Updated user details
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserActiveStatus(
            @PathVariable String id,
            HttpServletRequest request) {
        
        String userId = jwtUtils.extractUserIdFromToken(request);
        UserResponse updatedUser = userService.toggleUserActiveStatus(id, userId);
        
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User status toggled successfully"));
    }

    /**
     * Update a user's account status
     * 
     * @param id User ID
     * @param status New account status
     * @param request HTTP request for extracting the current user
     * @return Updated user details
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateAccountStatus(
            @PathVariable String id,
            @RequestBody AccountStatus status,
            HttpServletRequest request) {
        
        String userId = jwtUtils.extractUserIdFromToken(request);
        UserResponse updatedUser = userService.setAccountStatus(id, status, userId);
        
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Account status updated successfully"));
    }
}
