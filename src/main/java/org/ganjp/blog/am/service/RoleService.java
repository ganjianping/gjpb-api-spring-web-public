package org.ganjp.blog.am.service;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.am.model.dto.request.RoleRequest;
import org.ganjp.blog.am.model.dto.request.RoleUpdateRequest;
import org.ganjp.blog.am.model.dto.response.RoleResponse;
import org.ganjp.blog.am.model.entity.Role;
import org.ganjp.blog.am.repository.RoleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder", "name"))
                .stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    public List<RoleResponse> getActiveRoles() {
        return roleRepository.findByActiveTrue()
                .stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    public RoleResponse getRoleById(String id) {
        return roleRepository.findById(id)
                .map(this::mapToRoleResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    public RoleResponse getRoleByCode(String code) {
        return roleRepository.findByCode(code)
                .map(this::mapToRoleResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code", code));
    }

    @Transactional
    public RoleResponse createRole(RoleRequest roleRequest, String userId) {
        if (roleRepository.existsByCode(roleRequest.getCode())) {
            throw new RuntimeException("Role with code " + roleRequest.getCode() + " already exists");
        }

        Role role = new Role();
        role.setId(UUID.randomUUID().toString());
        role.setCode(roleRequest.getCode());
        role.setName(roleRequest.getName());
        role.setDescription(roleRequest.getDescription());
        
        // Set the parent role if parentRoleId is provided
        if (roleRequest.getParentRoleId() != null && !roleRequest.getParentRoleId().isEmpty()) {
            Role parentRole = roleRepository.findById(roleRequest.getParentRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent Role", "id", roleRequest.getParentRoleId()));
            role.setParentRole(parentRole);
            // Set level as one greater than parent's level
            role.setLevel(parentRole.getLevel() + 1);
        } else {
            // No parent, set as top level (0)
            role.setLevel(roleRequest.getLevel() != null ? roleRequest.getLevel() : 0);
        }
        
        role.setSortOrder(roleRequest.getSortOrder() != null ? roleRequest.getSortOrder() : role.getLevel());
        role.setActive(roleRequest.getActive() != null ? roleRequest.getActive() : true);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setCreatedBy(userId);
        role.setUpdatedBy(userId);

        Role savedRole = roleRepository.save(role);
        return mapToRoleResponse(savedRole);
    }

    @Transactional
    public RoleResponse updateRole(String id, RoleRequest roleRequest, String userId) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Check if code is being changed and if new code already exists
        if (!role.getCode().equals(roleRequest.getCode()) && roleRepository.existsByCode(roleRequest.getCode())) {
            throw new RuntimeException("Role with code " + roleRequest.getCode() + " already exists");
        }

        role.setCode(roleRequest.getCode());
        role.setName(roleRequest.getName());
        role.setDescription(roleRequest.getDescription());
        role.setSortOrder(roleRequest.getSortOrder() != null ? roleRequest.getSortOrder() : 999);
        role.setActive(roleRequest.getActive() != null ? roleRequest.getActive() : true);
        
        // Update parent role if specified
        if (roleRequest.getParentRoleId() != null) {
            // Check for circular reference
            if (roleRequest.getParentRoleId().equals(id)) {
                throw new RuntimeException("Role cannot be its own parent");
            }
            
            // If parent role is changed
            if ((role.getParentRole() == null && !roleRequest.getParentRoleId().isEmpty()) ||
                (role.getParentRole() != null && 
                 !role.getParentRole().getId().equals(roleRequest.getParentRoleId()))) {
                
                // Only update if parent role ID has changed
                if (!roleRequest.getParentRoleId().isEmpty()) {
                    Role parentRole = roleRepository.findById(roleRequest.getParentRoleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Parent Role", "id", roleRequest.getParentRoleId()));
                    role.setParentRole(parentRole);
                    role.setLevel(parentRole.getLevel() + 1);
                } else {
                    // Remove parent role
                    role.setParentRole(null);
                    role.setLevel(roleRequest.getLevel() != null ? roleRequest.getLevel() : 0);
                }
            }
        }
        
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(userId);

        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Transactional
    public RoleResponse updateRolePartially(String id, RoleUpdateRequest updateRequest, String username) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Check if code is being changed and if new code already exists
        if (updateRequest.getCode() != null && !updateRequest.getCode().equals(role.getCode()) 
                && roleRepository.existsByCode(updateRequest.getCode())) {
            throw new RuntimeException("Role with code " + updateRequest.getCode() + " already exists");
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
        
        if (updateRequest.getActive() != null) {
            role.setActive(updateRequest.getActive());
        }
        
        // Update parent role if specified
        if (updateRequest.getParentRoleId() != null) {
            // Check for circular reference
            if (updateRequest.getParentRoleId().equals(id)) {
                throw new RuntimeException("Role cannot be its own parent");
            }
            
            // If empty string, remove parent
            if (updateRequest.getParentRoleId().isEmpty()) {
                role.setParentRole(null);
                
                // Update level if provided, otherwise set to default top level
                if (updateRequest.getLevel() != null) {
                    role.setLevel(updateRequest.getLevel());
                } else {
                    role.setLevel(0);
                }
            } else {
                Role parentRole = roleRepository.findById(updateRequest.getParentRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Role", "id", updateRequest.getParentRoleId()));
                role.setParentRole(parentRole);
                role.setLevel(parentRole.getLevel() + 1);
            }
        } else if (updateRequest.getLevel() != null) {
            // If only level is updated
            role.setLevel(updateRequest.getLevel());
        }
        
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(username);

        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Transactional
    public void deleteRole(String id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Transactional
    public RoleResponse toggleRoleStatus(String id, String username) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        
        role.setActive(!role.isActive());
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(username);
        
        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    private RoleResponse mapToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .sortOrder(role.getSortOrder())
                .level(role.getLevel())
                .parentRoleId(role.getParentRole() != null ? role.getParentRole().getId() : null)
                .systemRole(role.isSystemRole())
                .active(role.isActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .build();
    }
}