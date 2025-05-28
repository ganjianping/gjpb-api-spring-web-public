package org.ganjp.blog.am.service;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.am.model.dto.request.RoleUpsertRequest;
import org.ganjp.blog.am.model.dto.request.RolePatchRequest;
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
    public RoleResponse createRole(RoleUpsertRequest roleCreateRequest, String userId) {
        if (roleRepository.existsByCode(roleCreateRequest.getCode())) {
            throw new RuntimeException("Role with code " + roleCreateRequest.getCode() + " already exists");
        }

        Role role = new Role();
        role.setId(UUID.randomUUID().toString());
        role.setCode(roleCreateRequest.getCode());
        role.setName(roleCreateRequest.getName());
        role.setDescription(roleCreateRequest.getDescription());
        
        // Set the parent role if parentRoleId is provided
        if (roleCreateRequest.getParentRoleId() != null && !roleCreateRequest.getParentRoleId().isEmpty()) {
            Role parentRole = roleRepository.findById(roleCreateRequest.getParentRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent Role", "id", roleCreateRequest.getParentRoleId()));
            role.setParentRole(parentRole);
            // Set level as one greater than parent's level
            role.setLevel(parentRole.getLevel() + 1);
        } else {
            // No parent, set as top level (0)
            role.setLevel(roleCreateRequest.getLevel() != null ? roleCreateRequest.getLevel() : 0);
        }
        
        role.setSortOrder(roleCreateRequest.getSortOrder() != null ? roleCreateRequest.getSortOrder() : 999);
        role.setActive(roleCreateRequest.getActive() != null ? roleCreateRequest.getActive() : true);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setCreatedBy(userId);
        role.setUpdatedBy(userId);

        Role savedRole = roleRepository.save(role);
        return mapToRoleResponse(savedRole);
    }

    @Transactional
    public RoleResponse updateRoleFully(String id, RoleUpsertRequest roleUpdateRequest, String userId) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Check if code is being changed and if new code already exists
        if (!role.getCode().equals(roleUpdateRequest.getCode()) && roleRepository.existsByCode(roleUpdateRequest.getCode())) {
            throw new RuntimeException("Role with code " + roleUpdateRequest.getCode() + " already exists");
        }

        role.setCode(roleUpdateRequest.getCode());
        role.setName(roleUpdateRequest.getName());
        role.setDescription(roleUpdateRequest.getDescription());
        role.setSortOrder(roleUpdateRequest.getSortOrder() != null ? roleUpdateRequest.getSortOrder() : 999);
        role.setActive(roleUpdateRequest.getActive() != null ? roleUpdateRequest.getActive() : true);
        
        // Update parent role if specified
        if (roleUpdateRequest.getParentRoleId() != null) {
            // Check for circular reference
            if (roleUpdateRequest.getParentRoleId().equals(id)) {
                throw new RuntimeException("Role cannot be its own parent");
            }
            
            // If parent role is changed
            if ((role.getParentRole() == null && !roleUpdateRequest.getParentRoleId().isEmpty()) ||
                (role.getParentRole() != null && 
                 !role.getParentRole().getId().equals(roleUpdateRequest.getParentRoleId()))) {
                
                // Only update if parent role ID has changed
                if (!roleUpdateRequest.getParentRoleId().isEmpty()) {
                    Role parentRole = roleRepository.findById(roleUpdateRequest.getParentRoleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Parent Role", "id", roleUpdateRequest.getParentRoleId()));
                    role.setParentRole(parentRole);
                    role.setLevel(parentRole.getLevel() + 1);
                } else {
                    // Remove parent role
                    role.setParentRole(null);
                    role.setLevel(0);
                }
            }
        } else {
            role.setParentRole(null);
            role.setLevel(0);
        }
        
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(userId);

        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Transactional
    public RoleResponse updateRolePartially(String id, RolePatchRequest rolePatchRequest, String username) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Check if code is being changed and if new code already exists
        if (rolePatchRequest.getCode() != null && !rolePatchRequest.getCode().equals(role.getCode())
                && roleRepository.existsByCode(rolePatchRequest.getCode())) {
            throw new RuntimeException("Role with code " + rolePatchRequest.getCode() + " already exists");
        }

        // Only update fields that are not null in the request
        if (rolePatchRequest.getCode() != null) {
            role.setCode(rolePatchRequest.getCode());
        }
        
        if (rolePatchRequest.getName() != null) {
            role.setName(rolePatchRequest.getName());
        }
        
        if (rolePatchRequest.getDescription() != null) {
            role.setDescription(rolePatchRequest.getDescription());
        }
        
        if (rolePatchRequest.getSortOrder() != null) {
            role.setSortOrder(rolePatchRequest.getSortOrder());
        }
        
        if (rolePatchRequest.getActive() != null) {
            role.setActive(rolePatchRequest.getActive());
        }
        
        // Update parent role if specified
        if (rolePatchRequest.getParentRoleId() != null) {
            // Check for circular reference
            if (rolePatchRequest.getParentRoleId().equals(id)) {
                throw new RuntimeException("Role cannot be its own parent");
            }
            
            // If empty string, remove parent
            if (rolePatchRequest.getParentRoleId().isEmpty()) {
                role.setParentRole(null);
                
                // Update level if provided, otherwise set to default top level
                if (rolePatchRequest.getLevel() != null) {
                    role.setLevel(rolePatchRequest.getLevel());
                } else {
                    role.setLevel(0);
                }
            } else {
                Role parentRole = roleRepository.findById(rolePatchRequest.getParentRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Role", "id", rolePatchRequest.getParentRoleId()));
                role.setParentRole(parentRole);
                role.setLevel(parentRole.getLevel() + 1);
            }
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