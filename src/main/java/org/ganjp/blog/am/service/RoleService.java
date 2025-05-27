package org.ganjp.blog.am.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.am.exception.DuplicateResourceException;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.am.model.dto.request.RoleRequest;
import org.ganjp.blog.am.model.dto.request.RoleUpdateRequest;
import org.ganjp.blog.am.model.dto.response.RoleResponse;
import org.ganjp.blog.am.model.entity.Role;
import org.ganjp.blog.am.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing roles in the authentication system.
 * Provides methods for creating, updating, deleting, and retrieving roles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Retrieves all roles sorted by sort order and name.
     *
     * @return List of all roles as DTOs
     */
    public List<RoleResponse> getAllRoles() {
        log.debug("Retrieving all roles");
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder", "name"))
                .stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves all roles with pagination support.
     *
     * @param pageable Pagination information
     * @return Page of role DTOs
     */
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        log.debug("Retrieving all roles with pagination: {}", pageable);
        return roleRepository.findAll(pageable)
                .map(this::mapToRoleResponse);
    }

    /**
     * Retrieves all active roles.
     *
     * @return List of active roles as DTOs
     */
    public List<RoleResponse> getActiveRoles() {
        log.debug("Retrieving all active roles");
        return roleRepository.findByActiveTrue()
                .stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a role by its ID.
     *
     * @param id The role ID
     * @return The role as a DTO
     * @throws ResourceNotFoundException if role not found
     */
    public RoleResponse getRoleById(String id) {
        Assert.hasText(id, "Role ID cannot be empty");
        log.debug("Retrieving role with ID: {}", id);
        return roleRepository.findById(id)
                .map(this::mapToRoleResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    /**
     * Retrieves a role by its code.
     *
     * @param code The role code
     * @return The role as a DTO
     * @throws ResourceNotFoundException if role not found
     */
    public RoleResponse getRoleByCode(String code) {
        Assert.hasText(code, "Role code cannot be empty");
        log.debug("Retrieving role with code: {}", code);
        return roleRepository.findByCode(code)
                .map(this::mapToRoleResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code", code));
    }

    /**
     * Creates a new role.
     *
     * @param roleRequest The role data
     * @param username The username of the user creating the role
     * @return The created role as a DTO
     * @throws DuplicateResourceException if a role with the same code already exists
     */
    @Transactional
    public RoleResponse createRole(RoleRequest roleRequest, String username) {
        Assert.notNull(roleRequest, "Role request cannot be null");
        Assert.hasText(roleRequest.getCode(), "Role code cannot be empty");
        Assert.hasText(roleRequest.getName(), "Role name cannot be empty");
        Assert.hasText(username, "Username cannot be empty");
        
        log.debug("Creating new role with code: {}", roleRequest.getCode());
        
        if (roleRepository.existsByCode(roleRequest.getCode())) {
            log.warn("Attempt to create duplicate role with code: {}", roleRequest.getCode());
            throw DuplicateResourceException.of("Role", "code", roleRequest.getCode());
        }

        LocalDateTime now = LocalDateTime.now();
        Role.RoleBuilder roleBuilder = Role.builder()
                .id(UUID.randomUUID().toString())
                .code(roleRequest.getCode())
                .name(roleRequest.getName())
                .description(roleRequest.getDescription())
                .sortOrder(roleRequest.getSortOrder() != null ? roleRequest.getSortOrder() : 0)
                .active(roleRequest.getActive() != null ? roleRequest.getActive() : true)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(username)
                .updatedBy(username);
                
        // Handle parent role and level
        if (roleRequest.getParentRoleId() != null) {
            Role parentRole = roleRepository.findById(roleRequest.getParentRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Role", "id", roleRequest.getParentRoleId()));
            
            roleBuilder.parentRole(parentRole);
            // Calculate level as parent's level + 1
            roleBuilder.level(parentRole.getLevel() + 1);
        } else {
            // Top level role
            roleBuilder.level(roleRequest.getLevel() != null ? roleRequest.getLevel() : 0);
        }
        
        Role role = roleBuilder.build();

        Role savedRole = roleRepository.save(role);
        log.info("Created new role with id: {} and code: {}", savedRole.getId(), savedRole.getCode());
        return mapToRoleResponse(savedRole);
    }

    /**
     * Updates an existing role with complete information.
     *
     * @param id The role ID to update
     * @param roleRequest The role data with all fields
     * @param username The username of the user updating the role
     * @return The updated role as a DTO
     * @throws ResourceNotFoundException if role not found
     * @throws DuplicateResourceException if the new code already exists for another role
     */
    @Transactional
    public RoleResponse updateRole(String id, RoleRequest roleRequest, String username) {
        Assert.hasText(id, "Role ID cannot be empty");
        Assert.notNull(roleRequest, "Role request cannot be null");
        Assert.hasText(roleRequest.getCode(), "Role code cannot be empty");
        Assert.hasText(roleRequest.getName(), "Role name cannot be empty");
        Assert.hasText(username, "Username cannot be empty");
        
        log.debug("Updating role with ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Check if code is being changed and if new code already exists
        if (!role.getCode().equals(roleRequest.getCode()) && roleRepository.existsByCode(roleRequest.getCode())) {
            log.warn("Attempt to update role with duplicate code: {}", roleRequest.getCode());
            throw DuplicateResourceException.of("Role", "code", roleRequest.getCode());
        }

        role.setCode(roleRequest.getCode());
        role.setName(roleRequest.getName());
        role.setDescription(roleRequest.getDescription());
        role.setSortOrder(roleRequest.getSortOrder() != null ? roleRequest.getSortOrder() : 0);
        role.setActive(roleRequest.getActive() != null ? roleRequest.getActive() : true);
        
        // Handle parent role and level
        if (roleRequest.getParentRoleId() != null) {
            Role parentRole = roleRepository.findById(roleRequest.getParentRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Role", "id", roleRequest.getParentRoleId()));
            
            role.setParentRole(parentRole);
            // Calculate level as parent's level + 1
            role.setLevel(parentRole.getLevel() + 1);
        } else if (roleRequest.getLevel() != null) {
            // Set explicit level if provided
            role.setLevel(roleRequest.getLevel());
            role.setParentRole(null);
        }
        
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(username);

        Role updatedRole = roleRepository.save(role);
        log.info("Updated role with id: {} and code: {}", updatedRole.getId(), updatedRole.getCode());
        return mapToRoleResponse(updatedRole);
    }

    /**
     * Updates an existing role partially, only changing the fields that are provided.
     *
     * @param id The role ID to update
     * @param updateRequest The role data with only fields that should be updated
     * @param username The username of the user updating the role
     * @return The updated role as a DTO
     * @throws ResourceNotFoundException if role not found
     * @throws DuplicateResourceException if the new code already exists for another role
     */
    @Transactional
    public RoleResponse updateRolePartially(String id, RoleUpdateRequest updateRequest, String username) {
        Assert.hasText(id, "Role ID cannot be empty");
        Assert.notNull(updateRequest, "Role update request cannot be null");
        Assert.hasText(username, "Username cannot be empty");
        
        log.debug("Partially updating role with ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Check if code is being changed and if new code already exists
        if (updateRequest.getCode() != null && !updateRequest.getCode().equals(role.getCode()) 
                && roleRepository.existsByCode(updateRequest.getCode())) {
            log.warn("Attempt to update role with duplicate code: {}", updateRequest.getCode());
            throw DuplicateResourceException.of("Role", "code", updateRequest.getCode());
        }

        // Only update fields that are not null in the request
        if (updateRequest.getCode() != null) {
            role.setCode(updateRequest.getCode());
        }
        
        if (updateRequest.getName() != null) {
            role.setName(updateRequest.getName());
        }
        
        if (updateRequest.getDescription() != null) {
            role.setDescription(updateRequest.getDescription());
        }
        
        if (updateRequest.getSortOrder() != null) {
            role.setSortOrder(updateRequest.getSortOrder());
        }
        
        // Handle parent role and level
        if (updateRequest.getParentRoleId() != null) {
            Role parentRole = roleRepository.findById(updateRequest.getParentRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Role", "id", updateRequest.getParentRoleId()));
            
            role.setParentRole(parentRole);
            // Calculate level as parent's level + 1
            role.setLevel(parentRole.getLevel() + 1);
        } else if (updateRequest.getLevel() != null) {
            // If explicitly setting level without parent
            role.setLevel(updateRequest.getLevel());
        }
        
        if (updateRequest.getActive() != null) {
            role.setActive(updateRequest.getActive());
        }
        
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(username);

        Role updatedRole = roleRepository.save(role);
        log.info("Partially updated role with id: {} and code: {}", updatedRole.getId(), updatedRole.getCode());
        return mapToRoleResponse(updatedRole);
    }

    /**
     * Deletes a role by its ID.
     *
     * @param id The role ID to delete
     * @throws ResourceNotFoundException if role not found
     */
    @Transactional
    public void deleteRole(String id) {
        Assert.hasText(id, "Role ID cannot be empty");
        log.debug("Deleting role with ID: {}", id);
        
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role", "id", id);
        }
        roleRepository.deleteById(id);
        log.info("Deleted role with ID: {}", id);
    }

    /**
     * Toggles the active status of a role.
     *
     * @param id The role ID to toggle status for
     * @param username The username of the user toggling the status
     * @return The updated role as a DTO
     * @throws ResourceNotFoundException if role not found
     */
    @Transactional
    public RoleResponse toggleRoleStatus(String id, String username) {
        Assert.hasText(id, "Role ID cannot be empty");
        Assert.hasText(username, "Username cannot be empty");
        log.debug("Toggling status for role with ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        
        boolean newStatus = !role.isActive();
        role.setActive(newStatus);
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(username);
        
        Role updatedRole = roleRepository.save(role);
        log.info("Toggled role status for ID: {} to {}", id, newStatus);
        return mapToRoleResponse(updatedRole);
    }

    /**
     * Maps a Role entity to a RoleResponse DTO.
     *
     * @param role The Role entity to map
     * @return The RoleResponse DTO
     */
    private RoleResponse mapToRoleResponse(Role role) {
        if (role == null) {
            return null;
        }
        
        // Get parent role ID if exists
        String parentRoleId = null;
        if (role.getParentRole() != null) {
            parentRoleId = role.getParentRole().getId();
        }
        
        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .sortOrder(role.getSortOrder())
                .level(role.getLevel())
                .parentRoleId(parentRoleId)
                .systemRole(role.isSystemRole())
                .active(role.isActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .build();
    }
    
    /**
     * Validates that a role exists by its ID.
     *
     * @param id The role ID to check
     * @throws ResourceNotFoundException if role not found
     */
    public void validateRoleExists(String id) {
        Assert.hasText(id, "Role ID cannot be empty");
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role", "id", id);
        }
    }
    
    /**
     * Retrieves a role entity by its code.
     *
     * @param code The role code
     * @return The Role entity
     * @throws ResourceNotFoundException if role not found
     */
    public Role getRoleEntityByCode(String code) {
        Assert.hasText(code, "Role code cannot be empty");
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code", code));
    }
}