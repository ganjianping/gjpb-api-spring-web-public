package org.ganjp.blog.am.service;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.am.model.dto.request.UserCreateRequest;
import org.ganjp.blog.am.model.dto.request.UserUpdateRequest;
import org.ganjp.blog.am.model.dto.response.RoleResponse;
import org.ganjp.blog.am.model.dto.response.UserResponse;
import org.ganjp.blog.am.model.entity.Role;
import org.ganjp.blog.am.model.entity.User;
import org.ganjp.blog.am.model.entity.UserRole;
import org.ganjp.blog.am.model.enums.AccountStatus;
import org.ganjp.blog.am.repository.RoleRepository;
import org.ganjp.blog.am.repository.UserRepository;
import org.ganjp.blog.am.repository.UserRoleRepository;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users with pagination
     *
     * @param pageable pagination information
     * @return Page of UserResponse objects
     */
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }
    
    /**
     * Find users by username containing the provided string
     *
     * @param username username substring to search for
     * @param pageable pagination information
     * @return Page of UserResponse objects
     */
    public Page<UserResponse> findUsersByUsernameContaining(String username, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(username, pageable)
                .map(this::mapToUserResponse);
    }

    /**
     * Get a user by ID
     *
     * @param id user ID
     * @return UserResponse
     * @throws ResourceNotFoundException if user not found
     */
    public UserResponse getUserById(String id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
    
    /**
     * Get a user by username
     *
     * @param username username to search for
     * @return UserResponse
     * @throws ResourceNotFoundException if user not found
     */
    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Create a new user
     *
     * @param userCreateRequest user data
     * @param currentUserId ID of the user performing the operation
     * @return UserResponse
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest userCreateRequest, String currentUserId) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(userCreateRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Validate email uniqueness if provided
        if (userCreateRequest.getEmail() != null && userRepository.existsByEmail(userCreateRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Validate mobile uniqueness if provided
        if (userCreateRequest.getMobileCountryCode() != null && userCreateRequest.getMobileNumber() != null &&
                userRepository.existsByMobileCountryCodeAndMobileNumber(
                        userCreateRequest.getMobileCountryCode(), userCreateRequest.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number is already registered");
        }

        // Create user entity
        String userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(userId)
                .username(userCreateRequest.getUsername())
                .nickname(userCreateRequest.getNickname())
                .email(userCreateRequest.getEmail())
                .mobileCountryCode(userCreateRequest.getMobileCountryCode())
                .mobileNumber(userCreateRequest.getMobileNumber())
                .password(passwordEncoder.encode(userCreateRequest.getPassword()))
                .accountStatus(userCreateRequest.getAccountStatus() != null ? userCreateRequest.getAccountStatus() : AccountStatus.pending_verification)
                .active(userCreateRequest.getActive() != null ? userCreateRequest.getActive() : true)
                .passwordChangedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        User savedUser = userRepository.save(user);

        // Assign roles if provided
        if (userCreateRequest.getRoleCodes() != null && !userCreateRequest.getRoleCodes().isEmpty()) {
            assignRolesToUser(savedUser, userCreateRequest.getRoleCodes(), currentUserId);
        }

        return mapToUserResponse(savedUser);
    }

    /**
     * Update an existing user
     *
     * @param id user ID to update
     * @param userUpdateRequest updated user data
     * @param currentUserId ID of the user performing the operation
     * @return UserResponse
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserResponse updateUser(String id, UserUpdateRequest userUpdateRequest, String currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check username uniqueness if it's being changed
        if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().equals(user.getUsername())
                && userRepository.existsByUsername(userUpdateRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Check email uniqueness if it's being changed
        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(userUpdateRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Check mobile uniqueness if it's being changed
        if (userUpdateRequest.getMobileCountryCode() != null && userUpdateRequest.getMobileNumber() != null
                && (user.getMobileCountryCode() == null || user.getMobileNumber() == null
                || !userUpdateRequest.getMobileCountryCode().equals(user.getMobileCountryCode())
                || !userUpdateRequest.getMobileNumber().equals(user.getMobileNumber()))
                && userRepository.existsByMobileCountryCodeAndMobileNumber(
                userUpdateRequest.getMobileCountryCode(), userUpdateRequest.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number is already registered");
        }

        // Update fields if provided
        if (userUpdateRequest.getUsername() != null) {
            user.setUsername(userUpdateRequest.getUsername());
        }

        if (userUpdateRequest.getNickname() != null) {
            user.setNickname(userUpdateRequest.getNickname());
        }

        if (userUpdateRequest.getEmail() != null) {
            user.setEmail(userUpdateRequest.getEmail());
        }

        // Update mobile fields together
        if (userUpdateRequest.getMobileCountryCode() != null && userUpdateRequest.getMobileNumber() != null) {
            user.setMobileCountryCode(userUpdateRequest.getMobileCountryCode());
            user.setMobileNumber(userUpdateRequest.getMobileNumber());
        } else if (userUpdateRequest.getMobileCountryCode() == null && userUpdateRequest.getMobileNumber() == null
                   && user.getMobileCountryCode() != null && user.getMobileNumber() != null) {
            // Clear mobile fields if explicitly set to null
            user.setMobileCountryCode(null);
            user.setMobileNumber(null);
        }

        // Update password if provided
        if (userUpdateRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
            user.setPasswordChangedAt(LocalDateTime.now());
        }

        // Update account status if provided
        if (userUpdateRequest.getAccountStatus() != null) {
            user.setAccountStatus(userUpdateRequest.getAccountStatus());
        }

        // Update active status if provided
        if (userUpdateRequest.getActive() != null) {
            user.setActive(userUpdateRequest.getActive());
        }

        // Update audit fields
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUserId);

        User updatedUser = userRepository.save(user);

        // Update roles if provided
        if (userUpdateRequest.getRoleCodes() != null) {
            // Remove existing roles
            List<UserRole> existingUserRoles = userRoleRepository.findByUserId(id);
            userRoleRepository.deleteAll(existingUserRoles);
            
            // Assign new roles
            if (!userUpdateRequest.getRoleCodes().isEmpty()) {
                assignRolesToUser(updatedUser, userUpdateRequest.getRoleCodes(), currentUserId);
            }
        }

        return mapToUserResponse(updatedUser);
    }

    /**
     * Delete a user by ID (soft delete)
     *
     * @param id user ID
     * @param currentUserId ID of the user performing the operation
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(String id, String currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Perform soft delete
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUserId);

        userRepository.save(user);
    }

    /**
     * Hard delete a user by ID (use with caution)
     *
     * @param id user ID
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void hardDeleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Change a user's password
     *
     * @param id user ID
     * @param newPassword new password
     * @param currentUserId ID of the user performing the operation
     * @return UserResponse
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserResponse changePassword(String id, String newPassword, String currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUserId);

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    /**
     * Toggle a user's active status
     *
     * @param id user ID
     * @param currentUserId ID of the user performing the operation
     * @return UserResponse
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserResponse toggleUserActiveStatus(String id, String currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setActive(!user.isActive());
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUserId);

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    /**
     * Set a user's account status
     *
     * @param id user ID
     * @param status new account status
     * @param currentUserId ID of the user performing the operation
     * @return UserResponse
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserResponse setAccountStatus(String id, org.ganjp.blog.am.model.enums.AccountStatus status, String currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setAccountStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUserId);

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    /**
     * Assign roles to a user
     *
     * @param user User entity
     * @param roleCodes Set of role codes
     * @param currentUserId ID of the user performing the operation
     */
    private void assignRolesToUser(User user, Set<String> roleCodes, String currentUserId) {
        List<Role> roles = new ArrayList<>();
        for (String code : roleCodes) {
            roleRepository.findByCode(code).ifPresent(roles::add);
        }
        
        // Validate all requested roles exist
        if (roles.size() != roleCodes.size()) {
            throw new IllegalArgumentException("One or more role codes are invalid");
        }

        LocalDateTime now = LocalDateTime.now();

        List<UserRole> userRoles = roles.stream().map(role -> {
            return UserRole.builder()
                    .user(user)
                    .role(role)
                    .grantedAt(now)
                    .grantedBy(currentUserId)
                    .createdAt(now)
                    .updatedAt(now)
                    .createdBy(currentUserId)
                    .updatedBy(currentUserId)
                    .active(true)
                    .build();
        }).collect(Collectors.toList());

        userRoleRepository.saveAll(userRoles);
    }

    /**
     * Map User entity to UserResponse DTO
     *
     * @param user User entity
     * @return UserResponse
     */
    private UserResponse mapToUserResponse(User user) {
        // Explicitly fetch user roles from the repository since they're marked as @Transient in User entity
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        
        List<RoleResponse> roleResponses = userRoles.stream()
                .filter(ur -> ur.isActive() && (ur.getExpiresAt() == null || ur.getExpiresAt().isAfter(LocalDateTime.now())))
                .map(ur -> {
                    Role role = ur.getRole();
                    return RoleResponse.builder()
                            .id(role.getId())
                            .code(role.getCode())
                            .name(role.getName())
                            .description(role.getDescription())
                            .sortOrder(role.getSortOrder())
                            .level(role.getLevel())
                            .parentRoleId(role.getParentRole() != null ? role.getParentRole().getId() : null)
                            .active(role.isActive())
                            .systemRole(role.isSystemRole())
                            .createdAt(role.getCreatedAt())
                            .updatedAt(role.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .mobileCountryCode(user.getMobileCountryCode())
                .mobileNumber(user.getMobileNumber())
                .accountStatus(user.getAccountStatus())
                .active(user.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .passwordChangedAt(user.getPasswordChangedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roleResponses)
                .build();
    }
}
